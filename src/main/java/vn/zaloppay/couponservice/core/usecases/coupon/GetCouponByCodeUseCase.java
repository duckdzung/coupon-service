package vn.zaloppay.couponservice.core.usecases.coupon;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Component;
import vn.zaloppay.couponservice.core.entities.Coupon;
import vn.zaloppay.couponservice.core.exceptions.ResourceNotFoundException;
import vn.zaloppay.couponservice.core.repositories.ICouponRepository;
import vn.zaloppay.couponservice.core.usecases.UseCase;

@Component
@RequiredArgsConstructor
public class GetCouponByCodeUseCase extends UseCase<GetCouponByCodeUseCase.InputValues, GetCouponByCodeUseCase.OutputValues> {

    private final ICouponRepository couponRepository;

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
