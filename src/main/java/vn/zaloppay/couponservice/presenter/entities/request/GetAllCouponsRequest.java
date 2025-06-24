package vn.zaloppay.couponservice.presenter.entities.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.zaloppay.couponservice.core.entities.discount.DiscountType;
import vn.zaloppay.couponservice.core.entities.UsageType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAllCouponsRequest {

    private DiscountType discountType;

    private UsageType usageType;

    @Min(value = 0, message = "Page number must be greater than or equal to 0")
    @Builder.Default
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be greater than 0")
    @Builder.Default
    private Integer size = 10;

    @Builder.Default
    private String sortBy = "startTime";

    @Builder.Default
    private String sortDirection = "DESC";

}
