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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCouponUseCaseTest {

    @Mock
    private ICouponRepository couponRepository;

    @InjectMocks
    private UpdateCouponUseCase updateCouponUseCase;

    private Coupon existingCoupon;
    private LocalDateTime now;
    private UpdateCouponUseCase.InputValues validUpdateInput;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        existingCoupon = new Coupon(
                "UPDATEME",
                "Original Title",
                "Original Description",
                DiscountType.PERCENT,
                UsageType.MANUAL,
                new BigDecimal("10"), // This should not be updated
                new BigDecimal("30"),
                new BigDecimal("100"),
                now.minusHours(1),
                now.plusHours(1),
                5
        );

        validUpdateInput = new UpdateCouponUseCase.InputValues(
                "UPDATEME",
                "Updated Title",
                "Updated Description",
                DiscountType.FIXED,
                UsageType.AUTO,
                new BigDecimal("80"), // Updated max discount
                new BigDecimal("200"), // Updated min order value
                now.plusHours(2), // Updated start time
                now.plusDays(1), // Updated end time
                15 // Updated remaining usage
        );
    }

    @Test
    void execute_WithValidUpdate_ShouldUpdateCoupon() {
        // Given
        Coupon expectedUpdatedCoupon = new Coupon(
                "UPDATEME",
                "Updated Title",
                "Updated Description",
                DiscountType.FIXED,
                UsageType.AUTO,
                new BigDecimal("10"), // Original discount value preserved
                new BigDecimal("80"),
                new BigDecimal("200"),
                now.plusHours(2),
                now.plusDays(1),
                15
        );

        when(couponRepository.findByCode("UPDATEME")).thenReturn(existingCoupon);
        when(couponRepository.update(any(Coupon.class))).thenReturn(expectedUpdatedCoupon);

        // When
        UpdateCouponUseCase.OutputValues result = updateCouponUseCase.execute(validUpdateInput);

        // Then
        assertNotNull(result);
        assertEquals(expectedUpdatedCoupon, result.getCoupon());

        // Verify the updated fields
        assertEquals("Updated Title", result.getCoupon().getTitle());
        assertEquals("Updated Description", result.getCoupon().getDescription());
        assertEquals(DiscountType.FIXED, result.getCoupon().getDiscountType());
        assertEquals(UsageType.AUTO, result.getCoupon().getUsageType());
        assertEquals(new BigDecimal("80"), result.getCoupon().getMaxDiscountAmount());
        assertEquals(new BigDecimal("200"), result.getCoupon().getMinOrderValue());
        assertEquals(15, result.getCoupon().getRemainingUsage());

        // Verify preserved field
        assertEquals(new BigDecimal("10"), result.getCoupon().getDiscountValue());

        verify(couponRepository).findByCode("UPDATEME");
        verify(couponRepository).update(any(Coupon.class));
    }

    @Test
    void execute_WithNonExistentCoupon_ShouldThrowResourceNotFoundException() {
        // Given
        UpdateCouponUseCase.InputValues input = new UpdateCouponUseCase.InputValues(
                "NONEXISTENT",
                "Some Title",
                "Some Description",
                DiscountType.PERCENT,
                UsageType.MANUAL,
                new BigDecimal("50"),
                new BigDecimal("150"),
                now.plusHours(1),
                now.plusDays(1),
                10
        );

        when(couponRepository.findByCode("NONEXISTENT")).thenReturn(null);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> updateCouponUseCase.execute(input)
        );
        assertEquals("Cannot find coupon with code: NONEXISTENT", exception.getMessage());
        verify(couponRepository).findByCode("NONEXISTENT");
        verify(couponRepository, never()).update(any(Coupon.class));
    }

    @Test
    void execute_WithPartialUpdate_ShouldUpdateOnlyProvidedFields() {
        // Given
        UpdateCouponUseCase.InputValues partialUpdateInput = new UpdateCouponUseCase.InputValues(
                "UPDATEME",
                "New Title Only",
                existingCoupon.getDescription(), // Keep same description
                existingCoupon.getDiscountType(), // Keep same discount type
                existingCoupon.getUsageType(), // Keep same usage type
                existingCoupon.getMaxDiscountAmount(), // Keep same max discount
                existingCoupon.getMinOrderValue(), // Keep same min order value
                existingCoupon.getStartTime(), // Keep same start time
                existingCoupon.getEndTime(), // Keep same end time
                existingCoupon.getRemainingUsage() // Keep same remaining usage
        );

        Coupon expectedPartiallyUpdatedCoupon = new Coupon(
                "UPDATEME",
                "New Title Only",
                existingCoupon.getDescription(),
                existingCoupon.getDiscountType(),
                existingCoupon.getUsageType(),
                existingCoupon.getDiscountValue(), // Original discount value preserved
                existingCoupon.getMaxDiscountAmount(),
                existingCoupon.getMinOrderValue(),
                existingCoupon.getStartTime(),
                existingCoupon.getEndTime(),
                existingCoupon.getRemainingUsage()
        );

        when(couponRepository.findByCode("UPDATEME")).thenReturn(existingCoupon);
        when(couponRepository.update(any(Coupon.class))).thenReturn(expectedPartiallyUpdatedCoupon);

        // When
        UpdateCouponUseCase.OutputValues result = updateCouponUseCase.execute(partialUpdateInput);

        // Then
        assertNotNull(result);
        assertEquals("New Title Only", result.getCoupon().getTitle());
        assertEquals(existingCoupon.getDescription(), result.getCoupon().getDescription());
        assertEquals(existingCoupon.getDiscountType(), result.getCoupon().getDiscountType());
        verify(couponRepository).findByCode("UPDATEME");
        verify(couponRepository).update(any(Coupon.class));
    }

    @Test
    void execute_WithDifferentDiscountTypes_ShouldUpdateCorrectly() {
        // Given - Change from PERCENT to FIXED
        UpdateCouponUseCase.InputValues changeTypeInput = new UpdateCouponUseCase.InputValues(
                "UPDATEME",
                "Changed to Fixed",
                "Now a fixed discount coupon",
                DiscountType.FIXED,
                UsageType.AUTO,
                new BigDecimal("100"), // New max discount for fixed type
                new BigDecimal("300"), // Higher min order value
                now.plusHours(3),
                now.plusDays(3),
                20
        );

        Coupon expectedChangedTypeCoupon = new Coupon(
                "UPDATEME",
                "Changed to Fixed",
                "Now a fixed discount coupon",
                DiscountType.FIXED,
                UsageType.AUTO,
                existingCoupon.getDiscountValue(), // Original discount value preserved
                new BigDecimal("100"),
                new BigDecimal("300"),
                now.plusHours(3),
                now.plusDays(3),
                20
        );

        when(couponRepository.findByCode("UPDATEME")).thenReturn(existingCoupon);
        when(couponRepository.update(any(Coupon.class))).thenReturn(expectedChangedTypeCoupon);

        // When
        UpdateCouponUseCase.OutputValues result = updateCouponUseCase.execute(changeTypeInput);

        // Then
        assertNotNull(result);
        assertEquals(DiscountType.FIXED, result.getCoupon().getDiscountType());
        assertEquals(UsageType.AUTO, result.getCoupon().getUsageType());
        assertEquals("Changed to Fixed", result.getCoupon().getTitle());
        verify(couponRepository).findByCode("UPDATEME");
        verify(couponRepository).update(any(Coupon.class));
    }

    @Test
    void execute_ShouldPreserveOriginalDiscountValue() {
        // Given - Ensure discount value is always preserved from original coupon
        when(couponRepository.findByCode("UPDATEME")).thenReturn(existingCoupon);
        when(couponRepository.update(any(Coupon.class))).thenAnswer(invocation -> {
            Coupon updatedCoupon = invocation.getArgument(0);
            // Verify that the discount value matches the original
            assertEquals(existingCoupon.getDiscountValue(), updatedCoupon.getDiscountValue());
            return updatedCoupon;
        });

        // When
        UpdateCouponUseCase.OutputValues result = updateCouponUseCase.execute(validUpdateInput);

        // Then
        assertNotNull(result);
        verify(couponRepository).findByCode("UPDATEME");
        verify(couponRepository).update(any(Coupon.class));
    }
} 