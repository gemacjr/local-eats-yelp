package com.yelpclone.business;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    /** Precompute which named areas (cities/neighborhoods) contain this business. */
    @Modifying
    @Query(value = """
        INSERT INTO business_locations (business_id, location_id)
        SELECT b.id, l.id FROM businesses b JOIN locations l ON ST_Covers(l.boundary, b.geog)
        WHERE b.id = :businessId
        ON CONFLICT DO NOTHING
        """, nativeQuery = true)
    int assignLocations(Long businessId);

    @Query("select distinct b.category from Business b order by b.category")
    List<String> findDistinctCategories();
}
