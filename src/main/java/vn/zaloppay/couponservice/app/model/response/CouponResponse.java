package vn.zaloppay.couponservice.app.model.response;

import lombok.Value;
import vn.zaloppay.couponservice.domain.model.Coupon;
import vn.zaloppay.couponservice.domain.model.discount.DiscountType;
import vn.zaloppay.couponservice.domain.model.UsageType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Value
public class CouponResponse {

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

    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getCode(),
                coupon.getTitle(),
                coupon.getDescription(),
                coupon.getDiscountType(),
                coupon.getUsageType(),
                coupon.getDiscountValue(),
                coupon.getMaxDiscountAmount(),
                coupon.getMinOrderValue(),
                coupon.getStartTime(),
                coupon.getEndTime(),
                coupon.getRemainingUsage()
        );
    }

    public static List<CouponResponse> from(List<Coupon> coupons) {
        return coupons.stream()
                .map(CouponResponse::from)
                .toList();
    }

}
