package vn.zaloppay.couponservice.domain.usecase.coupon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.zaloppay.couponservice.domain.model.Coupon;
import vn.zaloppay.couponservice.domain.model.UsageType;
import vn.zaloppay.couponservice.domain.model.discount.DiscountType;
import vn.zaloppay.couponservice.domain.exceptions.BadRequestException;
import vn.zaloppay.couponservice.domain.exceptions.ResourceNotFoundException;
import vn.zaloppay.couponservice.domain.repository.ICouponRepository;
import vn.zaloppay.couponservice.domain.service.IDistributedLockService;
import vn.zaloppay.couponservice.domain.util.CacheKey;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplyCouponUseCaseTest {

    @Mock
    private ICouponRepository couponRepository;

    @Mock
    private IDistributedLockService distributedLockService;

    @InjectMocks
    private ApplyCouponUseCase applyCouponUseCase;

    private Coupon validCoupon;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        validCoupon = new Coupon(
                1L,
                "DISCOUNT10",
                "10% Discount",
                "Get 10% off your order",
                DiscountType.PERCENT,
                UsageType.MANUAL,
                new BigDecimal("10"),
                new BigDecimal("50"),
                new BigDecimal("100"),
                now.minusHours(1),
                now.plusHours(1),
                5
        );
    }

    @Test
    void execute_WithSpecificCouponCode_ShouldApplyCoupon() {
        // Given
        ApplyCouponUseCase.InputValues input = new ApplyCouponUseCase.InputValues(
                new BigDecimal("200"),
                now,
                "DISCOUNT10"
        );

        String lockKey = CacheKey.couponLockKey("DISCOUNT10");

        when(distributedLockService.executeWithLockAndRetry(
                eq(lockKey),
                any(Duration.class),
                any(Duration.class),
                anyInt(),
                any(Duration.class),
                any(Supplier.class)
        )).thenAnswer(invocation -> {
            Supplier<ApplyCouponUseCase.OutputValues> supplier = invocation.getArgument(5);
            when(couponRepository.findByCode("DISCOUNT10")).thenReturn(validCoupon);
            return supplier.get();
        });

        // When
        ApplyCouponUseCase.OutputValues result = applyCouponUseCase.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(0, new BigDecimal("20").compareTo(result.getDiscountAmount()));
        assertEquals("DISCOUNT10", result.getCoupon().getCode());
        verify(couponRepository).decrementRemainingUsage("DISCOUNT10");
    }

    @Test
    void execute_WithoutCouponCode_ShouldFindBestCoupon() {
        // Given
        ApplyCouponUseCase.InputValues input = new ApplyCouponUseCase.InputValues(
                new BigDecimal("200"),
                now,
                null
        );

        Coupon lesserCoupon = new Coupon(
                2L,
                "DISCOUNT5",
                "5% Discount",
                "Get 5% off",
                DiscountType.PERCENT,
                UsageType.AUTO,
                new BigDecimal("5"),
                new BigDecimal("25"),
                new BigDecimal("100"),
                now.minusHours(1),
                now.plusHours(1),
                10
        );

        List<Coupon> eligibleCoupons = Arrays.asList(validCoupon, lesserCoupon);
        String lockKey = CacheKey.couponLockKey("DISCOUNT10");

        when(couponRepository.findEligibleCoupons(new BigDecimal("200"), now))
                .thenReturn(eligibleCoupons);

        when(distributedLockService.executeWithLockAndRetry(
                eq(lockKey),
                any(Duration.class),
                any(Duration.class),
                anyInt(),
                any(Duration.class),
                any(Supplier.class)
        )).thenAnswer(invocation -> {
            Supplier<ApplyCouponUseCase.OutputValues> supplier = invocation.getArgument(5);
            when(couponRepository.findByCode("DISCOUNT10")).thenReturn(validCoupon);
            return supplier.get();
        });

        // When
        ApplyCouponUseCase.OutputValues result = applyCouponUseCase.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(0, new BigDecimal("20").compareTo(result.getDiscountAmount()));
        assertEquals("DISCOUNT10", result.getCoupon().getCode());
        verify(couponRepository).decrementRemainingUsage("DISCOUNT10");
    }

    @Test
    void execute_WithoutCouponCodeAndNoEligibleCoupons_ShouldReturnNull() {
        // Given
        ApplyCouponUseCase.InputValues input = new ApplyCouponUseCase.InputValues(
                new BigDecimal("50"),
                now,
                null
        );

        when(couponRepository.findEligibleCoupons(new BigDecimal("50"), now))
                .thenReturn(Collections.emptyList());

        // When
        ApplyCouponUseCase.OutputValues result = applyCouponUseCase.execute(input);

        // Then
        assertNull(result);
        verify(distributedLockService, never()).executeWithLockAndRetry(anyString(), any(), any(), anyInt(), any(), any());
    }

    @Test
    void execute_WithInvalidCoupon_ShouldThrowResourceNotFoundException() {
        // Given
        ApplyCouponUseCase.InputValues input = new ApplyCouponUseCase.InputValues(
                new BigDecimal("200"),
                now,
                "INVALID"
        );

        String lockKey = CacheKey.couponLockKey("INVALID");

        when(distributedLockService.executeWithLockAndRetry(
                eq(lockKey),
                any(Duration.class),
                any(Duration.class),
                anyInt(),
                any(Duration.class),
                any(Supplier.class)
        )).thenAnswer(invocation -> {
            Supplier<ApplyCouponUseCase.OutputValues> supplier = invocation.getArgument(5);
            when(couponRepository.findByCode("INVALID")).thenReturn(null);
            return supplier.get();
        });

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> applyCouponUseCase.execute(input)
        );
        assertEquals("Cannot find coupon with code: INVALID", exception.getMessage());
    }

    @Test
    void execute_WithExpiredCoupon_ShouldThrowBadRequestException() {
        // Given
        Coupon expiredCoupon = new Coupon(
                3L,
                "EXPIRED",
                "Expired Coupon",
                "This coupon is expired",
                DiscountType.PERCENT,
                UsageType.MANUAL,
                new BigDecimal("10"),
                new BigDecimal("50"),
                new BigDecimal("100"),
                now.minusHours(2),
                now.minusHours(1), // Expired
                5
        );

        ApplyCouponUseCase.InputValues input = new ApplyCouponUseCase.InputValues(
                new BigDecimal("200"),
                now,
                "EXPIRED"
        );

        String lockKey = CacheKey.couponLockKey("EXPIRED");

        when(distributedLockService.executeWithLockAndRetry(
                eq(lockKey),
                any(Duration.class),
                any(Duration.class),
                anyInt(),
                any(Duration.class),
                any(Supplier.class)
        )).thenAnswer(invocation -> {
            Supplier<ApplyCouponUseCase.OutputValues> supplier = invocation.getArgument(5);
            when(couponRepository.findByCode("EXPIRED")).thenReturn(expiredCoupon);
            return supplier.get();
        });

        // When & Then
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> applyCouponUseCase.execute(input)
        );
        assertEquals("Coupon is expired", exception.getMessage());
    }

    @Test
    void execute_WithOrderAmountBelowMinimum_ShouldThrowBadRequestException() {
        // Given
        ApplyCouponUseCase.InputValues input = new ApplyCouponUseCase.InputValues(
                new BigDecimal("50"), // Below minimum of 100
                now,
                "DISCOUNT10"
        );

        String lockKey = CacheKey.couponLockKey("DISCOUNT10");

        when(distributedLockService.executeWithLockAndRetry(
                eq(lockKey),
                any(Duration.class),
                any(Duration.class),
                anyInt(),
                any(Duration.class),
                any(Supplier.class)
        )).thenAnswer(invocation -> {
            Supplier<ApplyCouponUseCase.OutputValues> supplier = invocation.getArgument(5);
            when(couponRepository.findByCode("DISCOUNT10")).thenReturn(validCoupon);
            return supplier.get();
        });

        // When & Then
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> applyCouponUseCase.execute(input)
        );
        assertEquals("The minimum order value of the coupon is greater than the order amount", exception.getMessage());
    }

    @Test
    void execute_WithNoRemainingUsage_ShouldThrowBadRequestException() {
        // Given
        Coupon exhaustedCoupon = new Coupon(
                4L,
                "EXHAUSTED",
                "Exhausted Coupon",
                "No remaining usage",
                DiscountType.PERCENT,
                UsageType.MANUAL,
                new BigDecimal("10"),
                new BigDecimal("50"),
                new BigDecimal("100"),
                now.minusHours(1),
                now.plusHours(1),
                0 // No remaining usage
        );

        ApplyCouponUseCase.InputValues input = new ApplyCouponUseCase.InputValues(
                new BigDecimal("200"),
                now,
                "EXHAUSTED"
        );

        String lockKey = CacheKey.couponLockKey("EXHAUSTED");

        when(distributedLockService.executeWithLockAndRetry(
                eq(lockKey),
                any(Duration.class),
                any(Duration.class),
                anyInt(),
                any(Duration.class),
                any(Supplier.class)
        )).thenAnswer(invocation -> {
            Supplier<ApplyCouponUseCase.OutputValues> supplier = invocation.getArgument(5);
            when(couponRepository.findByCode("EXHAUSTED")).thenReturn(exhaustedCoupon);
            return supplier.get();
        });

        // When & Then
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> applyCouponUseCase.execute(input)
        );
        assertEquals("Coupon has no remaining usage", exception.getMessage());
    }

    @Test
    void execute_WithDiscountCapping_ShouldCapAtMaximum() {
        // Given - Test both discount calculation and capping
        Coupon cappedCoupon = new Coupon(
                5L,
                "CAPPED20",
                "Capped 20% Discount",
                "20% off but max 30",
                DiscountType.PERCENT,
                UsageType.MANUAL,
                new BigDecimal("20"),
                new BigDecimal("30"), // Max cap of 30
                new BigDecimal("100"),
                now.minusHours(1),
                now.plusHours(1),
                5
        );

        ApplyCouponUseCase.InputValues input = new ApplyCouponUseCase.InputValues(
                new BigDecimal("200"), // 20% of 200 = 40, but capped at 30
                now,
                "CAPPED20"
        );

        String lockKey = CacheKey.couponLockKey("CAPPED20");

        when(distributedLockService.executeWithLockAndRetry(
                eq(lockKey),
                any(Duration.class),
                any(Duration.class),
                anyInt(),
                any(Duration.class),
                any(Supplier.class)
        )).thenAnswer(invocation -> {
            Supplier<ApplyCouponUseCase.OutputValues> supplier = invocation.getArgument(5);
            when(couponRepository.findByCode("CAPPED20")).thenReturn(cappedCoupon);
            return supplier.get();
        });

        // When
        ApplyCouponUseCase.OutputValues result = applyCouponUseCase.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(0, new BigDecimal("30").compareTo(result.getDiscountAmount())); // Capped at 30
        verify(couponRepository).decrementRemainingUsage("CAPPED20");
    }

    @Test
    void execute_WithFixedDiscount_ShouldReturnFixedAmount() {
        // Given - Test fixed discount type
        Coupon fixedCoupon = new Coupon(
                6L,
                "FIXED50",
                "Fixed 50 Off",
                "Get 50 off",
                DiscountType.FIXED,
                UsageType.MANUAL,
                new BigDecimal("50"),
                new BigDecimal("50"),
                new BigDecimal("100"),
                now.minusHours(1),
                now.plusHours(1),
                5
        );

        ApplyCouponUseCase.InputValues input = new ApplyCouponUseCase.InputValues(
                new BigDecimal("200"),
                now,
                "FIXED50"
        );

        String lockKey = CacheKey.couponLockKey("FIXED50");

        when(distributedLockService.executeWithLockAndRetry(
                eq(lockKey),
                any(Duration.class),
                any(Duration.class),
                anyInt(),
                any(Duration.class),
                any(Supplier.class)
        )).thenAnswer(invocation -> {
            Supplier<ApplyCouponUseCase.OutputValues> supplier = invocation.getArgument(5);
            when(couponRepository.findByCode("FIXED50")).thenReturn(fixedCoupon);
            return supplier.get();
        });

        // When
        ApplyCouponUseCase.OutputValues result = applyCouponUseCase.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(0, new BigDecimal("50").compareTo(result.getDiscountAmount()));
        verify(couponRepository).decrementRemainingUsage("FIXED50");
    }

    @Test
    void execute_WithValidationFailure_ShouldNotDecrementUsage() {
        // Given
        ApplyCouponUseCase.InputValues input = new ApplyCouponUseCase.InputValues(
                new BigDecimal("50"), // Below minimum
                now,
                "DISCOUNT10"
        );

        String lockKey = CacheKey.couponLockKey("DISCOUNT10");

        when(distributedLockService.executeWithLockAndRetry(
                eq(lockKey),
                any(Duration.class),
                any(Duration.class),
                anyInt(),
                any(Duration.class),
                any(Supplier.class)
        )).thenAnswer(invocation -> {
            Supplier<ApplyCouponUseCase.OutputValues> supplier = invocation.getArgument(5);
            when(couponRepository.findByCode("DISCOUNT10")).thenReturn(validCoupon);
            return supplier.get();
        });

        // When & Then
        assertThrows(BadRequestException.class, () -> applyCouponUseCase.execute(input));

        // Verify usage was NOT decremented due to validation failure
        verify(couponRepository, never()).decrementRemainingUsage("DISCOUNT10");
    }

} 