package com.yelpclone.photo;

import com.yelpclone.business.BusinessRepository;
import com.yelpclone.common.NotFoundException;
import com.yelpclone.config.AppProperties;
import com.yelpclone.photo.PhotoDtos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Photo uploads never stream through the API server. The client asks for a presigned PUT URL,
 * uploads the bytes straight to S3 (LocalStack locally), then confirms the key so we record it.
 */
@Service
public class PhotoService {

    private static final Map<String, String> EXTENSIONS = Map.of(
        "image/jpeg", "jpg", "image/png", "png", "image/webp", "webp", "image/gif", "gif");

    private final PhotoRepository photos;
    private final BusinessRepository businesses;
    private final S3Client s3;
    private final S3Presigner presigner;
    private final AppProperties.S3 cfg;

    public PhotoService(PhotoRepository photos, BusinessRepository businesses, S3Client s3,
                        S3Presigner presigner, AppProperties props) {
        this.photos = photos;
        this.businesses = businesses;
        this.s3 = s3;
        this.presigner = presigner;
        this.cfg = props.s3();
    }

    public UploadUrlResponse createUploadUrl(long businessId, UploadUrlRequest req) {
        requireBusiness(businessId);
        String key = keyPrefix(businessId) + UUID.randomUUID() + "." + EXTENSIONS.get(req.contentType());
        Duration ttl = Duration.ofMinutes(cfg.presignMinutes());
        var presigned = presigner.presignPutObject(p -> p
            .signatureDuration(ttl)
            .putObjectRequest(PutObjectRequest.builder()
                .bucket(cfg.bucket()).key(key).contentType(req.contentType()).build()));
        return new UploadUrlResponse(presigned.url().toString(), key, req.contentType(), ttl.toSeconds());
    }

    @Transactional
    public PhotoResponse confirm(long businessId, long userId, ConfirmPhotoRequest req) {
        requireBusiness(businessId);
        if (!req.key().startsWith(keyPrefix(businessId))) {
            throw new IllegalArgumentException("Key does not belong to this business");
        }
        try {
            s3.headObject(HeadObjectRequest.builder().bucket(cfg.bucket()).key(req.key()).build());
        } catch (NoSuchKeyException e) {
            throw new IllegalArgumentException("No uploaded object found for key " + req.key());
        } catch (S3Exception e) {
            if (e.statusCode() == 404) throw new IllegalArgumentException("No uploaded object found for key " + req.key());
            throw e;
        }
        Photo photo = photos.save(new Photo(businessId, userId, req.key(), req.caption()));
        return toResponse(photo);
    }

    @Transactional(readOnly = true)
    public List<PhotoResponse> list(long businessId) {
        return photos.findTop20ByBusinessIdOrderByCreatedAtDesc(businessId).stream().map(this::toResponse).toList();
    }

    private PhotoResponse toResponse(Photo p) {
        // Bucket stays private; each view gets a short-lived signed GET URL.
        var url = presigner.presignGetObject(g -> g
            .signatureDuration(Duration.ofMinutes(cfg.presignMinutes()))
            .getObjectRequest(GetObjectRequest.builder().bucket(cfg.bucket()).key(p.getS3Key()).build())).url();
        return new PhotoResponse(p.getId(), url.toString(), p.getCaption(), p.getCreatedAt());
    }

    private void requireBusiness(long businessId) {
        if (!businesses.existsById(businessId)) throw new NotFoundException("Business " + businessId + " not found");
    }

    private static String keyPrefix(long businessId) {
        return "businesses/" + businessId + "/";
    }
}
