package vn.zaloppay.couponservice.domain.model.discount;

import java.math.BigDecimal;

public interface DiscountStrategy {
    BigDecimal calculateDiscount(BigDecimal orderAmount, BigDecimal discountValue, BigDecimal maxDiscountAmount);
} 