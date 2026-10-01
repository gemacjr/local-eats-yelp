package com.yelpclone.review;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = "author")
    List<Review> findByBusinessIdOrderByCreatedAtDescIdDesc(Long businessId, Pageable pageable);

    boolean existsByBusinessIdAndAuthorId(Long businessId, Long userId);
}
