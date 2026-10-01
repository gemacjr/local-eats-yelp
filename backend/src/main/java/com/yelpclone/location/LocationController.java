package com.yelpclone.location;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Named areas users can search by ("Mission District", "New York"). */
@RestController
public class LocationController {

    public record LocationResponse(Long id, String name, String slug, String type) {}

    private final JdbcTemplate jdbc;

    public LocationController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/api/locations")
    public List<LocationResponse> list() {
        return jdbc.query("SELECT id, name, slug, type FROM locations ORDER BY type, name",
            (rs, i) -> new LocationResponse(rs.getLong("id"), rs.getString("name"), rs.getString("slug"), rs.getString("type")));
    }
}
