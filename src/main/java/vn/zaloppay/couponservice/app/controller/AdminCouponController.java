package vn.zaloppay.couponservice.app.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.zaloppay.couponservice.domain.model.Coupon;
import vn.zaloppay.couponservice.domain.usecase.IUseCaseExecutor;
import vn.zaloppay.couponservice.domain.usecase.coupon.CreateCouponUseCase;
import vn.zaloppay.couponservice.domain.usecase.coupon.GetAllCouponUseCase;
import vn.zaloppay.couponservice.domain.usecase.coupon.UpdateCouponUseCase;
import vn.zaloppay.couponservice.app.config.logging.Limer;
import vn.zaloppay.couponservice.app.model.request.CreateCouponRequest;
import vn.zaloppay.couponservice.app.model.request.GetAllCouponsRequest;
import vn.zaloppay.couponservice.app.model.request.UpdateCouponRequest;
import vn.zaloppay.couponservice.app.model.response.ApiResponse;
import vn.zaloppay.couponservice.app.model.response.CouponResponse;
import vn.zaloppay.couponservice.app.model.response.DeleteCouponResponse;
import vn.zaloppay.couponservice.app.model.response.GetAllCouponsResponse;

@RestController
@RequestMapping("/api/v1/admin/coupons")
@RequiredArgsConstructor
@Validated
@Limer(enabledLogLatency = true, enabledLogInOut = true)
public class AdminCouponController {

    private final IUseCaseExecutor useCaseExecutor;

    private final CreateCouponUseCase createCouponUseCase;

    private final GetAllCouponUseCase getAllCouponUseCase;

    private final UpdateCouponUseCase updateCouponUseCase;

    @GetMapping()
    public ResponseEntity<ApiResponse> getAllCoupons(@Valid @ModelAttribute GetAllCouponsRequest request) {
        GetAllCouponsResponse response = useCaseExecutor.execute(
                getAllCouponUseCase,
                new GetAllCouponUseCase.InputValues(
                        request.getDiscountType(),
                        request.getUsageType(),
                        request.getPage(),
                        request.getSize(),
                        request.getSortBy(),
                        request.getSortDirection()
                ),
                outputValues -> {
                    Page<Coupon> couponPage = outputValues.getCoupons();
                    return GetAllCouponsResponse.builder()
                            .content(CouponResponse.from(couponPage.getContent()))
                            .currentPage(couponPage.getNumber())
                            .totalPages(couponPage.getTotalPages())
                            .totalElements(couponPage.getTotalElements())
                            .pageSize(couponPage.getSize())
                            .hasNext(couponPage.hasNext())
                            .hasPrevious(couponPage.hasPrevious())
                            .build();
                });

        return new ResponseEntity<>(ApiResponse.success(response, "Get all coupons successfully"), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createCoupon(@Valid @RequestBody CreateCouponRequest createCouponRequest) {
        CouponResponse coupon = useCaseExecutor.execute(
                createCouponUseCase,
                new CreateCouponUseCase.InputValues(
                        createCouponRequest.getCode(),
                        createCouponRequest.getTitle(),
                        createCouponRequest.getDescription(),
                        createCouponRequest.getDiscountType(),
                        createCouponRequest.getUsageType(),
                        createCouponRequest.getDiscountValue(),
                        createCouponRequest.getMinOrderValue(),
                        createCouponRequest.getMaxDiscountAmount(),
                        createCouponRequest.getStartTime(),
                        createCouponRequest.getEndTime(),
                        createCouponRequest.getRemainingUsage()

                ),
                outputValues -> CouponResponse.from(outputValues.getCoupon()));

        return new ResponseEntity<>(ApiResponse.success(coupon, "Create coupon successfully"), HttpStatus.OK);
    }

    @PutMapping("/{code}")
    public ResponseEntity<ApiResponse> updateCoupon(
            @PathVariable
            @NotBlank(message = "Coupon code must not be blank")
            @Size(min = 3, max = 10, message = "Coupon code must be between 3 and 10 characters")
            String code,
            @Valid @RequestBody UpdateCouponRequest updateCouponRequest) {

        CouponResponse coupon = useCaseExecutor.execute(
                updateCouponUseCase,
                new UpdateCouponUseCase.InputValues(
                        code,
                        updateCouponRequest.getTitle(),
                        updateCouponRequest.getDescription(),
                        updateCouponRequest.getDiscountType(),
                        updateCouponRequest.getUsageType(),
                        updateCouponRequest.getMaxDiscountAmount(),
                        updateCouponRequest.getMinOrderValue(),
                        updateCouponRequest.getStartTime(),
                        updateCouponRequest.getEndTime(),
                        updateCouponRequest.getRemainingUsage()
                ),
                outputValues -> CouponResponse.from(outputValues.getCoupon())
        );

        return new ResponseEntity<>(ApiResponse.success(coupon, "Update coupon successfully"), HttpStatus.OK);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<ApiResponse> deleteCoupon(
            @PathVariable
            @NotBlank(message = "Coupon code must not be blank")
            @Size(min = 3, max = 10, message = "Coupon code must be between 3 and 10 characters")
            String code) {

        DeleteCouponResponse response = DeleteCouponResponse.success(code);
        return new ResponseEntity<>(ApiResponse.success(response, "Delete coupon successfully"), HttpStatus.OK);
    }

} 