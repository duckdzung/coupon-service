package vn.zaloppay.couponservice.core.usecases.coupon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import vn.zaloppay.couponservice.core.entities.Coupon;
import vn.zaloppay.couponservice.core.entities.UsageType;
import vn.zaloppay.couponservice.core.entities.discount.DiscountType;
import vn.zaloppay.couponservice.core.repositories.ICouponRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllCouponUseCaseTest {

    @Mock
    private ICouponRepository couponRepository;

    @InjectMocks
    private GetAllCouponUseCase getAllCouponUseCase;

    private List<Coupon> testCoupons;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        Coupon coupon1 = new Coupon(
                "PERCENT10",
                "10% Discount",
                "Get 10% off",
                DiscountType.PERCENT,
                UsageType.MANUAL,
                new BigDecimal("10"),
                new BigDecimal("50"),
                new BigDecimal("100"),
                now.minusHours(1),
                now.plusHours(1),
                5
        );

        Coupon coupon2 = new Coupon(
                "FIXED50",
                "Fixed 50 Off",
                "Get 50 off",
                DiscountType.FIXED,
                UsageType.AUTO,
                new BigDecimal("50"),
                new BigDecimal("50"),
                new BigDecimal("200"),
                now.minusHours(2),
                now.plusHours(2),
                10
        );

        testCoupons = Arrays.asList(coupon1, coupon2);
    }

    @Test
    void execute_WithDefaultParameters_ShouldReturnPaginatedCoupons() {
        // Given
        GetAllCouponUseCase.InputValues input = new GetAllCouponUseCase.InputValues(
                null, // No discount type filter
                null, // No usage type filter
                0,    // First page
                10,   // Page size
                "discountValue", // Valid sort field
                "ASC"   // Ascending
        );

        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "discountValue"));
        Page<Coupon> expectedPage = new PageImpl<>(testCoupons, expectedPageable, testCoupons.size());

        when(couponRepository.findAll(null, null, expectedPageable)).thenReturn(expectedPage);

        // When
        GetAllCouponUseCase.OutputValues result = getAllCouponUseCase.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(expectedPage, result.getCoupons());
        assertEquals(2, result.getCoupons().getContent().size());
        verify(couponRepository).findAll(null, null, expectedPageable);
    }

    @Test
    void execute_WithDiscountTypeFilter_ShouldReturnFilteredCoupons() {
        // Given
        GetAllCouponUseCase.InputValues input = new GetAllCouponUseCase.InputValues(
                DiscountType.PERCENT,
                null,
                0,
                10,
                "title",
                "DESC"
        );

        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "title"));
        List<Coupon> filteredCoupons = Collections.singletonList(testCoupons.getFirst()); // Only PERCENT coupon
        Page<Coupon> expectedPage = new PageImpl<>(filteredCoupons, expectedPageable, filteredCoupons.size());

        when(couponRepository.findAll(DiscountType.PERCENT, null, expectedPageable)).thenReturn(expectedPage);

        // When
        GetAllCouponUseCase.OutputValues result = getAllCouponUseCase.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(expectedPage, result.getCoupons());
        assertEquals(1, result.getCoupons().getContent().size());
        assertEquals(DiscountType.PERCENT, result.getCoupons().getContent().get(0).getDiscountType());
        verify(couponRepository).findAll(DiscountType.PERCENT, null, expectedPageable);
    }

    @Test
    void execute_WithUsageTypeFilter_ShouldReturnFilteredCoupons() {
        // Given
        GetAllCouponUseCase.InputValues input = new GetAllCouponUseCase.InputValues(
                null,
                UsageType.AUTO,
                1, // Second page
                5, // Smaller page size
                "discountValue",
                "ASC"
        );

        Pageable expectedPageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.ASC, "discountValue"));
        List<Coupon> filteredCoupons = Collections.singletonList(testCoupons.get(1)); // Only AUTO coupon
        Page<Coupon> expectedPage = new PageImpl<>(filteredCoupons, expectedPageable, filteredCoupons.size());

        when(couponRepository.findAll(null, UsageType.AUTO, expectedPageable)).thenReturn(expectedPage);

        // When
        GetAllCouponUseCase.OutputValues result = getAllCouponUseCase.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(expectedPage, result.getCoupons());
        assertEquals(1, result.getCoupons().getContent().size());
        assertEquals(UsageType.AUTO, result.getCoupons().getContent().get(0).getUsageType());
        verify(couponRepository).findAll(null, UsageType.AUTO, expectedPageable);
    }

    @Test
    void execute_WithBothFilters_ShouldReturnFilteredCoupons() {
        // Given
        GetAllCouponUseCase.InputValues input = new GetAllCouponUseCase.InputValues(
                DiscountType.FIXED,
                UsageType.AUTO,
                0,
                20,
                "endTime",
                "DESC"
        );

        Pageable expectedPageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "endTime"));
        List<Coupon> filteredCoupons = Collections.singletonList(testCoupons.get(1)); // FIXED and AUTO coupon
        Page<Coupon> expectedPage = new PageImpl<>(filteredCoupons, expectedPageable, filteredCoupons.size());

        when(couponRepository.findAll(DiscountType.FIXED, UsageType.AUTO, expectedPageable))
                .thenReturn(expectedPage);

        // When
        GetAllCouponUseCase.OutputValues result = getAllCouponUseCase.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(expectedPage, result.getCoupons());
        assertEquals(1, result.getCoupons().getContent().size());
        Coupon resultCoupon = result.getCoupons().getContent().get(0);
        assertEquals(DiscountType.FIXED, resultCoupon.getDiscountType());
        assertEquals(UsageType.AUTO, resultCoupon.getUsageType());
        verify(couponRepository).findAll(DiscountType.FIXED, UsageType.AUTO, expectedPageable);
    }

    @Test
    void execute_WithDescendingSortDirection_ShouldCreateDescendingSort() {
        // Given
        GetAllCouponUseCase.InputValues input = new GetAllCouponUseCase.InputValues(
                null,
                null,
                0,
                10,
                "endTime",
                "DESC"
        );

        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "endTime"));
        Page<Coupon> expectedPage = new PageImpl<>(testCoupons, expectedPageable, testCoupons.size());

        when(couponRepository.findAll(null, null, expectedPageable)).thenReturn(expectedPage);

        // When
        GetAllCouponUseCase.OutputValues result = getAllCouponUseCase.execute(input);

        // Then
        assertNotNull(result);
        verify(couponRepository).findAll(null, null, expectedPageable);
    }

    @Test
    void execute_WithInvalidSortDirection_ShouldDefaultToAscending() {
        // Given
        GetAllCouponUseCase.InputValues input = new GetAllCouponUseCase.InputValues(
                null,
                null,
                0,
                10,
                "title",
                "INVALID" // Invalid sort direction
        );

        // Should default to ASC
        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "title"));
        Page<Coupon> expectedPage = new PageImpl<>(testCoupons, expectedPageable, testCoupons.size());

        when(couponRepository.findAll(null, null, expectedPageable)).thenReturn(expectedPage);

        // When
        GetAllCouponUseCase.OutputValues result = getAllCouponUseCase.execute(input);

        // Then
        assertNotNull(result);
        verify(couponRepository).findAll(null, null, expectedPageable);
    }

    @Test
    void execute_WithNullSortDirection_ShouldDefaultToAscending() {
        // Given
        GetAllCouponUseCase.InputValues input = new GetAllCouponUseCase.InputValues(
                null,
                null,
                0,
                10,
                "maxDiscountAmount",
                null // Null sort direction
        );

        // Should default to ASC  
        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "maxDiscountAmount"));
        Page<Coupon> expectedPage = new PageImpl<>(testCoupons, expectedPageable, testCoupons.size());

        when(couponRepository.findAll(null, null, expectedPageable)).thenReturn(expectedPage);

        // When
        GetAllCouponUseCase.OutputValues result = getAllCouponUseCase.execute(input);

        // Then
        assertNotNull(result);
        verify(couponRepository).findAll(null, null, expectedPageable);
    }

    @Test
    void execute_WithDifferentPageSizes_ShouldRespectPageSize() {
        // Given
        GetAllCouponUseCase.InputValues input = new GetAllCouponUseCase.InputValues(
                null,
                null,
                2, // Third page
                3, // Small page size
                "discountValue",
                "ASC"
        );

        Pageable expectedPageable = PageRequest.of(2, 3, Sort.by(Sort.Direction.ASC, "discountValue"));
        Page<Coupon> expectedPage = new PageImpl<>(testCoupons, expectedPageable, testCoupons.size());

        when(couponRepository.findAll(null, null, expectedPageable)).thenReturn(expectedPage);

        // When
        GetAllCouponUseCase.OutputValues result = getAllCouponUseCase.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(expectedPage, result.getCoupons());
        verify(couponRepository).findAll(null, null, expectedPageable);
    }

} 