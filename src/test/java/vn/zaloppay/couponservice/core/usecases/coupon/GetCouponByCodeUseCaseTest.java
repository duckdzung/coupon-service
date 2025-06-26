package vn.zaloppay.couponservice.core.usecases.coupon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.zaloppay.couponservice.core.entities.Coupon;
import vn.zaloppay.couponservice.core.entities.UsageType;
import vn.zaloppay.couponservice.core.entities.discount.DiscountType;
import vn.zaloppay.couponservice.core.exceptions.ResourceNotFoundException;
import vn.zaloppay.couponservice.core.repositories.ICouponRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCouponByCodeUseCaseTest {

    @Mock
    private ICouponRepository couponRepository;

    @InjectMocks
    private GetCouponByCodeUseCase getCouponByCodeUseCase;

    private Coupon testCoupon;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        testCoupon = new Coupon(
                "TESTCODE123",
                "Test Coupon",
                "A test coupon for unit testing",
                DiscountType.PERCENT,
                UsageType.MANUAL,
                new BigDecimal("15"),
                new BigDecimal("75"),
                new BigDecimal("150"),
                now.minusHours(1),
                now.plusHours(1),
                8
        );
    }

    @Test
    void execute_WithValidCouponCode_ShouldReturnCoupon() {
        // Given
        GetCouponByCodeUseCase.InputValues input = new GetCouponByCodeUseCase.InputValues("TESTCODE123");

        when(couponRepository.findByCode("TESTCODE123")).thenReturn(testCoupon);

        // When
        GetCouponByCodeUseCase.OutputValues result = getCouponByCodeUseCase.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(testCoupon, result.getCoupon());
        assertEquals("TESTCODE123", result.getCoupon().getCode());
        assertEquals("Test Coupon", result.getCoupon().getTitle());
        assertEquals(DiscountType.PERCENT, result.getCoupon().getDiscountType());
        assertEquals(UsageType.MANUAL, result.getCoupon().getUsageType());
        verify(couponRepository).findByCode("TESTCODE123");
    }

    @Test
    void execute_WithNonExistentCouponCode_ShouldThrowResourceNotFoundException() {
        // Given
        GetCouponByCodeUseCase.InputValues input = new GetCouponByCodeUseCase.InputValues("NONEXISTENT");

        when(couponRepository.findByCode("NONEXISTENT")).thenReturn(null);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> getCouponByCodeUseCase.execute(input)
        );
        assertEquals("Cannot found coupon with code: NONEXISTENT", exception.getMessage());
        verify(couponRepository).findByCode("NONEXISTENT");
    }

    @Test
    void execute_WithEmptyString_ShouldCallRepositoryAndThrowException() {
        // Given
        GetCouponByCodeUseCase.InputValues input = new GetCouponByCodeUseCase.InputValues("");

        when(couponRepository.findByCode("")).thenReturn(null);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> getCouponByCodeUseCase.execute(input)
        );
        assertEquals("Cannot found coupon with code: ", exception.getMessage());
        verify(couponRepository).findByCode("");
    }

    @Test
    void execute_WithExpiredCoupon_ShouldStillReturnCoupon() {
        // Given
        Coupon expiredCoupon = new Coupon(
                "EXPIRED123",
                "Expired Coupon",
                "This coupon has expired",
                DiscountType.FIXED,
                UsageType.AUTO,
                new BigDecimal("100"),
                new BigDecimal("100"),
                new BigDecimal("200"),
                now.minusHours(3),
                now.minusHours(1), // Expired
                0
        );

        GetCouponByCodeUseCase.InputValues input = new GetCouponByCodeUseCase.InputValues("EXPIRED123");

        when(couponRepository.findByCode("EXPIRED123")).thenReturn(expiredCoupon);

        // When
        GetCouponByCodeUseCase.OutputValues result = getCouponByCodeUseCase.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(expiredCoupon, result.getCoupon());
        assertEquals("EXPIRED123", result.getCoupon().getCode());
        assertTrue(result.getCoupon().getEndTime().isBefore(now));
        verify(couponRepository).findByCode("EXPIRED123");
    }

    @Test
    void execute_WithExhaustedCoupon_ShouldStillReturnCoupon() {
        // Given
        Coupon exhaustedCoupon = new Coupon(
                "EXHAUSTED123",
                "Exhausted Coupon",
                "This coupon has no remaining usage",
                DiscountType.PERCENT,
                UsageType.MANUAL,
                new BigDecimal("20"),
                new BigDecimal("50"),
                new BigDecimal("100"),
                now.minusHours(1),
                now.plusHours(1),
                0 // No remaining usage
        );

        GetCouponByCodeUseCase.InputValues input = new GetCouponByCodeUseCase.InputValues("EXHAUSTED123");

        when(couponRepository.findByCode("EXHAUSTED123")).thenReturn(exhaustedCoupon);

        // When
        GetCouponByCodeUseCase.OutputValues result = getCouponByCodeUseCase.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(exhaustedCoupon, result.getCoupon());
        assertEquals("EXHAUSTED123", result.getCoupon().getCode());
        assertEquals(0, result.getCoupon().getRemainingUsage());
        verify(couponRepository).findByCode("EXHAUSTED123");
    }

    @Test
    void execute_WithDifferentCouponTypes_ShouldReturnCorrectCoupon() {
        // Given - Test with FIXED discount type
        Coupon fixedCoupon = new Coupon(
                "FIXED100",
                "Fixed Discount Coupon",
                "Get 100 off your order",
                DiscountType.FIXED,
                UsageType.AUTO,
                new BigDecimal("100"),
                new BigDecimal("100"),
                new BigDecimal("300"),
                now.minusHours(2),
                now.plusHours(2),
                15
        );

        GetCouponByCodeUseCase.InputValues input = new GetCouponByCodeUseCase.InputValues("FIXED100");

        when(couponRepository.findByCode("FIXED100")).thenReturn(fixedCoupon);

        // When
        GetCouponByCodeUseCase.OutputValues result = getCouponByCodeUseCase.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(fixedCoupon, result.getCoupon());
        assertEquals("FIXED100", result.getCoupon().getCode());
        assertEquals(DiscountType.FIXED, result.getCoupon().getDiscountType());
        assertEquals(UsageType.AUTO, result.getCoupon().getUsageType());
        verify(couponRepository).findByCode("FIXED100");
    }

} 