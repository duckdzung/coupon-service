package vn.zaloppay.couponservice.presenter.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.zaloppay.couponservice.core.entities.Coupon;
import vn.zaloppay.couponservice.core.usecases.IUseCaseExecutor;
import vn.zaloppay.couponservice.core.usecases.coupon.ApplyCouponUseCase;
import vn.zaloppay.couponservice.core.usecases.coupon.GetAvailableCouponsUseCase;
import vn.zaloppay.couponservice.core.usecases.coupon.GetCouponByCodeUseCase;
import vn.zaloppay.couponservice.presenter.config.rate_limit.RateLimit;
import vn.zaloppay.couponservice.presenter.entities.request.ApplyCouponRequest;
import vn.zaloppay.couponservice.presenter.entities.request.GetAvailableCouponsRequest;
import vn.zaloppay.couponservice.presenter.entities.response.ApiResponse;
import vn.zaloppay.couponservice.presenter.entities.response.ApplyCouponResponse;
import vn.zaloppay.couponservice.presenter.entities.response.AvailableCouponResponse;
import vn.zaloppay.couponservice.presenter.entities.response.CouponResponse;
import vn.zaloppay.couponservice.presenter.entities.response.GetAvailableCouponResponse;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Validated
public class CouponController {

    private final IUseCaseExecutor useCaseExecutor;

    private final GetCouponByCodeUseCase getCouponByCodeUseCase;

    private final ApplyCouponUseCase applyCouponUseCase;

    private final GetAvailableCouponsUseCase getAvailableCouponsUseCase;

    @GetMapping("/{code}")
    @RateLimit(maxRequests = 2, expirySeconds = 60)
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

    @GetMapping("/available")
    public ResponseEntity<ApiResponse> getAvailableCoupons(@Valid @ModelAttribute GetAvailableCouponsRequest request) {

        GetAvailableCouponResponse result = useCaseExecutor.execute(
                getAvailableCouponsUseCase,
                new GetAvailableCouponsUseCase.InputValues(
                        request.getOrderAmount(),
                        request.getDiscountType(),
                        request.getPage(),
                        request.getSize(),
                        request.getSortBy(),
                        request.getSortDirection()
                ),
                outputValues -> {
                    Page<Coupon> couponsPage = outputValues.getCouponsPage();

                    List<AvailableCouponResponse> couponResponses = couponsPage.getContent().stream()
                            .map(coupon -> AvailableCouponResponse.from(coupon, request.getOrderAmount()))
                            .toList();

                    return GetAvailableCouponResponse.builder()
                            .content(couponResponses)
                            .currentPage(couponsPage.getNumber())
                            .totalPages(couponsPage.getTotalPages())
                            .totalElements(couponsPage.getTotalElements())
                            .pageSize(couponsPage.getSize())
                            .hasNext(couponsPage.hasNext())
                            .hasPrevious(couponsPage.hasPrevious())
                            .build();
                }
        );

        return new ResponseEntity<>(ApiResponse.success(result, "Get available coupons successfully"), HttpStatus.OK);
    }

}
