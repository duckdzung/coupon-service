package vn.zaloppay.couponservice.core.repositories;

import vn.zaloppay.couponservice.core.entities.Coupon;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ICouponRepository {

    boolean existsByCode(String code);

    Coupon findByCode(String code);

    List<Coupon> findAll();

    List<Coupon> findEligibleCoupons(BigDecimal orderAmount, LocalDateTime currentTime);

    Coupon save(Coupon coupon);

    Coupon update(Coupon coupon);

    void delete(Coupon coupon);

    boolean decrementRemainingUsage(String code);

}
