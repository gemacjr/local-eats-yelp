package com.yelpclone.common;

import java.util.List;
import java.util.function.Function;

/** Offset pagination envelope. hasMore is computed by fetching size + 1 rows, avoiding a COUNT(*). */
public record PageResponse<T>(List<T> items, int page, int size, boolean hasMore) {

    public static <T> PageResponse<T> fromOverfetch(List<T> rows, int page, int size) {
        boolean hasMore = rows.size() > size;
        return new PageResponse<>(hasMore ? rows.subList(0, size) : rows, page, size, hasMore);
    }

    public <R> PageResponse<R> map(Function<T, R> mapper) {
        return new PageResponse<>(items.stream().map(mapper).toList(), page, size, hasMore);
    }
}
