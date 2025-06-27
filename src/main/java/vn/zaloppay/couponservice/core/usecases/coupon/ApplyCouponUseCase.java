package vn.zaloppay.couponservice.core.usecases.coupon;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Component;
import vn.zaloppay.couponservice.core.cache.CacheKey;
import vn.zaloppay.couponservice.core.cache.IDistributedLockService;
import vn.zaloppay.couponservice.core.entities.Coupon;
import vn.zaloppay.couponservice.core.exceptions.BadRequestException;
import vn.zaloppay.couponservice.core.exceptions.ResourceNotFoundException;
import vn.zaloppay.couponservice.core.repositories.ICouponRepository;
import vn.zaloppay.couponservice.core.usecases.UseCase;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApplyCouponUseCase extends UseCase<ApplyCouponUseCase.InputValues, ApplyCouponUseCase.OutputValues> {

    private final ICouponRepository couponRepository;
    private final IDistributedLockService distributedLockService;

    // Lock configuration constants
    private static final Duration LOCK_WAIT_TIME = Duration.ofSeconds(3);
    private static final Duration LOCK_LEASE_TIME = Duration.ofSeconds(10);
    
    // Retry configuration constants
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final Duration RETRY_DELAY = Duration.ofMillis(500);

    @Override
    public OutputValues execute(InputValues input) {
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

        // Find the coupon that gives maximum discount
        Coupon bestCoupon = eligibleCoupons.stream()
                .max(Comparator.comparing(coupon -> coupon.calculateDiscount(input.getOrderAmount())))
                .orElseThrow(() -> new BadRequestException("No eligible coupon found"));

        // Apply the best coupon with distributed lock
        return applyCouponWithLock(bestCoupon.getCode(), input);
    }

    private OutputValues applyCouponByCode(InputValues input) {
        return applyCouponWithLock(input.getCouponCode(), input);
    }

    private OutputValues applyCouponWithLock(String couponCode, InputValues input) {
        String lockKey = CacheKey.couponLockKey(couponCode);
        
        return distributedLockService.executeWithLockAndRetry(
            lockKey,
            LOCK_WAIT_TIME,
            LOCK_LEASE_TIME,
            MAX_RETRY_ATTEMPTS,
            RETRY_DELAY,
            () -> executeApplyCoupon(couponCode, input)
        );
    }

    private OutputValues executeApplyCoupon(String couponCode, InputValues input) {
        // Re-fetch coupon to get latest data (important for consistency)
        Coupon coupon = couponRepository.findByCode(couponCode);

        validateCoupon(coupon, couponCode, input);

        // Calculate discount amount
        BigDecimal discountAmount = coupon.calculateDiscount(input.getOrderAmount());

        // Decrement the remaining usage (critical section protected by lock)
        couponRepository.decrementRemainingUsage(coupon.getCode());

        return new OutputValues(discountAmount, coupon);
    }

    private void validateCoupon(Coupon coupon, String couponCode, InputValues input) {
        // Check if coupon exists
        if (coupon == null) {
            throw new ResourceNotFoundException("Cannot find coupon with code: " + couponCode);
        }

        // Check if coupon is expired
        if (coupon.getEndTime().isBefore(input.getCreatedAt())) {
            throw new BadRequestException("Coupon is expired");
        }

        // Check minimum order value
        if (coupon.getMinOrderValue().compareTo(input.getOrderAmount()) > 0) {
            throw new BadRequestException("The minimum order value of the coupon is greater than the order amount");
        }

        // Check remaining usage
        if (coupon.getRemainingUsage() <= 0) {
            throw new BadRequestException("Coupon has no remaining usage");
        }
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
