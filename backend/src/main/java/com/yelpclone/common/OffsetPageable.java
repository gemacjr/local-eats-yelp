package com.yelpclone.common;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * A Pageable with an arbitrary offset/limit. Needed for "fetch size + 1" pagination, since
 * PageRequest.of(page, size + 1) would shift the offset to page * (size + 1).
 */
public record OffsetPageable(long offset, int limit) implements Pageable {

    @Override public int getPageNumber() { return (int) (offset / limit); }
    @Override public int getPageSize() { return limit; }
    @Override public long getOffset() { return offset; }
    @Override public Sort getSort() { return Sort.unsorted(); }
    @Override public Pageable next() { return new OffsetPageable(offset + limit, limit); }
    @Override public Pageable previousOrFirst() { return hasPrevious() ? new OffsetPageable(Math.max(0, offset - limit), limit) : first(); }
    @Override public Pageable first() { return new OffsetPageable(0, limit); }
    @Override public Pageable withPage(int pageNumber) { return new OffsetPageable((long) pageNumber * limit, limit); }
    @Override public boolean hasPrevious() { return offset > 0; }
}
