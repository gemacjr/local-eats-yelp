package com.yelpclone.business;

import com.yelpclone.photo.PhotoDtos.PhotoResponse;
import com.yelpclone.review.ReviewDtos.ReviewResponse;
import jakarta.validation.constraints.*;

import java.util.List;

public final class BusinessDtos {
    private BusinessDtos() {}

    public enum SortOrder { RELEVANCE, RATING, DISTANCE }

    public record SearchCriteria(
        String query,
        String category,
        Double latitude,
        Double longitude,
        Double radiusMeters,
        String locationSlug,
        SortOrder sort,
        int page,
        int size) {

        public boolean hasPoint() {
            return latitude != null && longitude != null;
        }

        public boolean hasQuery() {
            return query != null && !query.isBlank();
        }
    }

    public record BusinessSummary(
        Long id, String name, String category, String address,
        double latitude, double longitude,
        double avgRating, int numRatings,
        Double distanceMeters) {}

    public record BusinessDetail(
        Long id, String name, String description, String category, String address,
        String phone, String website, double latitude, double longitude,
        double avgRating, int numRatings,
        List<ReviewResponse> recentReviews,
        boolean hasMoreReviews,
        List<PhotoResponse> photos) {}

    public record CreateBusinessRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 5000) String description,
        @NotBlank @Size(max = 50) String category,
        @NotBlank @Size(max = 300) String address,
        @Size(max = 30) String phone,
        @Size(max = 300) String website,
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude) {}
}
