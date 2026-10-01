package com.yelpclone.business;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * A business listing. {@code geog} and {@code search_vector} exist in the table as Postgres
 * generated columns and are intentionally not mapped here: the DB derives them from lat/lng/name.
 */
@Entity
@Table(name = "businesses")
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String address;

    private String phone;
    private String website;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "avg_rating", nullable = false)
    private double avgRating;

    @Column(name = "num_ratings", nullable = false)
    private int numRatings;

    /** Optimistic lock: concurrent reviews on the same business retry instead of losing an update. */
    @Version
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Business() {}

    public Business(String name, String description, String category, String address,
                    String phone, String website, double latitude, double longitude) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.address = address;
        this.phone = phone;
        this.website = website;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Fold one new star rating into the denormalized aggregates (avgRating, numRatings)
     * without re-reading every review. Called once per new review, inside the same transaction.
     *
     * @param stars an integer rating from 1 to 5 (already validated)
     */
    public void applyRating(int stars) {
        // Incremental mean: uses the count *before* this rating, then bumps it.
        avgRating = (avgRating * numRatings + stars) / (numRatings + 1);
        numRatings++;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getWebsite() { return website; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getAvgRating() { return avgRating; }
    public int getNumRatings() { return numRatings; }
    public Instant getCreatedAt() { return createdAt; }
}
