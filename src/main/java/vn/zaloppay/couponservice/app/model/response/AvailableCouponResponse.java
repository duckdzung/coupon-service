package vn.zaloppay.couponservice.app.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.zaloppay.couponservice.domain.model.Coupon;
import vn.zaloppay.couponservice.domain.model.discount.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableCouponResponse {

    private String code;
    
    private String title;
    
    private String description;
    
    private DiscountType discountType;
    
    private BigDecimal discountValue;
    
    private BigDecimal maxDiscountAmount;
    
    private BigDecimal minOrderValue;

    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer remainingUsage;
    
    private BigDecimal estimatedDiscountAmount;

    public static AvailableCouponResponse from(Coupon coupon, BigDecimal orderAmount) {
        BigDecimal estimatedDiscount = coupon.calculateDiscount(orderAmount);
        
        return AvailableCouponResponse.builder()
                .code(coupon.getCode())
                .title(coupon.getTitle())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .minOrderValue(coupon.getMinOrderValue())
                .startTime(coupon.getStartTime())
                .endTime(coupon.getEndTime())
                .remainingUsage(coupon.getRemainingUsage())
                .estimatedDiscountAmount(estimatedDiscount)
                .build();
    }

} 