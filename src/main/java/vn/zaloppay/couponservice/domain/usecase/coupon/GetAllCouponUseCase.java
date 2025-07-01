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
import vn.zaloppay.couponservice.domain.model.UsageType;
import vn.zaloppay.couponservice.domain.repository.ICouponRepository;
import vn.zaloppay.couponservice.domain.usecase.UseCase;
import vn.zaloppay.couponservice.domain.util.SortUtils;


@Component
@RequiredArgsConstructor
public class GetAllCouponUseCase extends UseCase<GetAllCouponUseCase.InputValues, GetAllCouponUseCase.OutputValues> {

    private final ICouponRepository couponRepository;

    /**
     * Retrieves all coupons with optional filtering, sorting, and pagination.
     * 
     * <p>This method supports:
     * <ul>
     *   <li>Filtering by discount type and usage type (optional)</li>
     *   <li>Sorting by configurable fields and direction</li>
     *   <li>Pagination with customizable page size</li>
     * </ul>
     * 
     * @param input search criteria including filters, pagination, and sorting parameters
     * @return OutputValues containing paginated coupon results with metadata
     */
    @Override
    public OutputValues execute(GetAllCouponUseCase.InputValues input) {
        // Create sorting
        Sort sort = SortUtils.createSort(input.getSortBy(), input.getSortDirection(), CouponSortField::from);
        
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
