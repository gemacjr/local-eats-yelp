package com.yelpclone.business;

import com.yelpclone.business.BusinessDtos.*;
import com.yelpclone.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    /** GET /api/businesses?query=&category=&lat=&lng=&radius=&location=&sort=&page=&size= */
    @GetMapping("/businesses")
    public PageResponse<BusinessSummary> search(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Double lat,
        @RequestParam(required = false) Double lng,
        @RequestParam(required = false) Double radius,
        @RequestParam(required = false) String location,
        @RequestParam(defaultValue = "RELEVANCE") SortOrder sort,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        return businessService.search(new SearchCriteria(query, category, lat, lng, radius, location, sort, page, size));
    }

    @GetMapping("/businesses/{id}")
    public BusinessDetail get(@PathVariable long id) {
        return businessService.get(id);
    }

    @PostMapping("/businesses")
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessDetail create(@Valid @RequestBody CreateBusinessRequest req) {
        return businessService.create(req);
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return businessService.categories();
    }
}
