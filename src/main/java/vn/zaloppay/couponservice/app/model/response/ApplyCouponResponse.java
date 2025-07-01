package vn.zaloppay.couponservice.app.model.response;

import lombok.Value;
import vn.zaloppay.couponservice.domain.model.Coupon;

import java.math.BigDecimal;

@Value
public class ApplyCouponResponse {

    BigDecimal discountAmount;
    CouponResponse coupon;

    public static ApplyCouponResponse from(BigDecimal discountAmount, Coupon coupon) {
        return new ApplyCouponResponse(
                discountAmount,
                CouponResponse.from(coupon)
        );
    }

} 