package vn.zaloppay.couponservice.core.usecases.coupon;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import vn.zaloppay.couponservice.core.entities.Coupon;
import vn.zaloppay.couponservice.core.entities.CouponSortField;
import vn.zaloppay.couponservice.core.entities.discount.DiscountType;
import vn.zaloppay.couponservice.core.repositories.ICouponRepository;
import vn.zaloppay.couponservice.core.usecases.UseCase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class GetAvailableCouponsUseCase extends UseCase<GetAvailableCouponsUseCase.InputValues, GetAvailableCouponsUseCase.OutputValues> {

    private final ICouponRepository couponRepository;

    @Override
    public OutputValues execute(InputValues inputValues) {
        Sort sort = createSort(inputValues.getSortBy(), inputValues.getSortDirection());
        Pageable pageable = PageRequest.of(inputValues.getPage(), inputValues.getSize(), sort);

        Page<Coupon> couponsPage = couponRepository.findAvailableCoupons(
                inputValues.getOrderAmount(),
                inputValues.getDiscountType(),
                LocalDateTime.now(),
                pageable
        );

        return new OutputValues(couponsPage);
    }

    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        String field = CouponSortField.from(sortBy);
        return Sort.by(direction, field);
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