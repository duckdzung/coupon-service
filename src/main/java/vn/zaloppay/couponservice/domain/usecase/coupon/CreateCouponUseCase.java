package vn.zaloppay.couponservice.domain.usecase.coupon;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Component;
import vn.zaloppay.couponservice.domain.model.Coupon;
import vn.zaloppay.couponservice.domain.model.UsageType;
import vn.zaloppay.couponservice.domain.model.discount.DiscountType;
import vn.zaloppay.couponservice.domain.exceptions.BadRequestException;
import vn.zaloppay.couponservice.domain.exceptions.ConflictException;
import vn.zaloppay.couponservice.domain.repository.ICouponRepository;
import vn.zaloppay.couponservice.domain.usecase.UseCase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CreateCouponUseCase extends UseCase<CreateCouponUseCase.InputValues, CreateCouponUseCase.OutputValues> {

    private final ICouponRepository couponRepository;

    /**
     * Creates a new coupon in the system with the provided specifications.
     * 
     * <p>This method performs the following validations and operations:
     * <ul>
     *   <li>Validates that the coupon code is unique (not already in use)</li>
     *   <li>Validates that the start time is before the end time</li>
     *   <li>Creates a new coupon with all provided attributes</li>
     *   <li>Persists the coupon to the database</li>
     * </ul>
     * 
     * @param input coupon creation details including code, title, description, discount configuration,
     *              time validity, and usage limits
     * @return OutputValues containing the newly created coupon with generated metadata
     * @throws ConflictException if a coupon with the same code already exists
     * @throws BadRequestException if start time is after end time, or other validation failures
     */
    @Override
    public OutputValues execute(InputValues input) {
        // Check if a coupon exists, throw an error
        if (couponRepository.findByCode(input.getCode()) != null) {
            throw new ConflictException("Coupon code already exists");
        }

        // Check if start time is after end time, throw an error
        if (input.getStartTime().isAfter(input.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        // If all condition is satisfied, create a new coupon
        Coupon coupon = new Coupon(
                null,
                input.getCode(),
                input.getTitle(),
                input.getDescription(),
                input.getDiscountType(),
                input.getUsageType(),
                input.getDiscountValue(),
                input.getMaxDiscountAmount(),
                input.getMinOrderValue(),
                input.getStartTime(),
                input.getEndTime(),
                input.getRemainingUsage()
        );

        return new OutputValues(couponRepository.save(coupon));
    }

    @Value
    public static class InputValues implements UseCase.InputValues {

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

    }

    @Value
    public static class OutputValues implements UseCase.OutputValues {
        Coupon coupon;
    }

}
