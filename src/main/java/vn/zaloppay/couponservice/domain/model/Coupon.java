package vn.zaloppay.couponservice.domain.model;

import lombok.Value;
import vn.zaloppay.couponservice.domain.model.discount.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
public class Coupon {

    Long id;

    String code;

    String title;

    String description;

    DiscountType discountType;

    UsageType usageType;

    BigDecimal discountValue;

    BigDecimal maxDiscountAmount;

    BigDecimal minOrderValue;

    LocalDateTime startTime;

    LocalDateTime endTime;

    Integer remainingUsage;

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        return discountType.getStrategy().calculateDiscount(orderAmount, discountValue, maxDiscountAmount);
    }

}
