package com.yelpclone.photo;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "business_photos")
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "s3_key", nullable = false, unique = true)
    private String s3Key;

    private String caption;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Photo() {}

    public Photo(Long businessId, Long userId, String s3Key, String caption) {
        this.businessId = businessId;
        this.userId = userId;
        this.s3Key = s3Key;
        this.caption = caption;
    }

    public Long getId() { return id; }
    public Long getBusinessId() { return businessId; }
    public Long getUserId() { return userId; }
    public String getS3Key() { return s3Key; }
    public String getCaption() { return caption; }
    public Instant getCreatedAt() { return createdAt; }
}
