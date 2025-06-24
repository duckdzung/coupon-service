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
import vn.zaloppay.couponservice.core.entities.UsageType;
import vn.zaloppay.couponservice.core.repositories.ICouponRepository;
import vn.zaloppay.couponservice.core.usecases.UseCase;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetAllCouponUseCase extends UseCase<GetAllCouponUseCase.InputValues, GetAllCouponUseCase.OutputValues> {

    private final ICouponRepository couponRepository;

    @Override
    public OutputValues execute(GetAllCouponUseCase.InputValues input) {
        // Create sorting
        Sort sort = createSort(input.getSortBy(), input.getSortDirection());
        
        // Create pageable
        Pageable pageable = PageRequest.of(input.getPage(), input.getSize(), sort);
        
        // Get paginated results
        Page<Coupon> couponPage = couponRepository.findAll(
                input.getDiscountType(), 
                input.getUsageType(), 
                pageable
        );
        
        return new OutputValues(couponPage);
    }

    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        
        // Validate and map sort field
        String validSortBy = mapSortField(sortBy);
        return Sort.by(direction, validSortBy);
    }

    private String mapSortField(String sortBy) {
        return CouponSortField.from(sortBy);
    }

    @Value
    public static class InputValues implements UseCase.InputValues {
        DiscountType discountType;
        UsageType usageType;
        Integer page;
        Integer size;
        String sortBy;
        String sortDirection;
    }

    @Value
    public static class OutputValues implements UseCase.OutputValues {
        Page<Coupon> coupons;
    }

}
