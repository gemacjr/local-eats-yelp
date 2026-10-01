package com.yelpclone.business;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class BusinessRatingTest {

    private Business newBusiness() {
        return new Business("Test", null, "Coffee", "1 Main St", null, null, 37.77, -122.41);
    }

    @Test
    void firstRatingBecomesTheAverage() {
        Business b = newBusiness();
        b.applyRating(4);
        assertThat(b.getNumRatings()).isEqualTo(1);
        assertThat(b.getAvgRating()).isEqualTo(4.0);
    }

    @Test
    void incrementalAverageMatchesArithmeticMean() {
        Business b = newBusiness();
        int[] ratings = {5, 3, 4, 1, 5, 2, 5};
        for (int r : ratings) b.applyRating(r);
        assertThat(b.getNumRatings()).isEqualTo(ratings.length);
        assertThat(b.getAvgRating()).isCloseTo(25.0 / 7, within(1e-9));
    }
}
