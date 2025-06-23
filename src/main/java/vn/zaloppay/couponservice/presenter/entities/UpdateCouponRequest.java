package vn.zaloppay.couponservice.presenter.entities;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import vn.zaloppay.couponservice.core.entities.discount.DiscountType;
import vn.zaloppay.couponservice.core.entities.UsageType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateCouponRequest {

    @NotBlank(message = "Coupon code must not be blank")
    @Length(min = 3, max = 10, message = "Coupon code must be between 3 and 10 characters")
    private String code;

    @NotBlank(message = "Title must not be blank")
    private String title;

    @NotBlank(message = "Description must not be blank")
    private String description;

    @NotNull(message = "Discount type must not be null")
    private DiscountType discountType;

    @NotNull(message = "Usage type must not be null")
    private UsageType usageType;

    @NotNull(message = "Max discount amount is required")
    @DecimalMin(value = "1.0", inclusive = true, message = "Max discount amount must be at least 1")
    private BigDecimal maxDiscountAmount;

    @NotNull(message = "Min order value is required")
    @DecimalMin(value = "1.0", inclusive = true, message = "Min order value must be at least 1")
    private BigDecimal minOrderValue;

    @NotNull(message = "Start time must not be null")
    private LocalDateTime startTime;

    @NotNull(message = "End time must not be null")
    private LocalDateTime endTime;

    @Min(value = 0, message = "Usage count must be non-negative")
    private Integer remainingUsage;
    
}
