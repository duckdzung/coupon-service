package vn.zaloppay.couponservice.presenter.entities.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteCouponResponse {

    private String code;
    private String message;

    public static DeleteCouponResponse success(String code) {
        return new DeleteCouponResponse(code, "Coupon deleted successfully");
    }

} 