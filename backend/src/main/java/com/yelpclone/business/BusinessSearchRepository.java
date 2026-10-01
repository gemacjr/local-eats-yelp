package com.yelpclone.business;

import com.yelpclone.business.BusinessDtos.BusinessSummary;
import com.yelpclone.business.BusinessDtos.SearchCriteria;
import com.yelpclone.business.BusinessDtos.SortOrder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Search is a dynamic query over PostGIS + full-text indexes, so it is built as SQL rather than JPA.
 * Every predicate here is backed by an index created in V1__schema.sql:
 *   ST_DWithin      -> GIST(geog)
 *   @@ tsquery      -> GIN(search_vector)
 *   name ILIKE      -> GIN(name gin_trgm_ops)
 *   location slug   -> business_locations PK (precomputed polygon membership)
 */
@Repository
public class BusinessSearchRepository {

    private static final String POINT = "ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography";

    private final NamedParameterJdbcTemplate jdbc;

    public BusinessSearchRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<BusinessSummary> search(SearchCriteria c) {
        var params = new MapSqlParameterSource();
        var where = new ArrayList<String>();
        var joins = new StringBuilder();

        String distanceExpr = "NULL::double precision";
        if (c.hasPoint()) {
            params.addValue("lat", c.latitude()).addValue("lng", c.longitude()).addValue("radius", c.radiusMeters());
            distanceExpr = "ST_Distance(b.geog, " + POINT + ")";
            where.add("ST_DWithin(b.geog, " + POINT + ", :radius)");
        }

        String rankExpr = "0";
        if (c.hasQuery()) {
            params.addValue("q", c.query().trim()).addValue("qLike", "%" + escapeLike(c.query().trim()) + "%");
            // Full-text match for words ("pizza napoletana") OR substring match for partial names ("taq").
            where.add("(b.search_vector @@ websearch_to_tsquery('english', :q) OR b.name ILIKE :qLike)");
            rankExpr = "ts_rank(b.search_vector, websearch_to_tsquery('english', :q))";
        }

        if (c.category() != null && !c.category().isBlank()) {
            params.addValue("category", c.category());
            where.add("b.category = :category");
        }

        if (c.locationSlug() != null && !c.locationSlug().isBlank()) {
            params.addValue("slug", c.locationSlug());
            joins.append(" JOIN business_locations bl ON bl.business_id = b.id")
                 .append(" JOIN locations l ON l.id = bl.location_id AND l.slug = :slug");
        }

        params.addValue("limit", c.size() + 1).addValue("offset", c.page() * c.size());

        String sql = """
            SELECT b.id, b.name, b.category, b.address, b.latitude, b.longitude,
                   b.avg_rating, b.num_ratings, %s AS distance_m
            FROM businesses b%s
            %s
            ORDER BY %s
            LIMIT :limit OFFSET :offset
            """.formatted(
                distanceExpr,
                joins,
                where.isEmpty() ? "" : "WHERE " + String.join(" AND ", where),
                orderBy(c, rankExpr, distanceExpr));

        return jdbc.query(sql, params, (rs, i) -> new BusinessSummary(
            rs.getLong("id"), rs.getString("name"), rs.getString("category"), rs.getString("address"),
            rs.getDouble("latitude"), rs.getDouble("longitude"),
            rs.getDouble("avg_rating"), rs.getInt("num_ratings"),
            (Double) rs.getObject("distance_m")));
    }

    private static String orderBy(SearchCriteria c, String rankExpr, String distanceExpr) {
        SortOrder sort = c.sort() == null ? SortOrder.RELEVANCE : c.sort();
        String byRating = "b.avg_rating DESC, b.num_ratings DESC";
        return switch (sort) {
            case RATING -> byRating + ", b.id";
            case DISTANCE -> c.hasPoint() ? distanceExpr + ", b.id" : byRating + ", b.id";
            case RELEVANCE -> {
                if (c.hasQuery()) yield rankExpr + " DESC, " + byRating + ", b.id";
                if (c.hasPoint()) yield distanceExpr + ", b.id";
                yield byRating + ", b.id";
            }
        };
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
