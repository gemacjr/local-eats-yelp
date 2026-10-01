package com.yelpclone.review;

import com.yelpclone.auth.UserRepository;
import com.yelpclone.business.Business;
import com.yelpclone.business.BusinessRepository;
import com.yelpclone.common.ConflictException;
import com.yelpclone.common.NotFoundException;
import com.yelpclone.common.OffsetPageable;
import com.yelpclone.common.PageResponse;
import com.yelpclone.review.ReviewDtos.CreateReviewRequest;
import com.yelpclone.review.ReviewDtos.ReviewResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);
    private static final int MAX_ATTEMPTS = 10;

    private final ReviewRepository reviews;
    private final BusinessRepository businesses;
    private final UserRepository users;
    private final TransactionTemplate tx;

    public ReviewService(ReviewRepository reviews, BusinessRepository businesses, UserRepository users,
                         PlatformTransactionManager txManager) {
        this.reviews = reviews;
        this.businesses = businesses;
        this.users = users;
        this.tx = new TransactionTemplate(txManager);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> list(long businessId, int page, int size) {
        int safeSize = Math.clamp(size, 1, 50);
        int safePage = Math.max(0, page);
        if (!businesses.existsById(businessId)) {
            throw new NotFoundException("Business " + businessId + " not found");
        }
        // Overfetch by one row to compute hasMore. Offsets are applied manually so page size stays exact.
        var rows = reviews.findByBusinessIdOrderByCreatedAtDescIdDesc(businessId,
            new OffsetPageable(safePage * safeSize, safeSize + 1));
        return PageResponse.fromOverfetch(rows, safePage, safeSize).map(ReviewResponse::from);
    }

    /**
     * Insert the review and fold its rating into the business aggregates in ONE transaction.
     * <ul>
     *   <li>Duplicate review -> the unique (user_id, business_id) constraint fails -> 409.</li>
     *   <li>Two reviews racing on the same business -> @Version check fails for the loser,
     *       whose whole transaction (review insert included) rolls back and is retried.</li>
     * </ul>
     */
    public ReviewResponse create(long businessId, long userId, CreateReviewRequest req) {
        for (int attempt = 1; ; attempt++) {
            try {
                return tx.execute(status -> createOnce(businessId, userId, req));
            } catch (ObjectOptimisticLockingFailureException e) {
                if (attempt >= MAX_ATTEMPTS) throw e;
                log.debug("Rating update conflict on business {} (attempt {}), retrying", businessId, attempt);
                backoff(attempt);
            } catch (DataIntegrityViolationException e) {
                throw new ConflictException("You have already reviewed this business");
            }
        }
    }

    /** Jittered backoff so writers that lost the same race don't collide again in lockstep. */
    private static void backoff(int attempt) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(5, 20L * attempt));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while retrying review", ie);
        }
    }

    private ReviewResponse createOnce(long businessId, long userId, CreateReviewRequest req) {
        Business business = businesses.findById(businessId)
            .orElseThrow(() -> new NotFoundException("Business " + businessId + " not found"));
        // Fast path for a friendly error; the DB constraint is the real guarantee under concurrency.
        if (reviews.existsByBusinessIdAndAuthorId(businessId, userId)) {
            throw new ConflictException("You have already reviewed this business");
        }
        var author = users.getReferenceById(userId);
        String text = req.text() == null || req.text().isBlank() ? null : req.text().trim();
        Review review = reviews.saveAndFlush(new Review(businessId, author, req.rating(), text));
        business.applyRating(req.rating());
        businesses.flush();
        return new ReviewResponse(review.getId(), businessId, userId,
            users.findById(userId).map(u -> u.getDisplayName()).orElse("unknown"),
            review.getRating(), review.getText(), review.getCreatedAt());
    }
}
