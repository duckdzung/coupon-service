package vn.zaloppay.couponservice.core.entities;

import lombok.Getter;

@Getter
public enum CouponSortField {
    DISCOUNT_VALUE("discountValue"),
    MAX_DISCOUNT("maxDiscountAmount"),
    END_TIME("endTime"),
    TITLE("title");

    private final String field;
    CouponSortField(String field) { this.field = field; }

    public static String from(String value) {
        for (CouponSortField f : values()) {
            if (f.field.equalsIgnoreCase(value)) return f.field;
        }
        return DISCOUNT_VALUE.field;
    }
}

