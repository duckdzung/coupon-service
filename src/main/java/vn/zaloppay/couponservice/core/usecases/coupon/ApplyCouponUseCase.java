package vn.zaloppay.couponservice.core.usecases.coupon;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Component;
import vn.zaloppay.couponservice.core.entities.Coupon;
import vn.zaloppay.couponservice.core.exceptions.BadRequestException;
import vn.zaloppay.couponservice.core.exceptions.ResourceNotFoundException;
import vn.zaloppay.couponservice.core.repositories.ICouponRepository;
import vn.zaloppay.couponservice.core.usecases.UseCase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApplyCouponUseCase extends UseCase<ApplyCouponUseCase.InputValues, ApplyCouponUseCase.OutputValues> {

    private final ICouponRepository couponRepository;

    @Override
    public ApplyCouponUseCase.OutputValues execute(ApplyCouponUseCase.InputValues input) {
        if (input.getCouponCode() == null) {
            return findAndApplyBestCoupon(input);
        }
        
        return applyCouponByCode(input);
    }

    private OutputValues findAndApplyBestCoupon(InputValues input) {
        List<Coupon> eligibleCoupons = couponRepository.findEligibleCoupons(
                input.getOrderAmount(), 
                input.getCreatedAt()
        );

        if (eligibleCoupons.isEmpty()) {
            return null; // No eligible coupons found
        }

        // Find the coupon that gives maximum discount
        Coupon bestCoupon = eligibleCoupons.stream()
                .max(Comparator.comparing(coupon -> coupon.calculateDiscount(input.getOrderAmount()))).get();
        BigDecimal discountAmount = bestCoupon.calculateDiscount(input.getOrderAmount());

        // Decrement the remaining usage for this coupon
        couponRepository.decrementRemainingUsage(bestCoupon.getCode());

        return new OutputValues(discountAmount, bestCoupon);
    }

    private OutputValues applyCouponByCode(InputValues input) {
        Coupon coupon = couponRepository.findByCode(input.getCouponCode());

        // Check if a coupon does not exist, throw an error
        if (coupon == null) {
            throw new ResourceNotFoundException("Cannot find coupon with code: " + input.getCouponCode());
        }

        // Check if a coupon is expired, throw an error
        if (coupon.getEndTime().isBefore(input.getCreatedAt())) {
            throw new BadRequestException("Coupon is expired");
        }

        // Check if the min order value is greater than the order amount, throw an error
        if (coupon.getMinOrderValue().compareTo(input.getOrderAmount()) > 0) {
            throw new BadRequestException("The minimum order value of the coupon is greater than the order amount");
        }

        // Check if the coupon has remaining usage
        if (coupon.getRemainingUsage() <= 0) {
            throw new BadRequestException("Coupon has no remaining usage");
        }

        // If all condition is satisfied, calculate discount amount for order
        BigDecimal discountAmount = coupon.calculateDiscount(input.getOrderAmount());

        // Decrement the remaining usage for this coupon
        couponRepository.decrementRemainingUsage(coupon.getCode());

        return new OutputValues(discountAmount, coupon);
    }

    @Value
    public static class InputValues implements UseCase.InputValues {
        BigDecimal orderAmount;
        LocalDateTime createdAt;
        String couponCode;
    }

    @Value
    public static class OutputValues implements UseCase.OutputValues {
        BigDecimal discountAmount;
        Coupon coupon;
    }

}
