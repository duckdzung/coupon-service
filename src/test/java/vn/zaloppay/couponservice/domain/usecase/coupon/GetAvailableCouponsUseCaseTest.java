package vn.zaloppay.couponservice.domain.usecase.coupon;

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
import vn.zaloppay.couponservice.domain.model.Coupon;
import vn.zaloppay.couponservice.domain.model.UsageType;
import vn.zaloppay.couponservice.domain.model.discount.DiscountType;
import vn.zaloppay.couponservice.domain.repository.ICouponRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAvailableCouponsUseCaseTest {

    @Mock
    private ICouponRepository couponRepository;

    @InjectMocks
    private GetAvailableCouponsUseCase getAvailableCouponsUseCase;

    private List<Coupon> availableCoupons;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        Coupon coupon1 = new Coupon(
                1L,
                "AVAILABLE10",
                "Available 10% Discount",
                "Get 10% off available now",
                DiscountType.PERCENT,
                UsageType.AUTO,
                new BigDecimal("10"),
                new BigDecimal("50"),
                new BigDecimal("100"),
                now.minusHours(1),
                now.plusHours(1),
                5
        );

        Coupon coupon2 = new Coupon(
                2L,
                "AVAILABLE50",
                "Available Fixed 50 Off",
                "Get 50 off available now",
                DiscountType.FIXED,
                UsageType.AUTO,
                new BigDecimal("50"),
                new BigDecimal("50"),
                new BigDecimal("200"),
                now.minusHours(2),
                now.plusHours(2),
                10
        );

        availableCoupons = Arrays.asList(coupon1, coupon2);
    }

    @Test
    void execute_WithValidOrderAmount_ShouldReturnAvailableCoupons() {
        // Given
        GetAvailableCouponsUseCase.InputValues input = new GetAvailableCouponsUseCase.InputValues(
                new BigDecimal("250"),
                null,
                0,
                10,
                "discountValue",
                "ASC"
        );

        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "discountValue"));
        Page<Coupon> expectedPage = new PageImpl<>(availableCoupons, expectedPageable, availableCoupons.size());

        when(couponRepository.findAvailableCoupons(
                eq(new BigDecimal("250")),
                eq(null),
                any(LocalDateTime.class),
                eq(expectedPageable)
        )).thenReturn(expectedPage);

        // When
        GetAvailableCouponsUseCase.OutputValues result = getAvailableCouponsUseCase.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(expectedPage, result.getCouponsPage());
        assertEquals(2, result.getCouponsPage().getContent().size());
        verify(couponRepository).findAvailableCoupons(
                eq(new BigDecimal("250")),
                eq(null),
                any(LocalDateTime.class),
                eq(expectedPageable)
        );
    }

    @Test
    void execute_WithDiscountTypeFilter_ShouldReturnFilteredCoupons() {
        // Given
        GetAvailableCouponsUseCase.InputValues input = new GetAvailableCouponsUseCase.InputValues(
                new BigDecimal("300"),
                DiscountType.PERCENT,
                0,
                5,
                "title",
                "DESC"
        );

        Pageable expectedPageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "title"));
        List<Coupon> filteredCoupons = Arrays.asList(availableCoupons.get(0)); // Only PERCENT coupon
        Page<Coupon> expectedPage = new PageImpl<>(filteredCoupons, expectedPageable, filteredCoupons.size());

        when(couponRepository.findAvailableCoupons(
                eq(new BigDecimal("300")),
                eq(DiscountType.PERCENT),
                any(LocalDateTime.class),
                eq(expectedPageable)
        )).thenReturn(expectedPage);

        // When
        GetAvailableCouponsUseCase.OutputValues result = getAvailableCouponsUseCase.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(expectedPage, result.getCouponsPage());
        assertEquals(1, result.getCouponsPage().getContent().size());
        assertEquals(DiscountType.PERCENT, result.getCouponsPage().getContent().get(0).getDiscountType());
        verify(couponRepository).findAvailableCoupons(
                eq(new BigDecimal("300")),
                eq(DiscountType.PERCENT),
                any(LocalDateTime.class),
                eq(expectedPageable)
        );
    }

    @Test
    void execute_WithDifferentPageSize_ShouldRespectPagination() {
        // Given
        GetAvailableCouponsUseCase.InputValues input = new GetAvailableCouponsUseCase.InputValues(
                new BigDecimal("150"),
                DiscountType.FIXED,
                1, // Second page
                3, // Small page size
                "discountValue",
                "ASC"
        );

        Pageable expectedPageable = PageRequest.of(1, 3, Sort.by(Sort.Direction.ASC, "discountValue"));
        List<Coupon> filteredCoupons = Arrays.asList(availableCoupons.get(1)); // Only FIXED coupon
        Page<Coupon> expectedPage = new PageImpl<>(filteredCoupons, expectedPageable, filteredCoupons.size());

        when(couponRepository.findAvailableCoupons(
                eq(new BigDecimal("150")),
                eq(DiscountType.FIXED),
                any(LocalDateTime.class),
                eq(expectedPageable)
        )).thenReturn(expectedPage);

        // When
        GetAvailableCouponsUseCase.OutputValues result = getAvailableCouponsUseCase.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(expectedPage, result.getCouponsPage());
        assertEquals(1, result.getCouponsPage().getContent().size());
        assertEquals(DiscountType.FIXED, result.getCouponsPage().getContent().get(0).getDiscountType());
        verify(couponRepository).findAvailableCoupons(
                eq(new BigDecimal("150")),
                eq(DiscountType.FIXED),
                any(LocalDateTime.class),
                eq(expectedPageable)
        );
    }

    @Test
    void execute_WithDescendingSortDirection_ShouldCreateDescendingSort() {
        // Given
        GetAvailableCouponsUseCase.InputValues input = new GetAvailableCouponsUseCase.InputValues(
                new BigDecimal("500"),
                null,
                0,
                20,
                "endTime",
                "DESC"
        );

        Pageable expectedPageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "endTime"));
        Page<Coupon> expectedPage = new PageImpl<>(availableCoupons, expectedPageable, availableCoupons.size());

        when(couponRepository.findAvailableCoupons(
                eq(new BigDecimal("500")),
                eq(null),
                any(LocalDateTime.class),
                eq(expectedPageable)
        )).thenReturn(expectedPage);

        // When
        GetAvailableCouponsUseCase.OutputValues result = getAvailableCouponsUseCase.execute(input);

        // Then
        assertNotNull(result);
        verify(couponRepository).findAvailableCoupons(
                eq(new BigDecimal("500")),
                eq(null),
                any(LocalDateTime.class),
                eq(expectedPageable)
        );
    }

    @Test
    void execute_WithLowOrderAmount_ShouldStillCallRepository() {
        // Given
        GetAvailableCouponsUseCase.InputValues input = new GetAvailableCouponsUseCase.InputValues(
                new BigDecimal("50"), // Low amount
                null,
                0,
                10,
                "maxDiscountAmount",
                "ASC"
        );

        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "maxDiscountAmount"));
        Page<Coupon> expectedPage = new PageImpl<>(availableCoupons, expectedPageable, 0); // No coupons available

        when(couponRepository.findAvailableCoupons(
                eq(new BigDecimal("50")),
                eq(null),
                any(LocalDateTime.class),
                eq(expectedPageable)
        )).thenReturn(expectedPage);

        // When
        GetAvailableCouponsUseCase.OutputValues result = getAvailableCouponsUseCase.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(expectedPage, result.getCouponsPage());
        verify(couponRepository).findAvailableCoupons(
                eq(new BigDecimal("50")),
                eq(null),
                any(LocalDateTime.class),
                eq(expectedPageable)
        );
    }
} 