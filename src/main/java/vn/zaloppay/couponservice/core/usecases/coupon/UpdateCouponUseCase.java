package vn.zaloppay.couponservice.core.usecases.coupon;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Component;
import vn.zaloppay.couponservice.core.entities.Coupon;
import vn.zaloppay.couponservice.core.entities.discount.DiscountType;
import vn.zaloppay.couponservice.core.entities.UsageType;
import vn.zaloppay.couponservice.core.exceptions.ResourceNotFoundException;
import vn.zaloppay.couponservice.core.repositories.ICouponRepository;
import vn.zaloppay.couponservice.core.usecases.UseCase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@AllArgsConstructor
public class UpdateCouponUseCase extends UseCase<UpdateCouponUseCase.InputValues, UpdateCouponUseCase.OutputValues> {

    private final ICouponRepository couponRepository;

    @Override
    public UpdateCouponUseCase.OutputValues execute(UpdateCouponUseCase.InputValues input) {
        Coupon coupon = couponRepository.findByCode(input.getCouponCode());

        if (coupon == null) {
            throw new ResourceNotFoundException("Cannot find coupon with code: " + input.getCouponCode());
        }

        Coupon updatedCoupon = new Coupon(
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
