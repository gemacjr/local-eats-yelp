package com.yelpclone.review;

import com.yelpclone.auth.CurrentUser;
import com.yelpclone.common.PageResponse;
import com.yelpclone.review.ReviewDtos.CreateReviewRequest;
import com.yelpclone.review.ReviewDtos.ReviewResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/businesses/{businessId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public PageResponse<ReviewResponse> list(@PathVariable long businessId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return reviewService.list(businessId, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@PathVariable long businessId,
                                 @AuthenticationPrincipal Jwt jwt,
                                 @Valid @RequestBody CreateReviewRequest req) {
        return reviewService.create(businessId, CurrentUser.id(jwt), req);
    }
}
