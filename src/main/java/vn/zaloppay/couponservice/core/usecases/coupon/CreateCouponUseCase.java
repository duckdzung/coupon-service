package vn.zaloppay.couponservice.core.usecases.coupon;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Component;
import vn.zaloppay.couponservice.core.entities.Coupon;
import vn.zaloppay.couponservice.core.entities.discount.DiscountType;
import vn.zaloppay.couponservice.core.entities.UsageType;
import vn.zaloppay.couponservice.core.exceptions.BadRequestException;
import vn.zaloppay.couponservice.core.exceptions.ConflictException;
import vn.zaloppay.couponservice.core.repositories.ICouponRepository;
import vn.zaloppay.couponservice.core.usecases.UseCase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@AllArgsConstructor
public class CreateCouponUseCase extends UseCase<CreateCouponUseCase.InputValues, CreateCouponUseCase.OutputValues> {

    private ICouponRepository couponRepository;

    @Override
    public OutputValues execute(InputValues input) {
        // Check if a coupon does not exist, throw an error
        if (couponRepository.findByCode(input.getCode()) != null) {
            throw new ConflictException("Coupon code already exists");
        }

        // Check if start time is after end time, throw an error
        if (input.getStartTime().isAfter(input.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        // If all condition is satisfied, create a new coupon
        Coupon coupon = new Coupon(
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
