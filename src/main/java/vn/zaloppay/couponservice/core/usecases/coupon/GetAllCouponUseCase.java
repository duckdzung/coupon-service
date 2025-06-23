package vn.zaloppay.couponservice.core.usecases.coupon;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Component;
import vn.zaloppay.couponservice.core.entities.Coupon;
import vn.zaloppay.couponservice.core.repositories.ICouponRepository;
import vn.zaloppay.couponservice.core.usecases.UseCase;

import java.util.List;

@Component
@AllArgsConstructor
public class GetAllCouponUseCase extends UseCase<GetAllCouponUseCase.InputValues, GetAllCouponUseCase.OutputValues> {

    private final ICouponRepository couponRepository;

    @Override
    public OutputValues execute(GetAllCouponUseCase.InputValues input) {
        return new OutputValues(couponRepository.findAll());
    }

    @Value
    public static class InputValues implements UseCase.InputValues {

    }

    @Value
    public static class OutputValues implements UseCase.OutputValues {
        List<Coupon> coupons;
    }

}
