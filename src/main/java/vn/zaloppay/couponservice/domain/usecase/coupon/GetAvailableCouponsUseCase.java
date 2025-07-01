package vn.zaloppay.couponservice.domain.usecase.coupon;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import vn.zaloppay.couponservice.domain.model.Coupon;
import vn.zaloppay.couponservice.domain.model.CouponSortField;
import vn.zaloppay.couponservice.domain.model.discount.DiscountType;
import vn.zaloppay.couponservice.domain.repository.ICouponRepository;
import vn.zaloppay.couponservice.domain.usecase.UseCase;
import vn.zaloppay.couponservice.domain.util.SortUtils;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class GetAvailableCouponsUseCase extends UseCase<GetAvailableCouponsUseCase.InputValues, GetAvailableCouponsUseCase.OutputValues> {

    private final ICouponRepository couponRepository;

    /**
     * Retrieves coupons that are available and applicable for a given order amount.
     * 
     * <p>Returns only coupons that are:
     * <ul>
     *   <li>Currently active (within valid time range)</li>
     *   <li>Have remaining usage available</li>
     *   <li>Meet the minimum order value requirement</li>
     *   <li>Match the specified discount type (if provided)</li>
     * </ul>
     * 
     * @param inputValues order amount, optional discount type filter, and pagination parameters
     * @return OutputValues containing paginated available coupons
     */
    @Override
    public OutputValues execute(InputValues inputValues) {
        Sort sort = SortUtils.createSort(inputValues.getSortBy(), inputValues.getSortDirection(), CouponSortField::from);
        Pageable pageable = PageRequest.of(inputValues.getPage(), inputValues.getSize(), sort);

        Page<Coupon> couponsPage = couponRepository.findAvailableCoupons(
                inputValues.getOrderAmount(),
                inputValues.getDiscountType(),
                LocalDateTime.now(),
                pageable
        );

        return new OutputValues(couponsPage);
    }

    @Value
    public static class InputValues implements UseCase.InputValues {
        BigDecimal orderAmount;
        DiscountType discountType;
        Integer page;
        Integer size;
        String sortBy;
        String sortDirection;
    }

    @Value
    public static class OutputValues implements UseCase.OutputValues {
        Page<Coupon> couponsPage;
    }

} 