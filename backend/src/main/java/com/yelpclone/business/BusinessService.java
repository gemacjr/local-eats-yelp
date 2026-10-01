package com.yelpclone.business;

import com.yelpclone.business.BusinessDtos.*;
import com.yelpclone.common.NotFoundException;
import com.yelpclone.common.PageResponse;
import com.yelpclone.config.AppProperties;
import com.yelpclone.photo.PhotoService;
import com.yelpclone.review.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BusinessService {

    private static final int DETAIL_REVIEW_COUNT = 10;

    private final BusinessRepository businesses;
    private final BusinessSearchRepository searchRepository;
    private final ReviewService reviewService;
    private final PhotoService photoService;
    private final AppProperties props;

    public BusinessService(BusinessRepository businesses, BusinessSearchRepository searchRepository,
                           ReviewService reviewService, PhotoService photoService, AppProperties props) {
        this.businesses = businesses;
        this.searchRepository = searchRepository;
        this.reviewService = reviewService;
        this.photoService = photoService;
        this.props = props;
    }

    @Transactional(readOnly = true)
    public PageResponse<BusinessSummary> search(SearchCriteria raw) {
        SearchCriteria c = normalize(raw);
        return PageResponse.fromOverfetch(searchRepository.search(c), c.page(), c.size());
    }

    @Transactional(readOnly = true)
    public BusinessDetail get(long id) {
        Business b = find(id);
        var reviews = reviewService.list(id, 0, DETAIL_REVIEW_COUNT);
        return new BusinessDetail(
            b.getId(), b.getName(), b.getDescription(), b.getCategory(), b.getAddress(),
            b.getPhone(), b.getWebsite(), b.getLatitude(), b.getLongitude(),
            b.getAvgRating(), b.getNumRatings(),
            reviews.items(), reviews.hasMore(),
            photoService.list(id));
    }

    @Transactional
    public BusinessDetail create(CreateBusinessRequest req) {
        Business b = businesses.saveAndFlush(new Business(
            req.name().trim(), req.description(), req.category().trim(), req.address().trim(),
            req.phone(), req.website(), req.latitude(), req.longitude()));
        businesses.assignLocations(b.getId());
        return new BusinessDetail(
            b.getId(), b.getName(), b.getDescription(), b.getCategory(), b.getAddress(),
            b.getPhone(), b.getWebsite(), b.getLatitude(), b.getLongitude(),
            0, 0, List.of(), false, List.of());
    }

    @Transactional(readOnly = true)
    public List<String> categories() {
        return businesses.findDistinctCategories();
    }

    Business find(long id) {
        return businesses.findById(id).orElseThrow(() -> new NotFoundException("Business " + id + " not found"));
    }

    private SearchCriteria normalize(SearchCriteria c) {
        var cfg = props.search();
        if ((c.latitude() == null) != (c.longitude() == null)) {
            throw new IllegalArgumentException("lat and lng must be provided together");
        }
        double radius = c.radiusMeters() == null ? cfg.defaultRadiusMeters()
            : Math.clamp(c.radiusMeters(), 100, cfg.maxRadiusMeters());
        int size = Math.clamp(c.size(), 1, cfg.maxPageSize());
        int page = Math.max(0, c.page());
        return new SearchCriteria(c.query(), c.category(), c.latitude(), c.longitude(), radius,
            c.locationSlug(), c.sort(), page, size);
    }
}
