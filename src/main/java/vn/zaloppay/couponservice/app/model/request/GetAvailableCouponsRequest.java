package vn.zaloppay.couponservice.app.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.zaloppay.couponservice.domain.model.discount.DiscountType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAvailableCouponsRequest {

    @DecimalMin(value = "0", message = "Order amount must be greater than or equal to 0")
    private BigDecimal orderAmount;

    private DiscountType discountType;

    @Min(value = 0, message = "Page number must be greater than or equal to 0")
    @Builder.Default
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be greater than 0")
    @Builder.Default
    private Integer size = 10;

    @Builder.Default
    private String sortBy = "discountValue";

    @Builder.Default
    private String sortDirection = "DESC";

} 