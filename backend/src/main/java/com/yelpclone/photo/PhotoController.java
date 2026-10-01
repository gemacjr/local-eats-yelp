package com.yelpclone.photo;

import com.yelpclone.auth.CurrentUser;
import com.yelpclone.photo.PhotoDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses/{businessId}/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @GetMapping
    public List<PhotoResponse> list(@PathVariable long businessId) {
        return photoService.list(businessId);
    }

    @PostMapping("/upload-url")
    public UploadUrlResponse uploadUrl(@PathVariable long businessId, @Valid @RequestBody UploadUrlRequest req) {
        return photoService.createUploadUrl(businessId, req);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PhotoResponse confirm(@PathVariable long businessId, @AuthenticationPrincipal Jwt jwt,
                                 @Valid @RequestBody ConfirmPhotoRequest req) {
        return photoService.confirm(businessId, CurrentUser.id(jwt), req);
    }
}
