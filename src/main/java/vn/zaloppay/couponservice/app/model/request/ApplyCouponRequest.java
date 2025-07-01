package vn.zaloppay.couponservice.app.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplyCouponRequest {

    @NotNull(message = "Order amount is required")
    @DecimalMin(value = "1.0", inclusive = true, message = "Order amount must be at least 1")
    private BigDecimal orderAmount;

    private String couponCode;

} 