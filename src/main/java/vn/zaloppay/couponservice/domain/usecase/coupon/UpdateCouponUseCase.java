package vn.zaloppay.couponservice.domain.usecase.coupon;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Component;
import vn.zaloppay.couponservice.domain.model.Coupon;
import vn.zaloppay.couponservice.domain.model.UsageType;
import vn.zaloppay.couponservice.domain.model.discount.DiscountType;
import vn.zaloppay.couponservice.domain.exceptions.BadRequestException;
import vn.zaloppay.couponservice.domain.exceptions.ResourceNotFoundException;
import vn.zaloppay.couponservice.domain.repository.ICouponRepository;
import vn.zaloppay.couponservice.domain.usecase.UseCase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UpdateCouponUseCase extends UseCase<UpdateCouponUseCase.InputValues, UpdateCouponUseCase.OutputValues> {

    private final ICouponRepository couponRepository;

    /**
     * Updates an existing coupon with new specifications.
     * 
     * <p>This method:
     * <ul>
     *   <li>Validates that the coupon exists by code</li>
     *   <li>Preserves the original ID and discount value</li>
     *   <li>Updates all other modifiable fields</li>
     *   <li>Persists the changes to the database</li>
     * </ul>
     * 
     * @param input updated coupon details and the code of the coupon to modify
     * @return OutputValues containing the updated coupon
     * @throws ResourceNotFoundException if no coupon exists with the specified code
     * @throws BadRequestException if invalid update data is provided
     */
    @Override
    public UpdateCouponUseCase.OutputValues execute(UpdateCouponUseCase.InputValues input) {
        Coupon coupon = couponRepository.findByCode(input.getCouponCode());

        if (coupon == null) {
            throw new ResourceNotFoundException("Cannot find coupon with code: " + input.getCouponCode());
        }

        Coupon updatedCoupon = new Coupon(
                coupon.getId(), 
                input.getCouponCode(),
                input.getTitle(),
                input.getDescription(),
                input.getDiscountType(),
                input.getUsageType(),
                coupon.getDiscountValue(),
                input.getMaxDiscountAmount(),
                input.getMinOrderValue(),
                input.getStartTime(),
                input.getEndTime(),
                input.getRemainingUsage()
        );

        return new UpdateCouponUseCase.OutputValues(couponRepository.update(updatedCoupon));
    }


    @Value
    public static class InputValues implements UseCase.InputValues {

        String couponCode;

        String title;

        String description;

        DiscountType discountType;

        UsageType usageType;

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
