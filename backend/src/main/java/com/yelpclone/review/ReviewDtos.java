package com.yelpclone.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class ReviewDtos {
    private ReviewDtos() {}

    public record CreateReviewRequest(
        @NotNull @Min(1) @Max(5) Integer rating,
        @Size(max = 5000) String text) {}

    public record ReviewResponse(Long id, Long businessId, Long userId, String authorName,
                                 int rating, String text, Instant createdAt) {
        static ReviewResponse from(Review r) {
            return new ReviewResponse(r.getId(), r.getBusinessId(), r.getAuthor().getId(),
                r.getAuthor().getDisplayName(), r.getRating(), r.getText(), r.getCreatedAt());
        }
    }
}
