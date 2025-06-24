package vn.zaloppay.couponservice.presenter.entities.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.zaloppay.couponservice.core.entities.Coupon;
import vn.zaloppay.couponservice.core.entities.discount.DiscountType;

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
                .endTime(coupon.getEndTime())
                .remainingUsage(coupon.getRemainingUsage())
                .estimatedDiscountAmount(estimatedDiscount)
                .build();
    }

} 