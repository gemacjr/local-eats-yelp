package com.yelpclone.review;

import com.yelpclone.auth.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private short rating;

    private String text;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Review() {}

    public Review(Long businessId, User author, int rating, String text) {
        this.businessId = businessId;
        this.author = author;
        this.rating = (short) rating;
        this.text = text;
    }

    public Long getId() { return id; }
    public Long getBusinessId() { return businessId; }
    public User getAuthor() { return author; }
    public int getRating() { return rating; }
    public String getText() { return text; }
    public Instant getCreatedAt() { return createdAt; }
}
