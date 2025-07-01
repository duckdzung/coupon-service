package vn.zaloppay.couponservice.domain.model.discount;

import java.math.BigDecimal;

public class FixedDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal calculateDiscount(BigDecimal orderAmount, BigDecimal discountValue, BigDecimal maxDiscountAmount) {
        return discountValue;
    }
} 