package vn.zaloppay.couponservice.presenter.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.zaloppay.couponservice.core.usecases.IUseCaseExecutor;
import vn.zaloppay.couponservice.core.usecases.coupon.ApplyCouponUseCase;
import vn.zaloppay.couponservice.core.usecases.coupon.GetCouponByCodeUseCase;
import vn.zaloppay.couponservice.presenter.entities.ApiResponse;
import vn.zaloppay.couponservice.presenter.entities.ApplyCouponRequest;
import vn.zaloppay.couponservice.presenter.entities.ApplyCouponResponse;
import vn.zaloppay.couponservice.presenter.entities.CouponResponse;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/coupons")
@AllArgsConstructor
@Validated
public class CouponController {

    private final IUseCaseExecutor useCaseExecutor;

    private final GetCouponByCodeUseCase getCouponByCodeUseCase;

    private final ApplyCouponUseCase applyCouponUseCase;

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse> getCouponByCode(
            @PathVariable 
            @NotBlank(message = "Coupon code must not be blank") 
            @Size(min = 3, max = 10, message = "Coupon code must be between 3 and 10 characters") 
            String code) {
        
        CouponResponse coupon = useCaseExecutor.execute(
                getCouponByCodeUseCase,
                new GetCouponByCodeUseCase.InputValues(code),
                outputValues -> CouponResponse.from(outputValues.getCoupon())
        );
        
        return new ResponseEntity<>(ApiResponse.success(coupon, "Get coupon by code successfully"), HttpStatus.OK);
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse> applyCoupon(@Valid @RequestBody ApplyCouponRequest applyCouponRequest) {
        
        ApplyCouponResponse result = useCaseExecutor.execute(
                applyCouponUseCase,
                new ApplyCouponUseCase.InputValues(
                        applyCouponRequest.getOrderAmount(),
                        LocalDateTime.now(),
                        applyCouponRequest.getCouponCode()
                ),
                outputValues -> ApplyCouponResponse.from(
                        outputValues.getDiscountAmount(),
                        outputValues.getCoupon()
                )
        );
        
        return new ResponseEntity<>(ApiResponse.success(result, "Apply coupon successfully"), HttpStatus.OK);
    }

}
