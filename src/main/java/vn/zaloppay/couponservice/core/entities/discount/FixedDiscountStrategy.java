package vn.zaloppay.couponservice.core.entities.discount;

import java.math.BigDecimal;

public class FixedDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal calculateDiscount(BigDecimal orderAmount, BigDecimal discountValue, BigDecimal maxDiscountAmount) {
        return discountValue;
    }
} 