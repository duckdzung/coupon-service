package vn.zaloppay.couponservice.domain.util;

import org.springframework.data.domain.Sort;

import java.util.function.Function;

public final class SortUtils {

    private SortUtils() {
    }

    public static Sort createSort(String sortBy, String sortDirection, Function<String, String> validator) {
        Sort.Direction direction = parseSortDirection(sortDirection);
        String field = validator.apply(sortBy);
        return Sort.by(direction, field);
    }


    private static Sort.Direction parseSortDirection(String sortDirection) {
        if (sortDirection == null) return Sort.Direction.ASC;
        try {
            return Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException e) {
            return Sort.Direction.ASC;
        }
    }
} 