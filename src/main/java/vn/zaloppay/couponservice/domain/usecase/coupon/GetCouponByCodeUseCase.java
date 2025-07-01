package vn.zaloppay.couponservice.domain.usecase.coupon;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Component;
import vn.zaloppay.couponservice.domain.model.Coupon;
import vn.zaloppay.couponservice.domain.exceptions.ResourceNotFoundException;
import vn.zaloppay.couponservice.domain.repository.ICouponRepository;
import vn.zaloppay.couponservice.domain.usecase.UseCase;

@Component
@RequiredArgsConstructor
public class GetCouponByCodeUseCase extends UseCase<GetCouponByCodeUseCase.InputValues, GetCouponByCodeUseCase.OutputValues> {

    private final ICouponRepository couponRepository;

    /**
     * Retrieves a coupon by its unique code.
     * 
     * @param input coupon code to search for
     * @return OutputValues containing the found coupon details
     * @throws ResourceNotFoundException if no coupon exists with the specified code
     */
    @Override
    public OutputValues execute(InputValues input) {
        Coupon coupon = couponRepository.findByCode(input.getCode());

        if (coupon == null) {
            throw new ResourceNotFoundException("Cannot found coupon with code: " + input.getCode());
        }

        return new OutputValues(coupon);
    }


    @Value
    public static class InputValues implements UseCase.InputValues {
        String code;
    }

    @Value
    public static class OutputValues implements UseCase.OutputValues {
        Coupon coupon;
    }

}
