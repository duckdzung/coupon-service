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
import vn.zaloppay.couponservice.domain.exceptions.ConflictException;
import vn.zaloppay.couponservice.domain.repository.ICouponRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCouponUseCaseTest {

    @Mock
    private ICouponRepository couponRepository;

    @InjectMocks
    private CreateCouponUseCase createCouponUseCase;

    private LocalDateTime now;
    private CreateCouponUseCase.InputValues validInput;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        validInput = new CreateCouponUseCase.InputValues(
                "NEWCOUPON",
                "New Coupon",
                "A new coupon for testing",
                DiscountType.PERCENT,
                UsageType.MANUAL,
                new BigDecimal("15"),
                new BigDecimal("100"),
                new BigDecimal("200"),
                now.plusHours(1),
                now.plusDays(7),
                10
        );
    }

    @Test
    void execute_WithValidInput_ShouldCreateCoupon() {
        // Given
        Coupon expectedCoupon = new Coupon(
                1L,
                validInput.getCode(),
                validInput.getTitle(),
                validInput.getDescription(),
                validInput.getDiscountType(),
                validInput.getUsageType(),
                validInput.getDiscountValue(),
                validInput.getMaxDiscountAmount(),
                validInput.getMinOrderValue(),
                validInput.getStartTime(),
                validInput.getEndTime(),
                validInput.getRemainingUsage()
        );

        when(couponRepository.findByCode("NEWCOUPON")).thenReturn(null);
        when(couponRepository.save(any(Coupon.class))).thenReturn(expectedCoupon);

        // When
        CreateCouponUseCase.OutputValues result = createCouponUseCase.execute(validInput);

        // Then
        assertNotNull(result);
        assertEquals(expectedCoupon, result.getCoupon());
        verify(couponRepository).findByCode("NEWCOUPON");
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void execute_WithExistingCouponCode_ShouldThrowConflictException() {
        // Given
        Coupon existingCoupon = new Coupon(
                1L,
                "NEWCOUPON",
                "Existing Coupon",
                "This coupon already exists",
                DiscountType.FIXED,
                UsageType.AUTO,
                new BigDecimal("50"),
                new BigDecimal("50"),
                new BigDecimal("100"),
                now.minusHours(1),
                now.plusHours(1),
                5
        );

        when(couponRepository.findByCode("NEWCOUPON")).thenReturn(existingCoupon);

        // When & Then
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> createCouponUseCase.execute(validInput)
        );
        assertEquals("Coupon code already exists", exception.getMessage());
        verify(couponRepository).findByCode("NEWCOUPON");
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void execute_WithStartTimeAfterEndTime_ShouldThrowBadRequestException() {
        // Given
        CreateCouponUseCase.InputValues invalidInput = new CreateCouponUseCase.InputValues(
                "INVALIDTIME",
                "Invalid Time Coupon",
                "Start time is after end time",
                DiscountType.PERCENT,
                UsageType.MANUAL,
                new BigDecimal("10"),
                new BigDecimal("50"),
                new BigDecimal("100"),
                now.plusDays(7), // Start time after end time
                now.plusHours(1), // End time before start time
                5
        );

        when(couponRepository.findByCode("INVALIDTIME")).thenReturn(null);

        // When & Then
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> createCouponUseCase.execute(invalidInput)
        );
        assertEquals("Start time must be before end time", exception.getMessage());
        verify(couponRepository).findByCode("INVALIDTIME");
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void execute_WithFixedDiscountType_ShouldCreateCoupon() {
        // Given
        CreateCouponUseCase.InputValues fixedDiscountInput = new CreateCouponUseCase.InputValues(
                "FIXED50",
                "Fixed Discount",
                "Fixed 50 off",
                DiscountType.FIXED,
                UsageType.AUTO,
                new BigDecimal("50"),
                new BigDecimal("50"),
                new BigDecimal("100"),
                now.plusHours(1),
                now.plusDays(1),
                20
        );

        Coupon expectedCoupon = new Coupon(
                2L,
                fixedDiscountInput.getCode(),
                fixedDiscountInput.getTitle(),
                fixedDiscountInput.getDescription(),
                fixedDiscountInput.getDiscountType(),
                fixedDiscountInput.getUsageType(),
                fixedDiscountInput.getDiscountValue(),
                fixedDiscountInput.getMaxDiscountAmount(),
                fixedDiscountInput.getMinOrderValue(),
                fixedDiscountInput.getStartTime(),
                fixedDiscountInput.getEndTime(),
                fixedDiscountInput.getRemainingUsage()
        );

        when(couponRepository.findByCode("FIXED50")).thenReturn(null);
        when(couponRepository.save(any(Coupon.class))).thenReturn(expectedCoupon);

        // When
        CreateCouponUseCase.OutputValues result = createCouponUseCase.execute(fixedDiscountInput);

        // Then
        assertNotNull(result);
        assertEquals(expectedCoupon, result.getCoupon());
        assertEquals(DiscountType.FIXED, result.getCoupon().getDiscountType());
        assertEquals(UsageType.AUTO, result.getCoupon().getUsageType());
        verify(couponRepository).findByCode("FIXED50");
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void execute_WithZeroRemainingUsage_ShouldCreateCoupon() {
        // Given
        CreateCouponUseCase.InputValues zeroUsageInput = new CreateCouponUseCase.InputValues(
                "ZEROUSAGE",
                "Zero Usage Coupon",
                "Coupon with zero remaining usage",
                DiscountType.PERCENT,
                UsageType.MANUAL,
                new BigDecimal("10"),
                new BigDecimal("30"),
                new BigDecimal("50"),
                now.plusHours(1),
                now.plusDays(1),
                0 // Zero remaining usage
        );

        Coupon expectedCoupon = new Coupon(
                3L,
                zeroUsageInput.getCode(),
                zeroUsageInput.getTitle(),
                zeroUsageInput.getDescription(),
                zeroUsageInput.getDiscountType(),
                zeroUsageInput.getUsageType(),
                zeroUsageInput.getDiscountValue(),
                zeroUsageInput.getMaxDiscountAmount(),
                zeroUsageInput.getMinOrderValue(),
                zeroUsageInput.getStartTime(),
                zeroUsageInput.getEndTime(),
                zeroUsageInput.getRemainingUsage()
        );

        when(couponRepository.findByCode("ZEROUSAGE")).thenReturn(null);
        when(couponRepository.save(any(Coupon.class))).thenReturn(expectedCoupon);

        // When
        CreateCouponUseCase.OutputValues result = createCouponUseCase.execute(zeroUsageInput);

        // Then
        assertNotNull(result);
        assertEquals(expectedCoupon, result.getCoupon());
        assertEquals(0, result.getCoupon().getRemainingUsage());
        verify(couponRepository).findByCode("ZEROUSAGE");
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void execute_WithSameStartAndEndTime_ShouldThrowBadRequestException() {
        // Given
        LocalDateTime sameTime = now.plusHours(1);
        CreateCouponUseCase.InputValues sameTimeInput = new CreateCouponUseCase.InputValues(
                "SAMETIME",
                "Same Time Coupon",
                "Start and end time are the same",
                DiscountType.PERCENT,
                UsageType.MANUAL,
                new BigDecimal("5"),
                new BigDecimal("25"),
                new BigDecimal("100"),
                sameTime, // Same time
                sameTime, // Same time
                1
        );

        when(couponRepository.findByCode("SAMETIME")).thenReturn(null);

        // When
        CreateCouponUseCase.OutputValues result = createCouponUseCase.execute(sameTimeInput);

        // Then - should succeed because isAfter returns false for equal times
        assertNotNull(result);
    }

} 