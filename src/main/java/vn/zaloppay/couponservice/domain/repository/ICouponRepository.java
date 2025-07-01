package vn.zaloppay.couponservice.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.zaloppay.couponservice.domain.model.Coupon;
import vn.zaloppay.couponservice.domain.model.discount.DiscountType;
import vn.zaloppay.couponservice.domain.model.UsageType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ICouponRepository {

    boolean existsByCode(String code);

    Coupon findByCode(String code);

    Page<Coupon> findAll(DiscountType discountType, UsageType usageType, Pageable pageable);

    List<Coupon> findEligibleCoupons(BigDecimal orderAmount, LocalDateTime currentTime);

    Page<Coupon> findAvailableCoupons(BigDecimal orderAmount, DiscountType discountType, LocalDateTime currentTime, Pageable pageable);

    Coupon save(Coupon coupon);

    Coupon update(Coupon coupon);

    void delete(Coupon coupon);

    boolean decrementRemainingUsage(String code);

}
