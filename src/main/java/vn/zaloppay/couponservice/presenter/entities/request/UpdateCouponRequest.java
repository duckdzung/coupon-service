package vn.zaloppay.couponservice.presenter.entities.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import vn.zaloppay.couponservice.core.entities.discount.DiscountType;
import vn.zaloppay.couponservice.core.entities.UsageType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateCouponRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Usage type is required")
    private UsageType usageType;

    @DecimalMin(value = "0", message = "Minimum order value must be greater than or equal to 0")
    private BigDecimal minOrderValue;

    @DecimalMin(value = "0", message = "Maximum discount amount must be greater than or equal to 0")
    private BigDecimal maxDiscountAmount;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    private Integer remainingUsage;

} 