package vn.zaloppay.couponservice.data.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.zaloppay.couponservice.core.entities.Coupon;
import vn.zaloppay.couponservice.core.repositories.ICouponRepository;
import vn.zaloppay.couponservice.data.entities.CouponEntity;
import vn.zaloppay.couponservice.presenter.config.Limer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Limer(enabledLogLatency = true)
public class CouponRepository implements ICouponRepository {

    private final JpaCouponRepository jpaCouponRepository;

    public CouponRepository(JpaCouponRepository jpaCouponRepository) {
        this.jpaCouponRepository = jpaCouponRepository;
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaCouponRepository.existsById(code);
    }

    @Override
    public Coupon findByCode(String code) {
        Optional<CouponEntity> couponEntity = jpaCouponRepository.findById(code);
        return couponEntity.map(this::toDomainObject).orElse(null);
    }

    @Override
    public List<Coupon> findAll() {
        List<CouponEntity> couponEntities = jpaCouponRepository.findAll();
        return couponEntities.stream()
                .map(this::toDomainObject)
                .toList();
    }

    @Override
    public List<Coupon> findEligibleCoupons(BigDecimal orderAmount, LocalDateTime currentTime) {
        return findAll().stream()
                .filter(coupon -> isEligibleForAutoApply(coupon, orderAmount, currentTime))
                .toList();
    }

    @Override
    public Coupon save(Coupon coupon) {
        CouponEntity couponEntity = toEntity(coupon);
        CouponEntity savedEntity = jpaCouponRepository.save(couponEntity);
        return toDomainObject(savedEntity);
    }

    @Override
    public Coupon update(Coupon coupon) {
        CouponEntity couponEntity = toEntity(coupon);
        CouponEntity updatedEntity = jpaCouponRepository.save(couponEntity);
        return toDomainObject(updatedEntity);
    }

    @Override
    public void delete(Coupon coupon) {
        jpaCouponRepository.deleteById(coupon.getCode());
    }

    @Override
    @Transactional
    public boolean decrementRemainingUsage(String code) {
        int updated = jpaCouponRepository.decrementRemainingUsage(code);
        return updated > 0;
    }

    private boolean isEligibleForAutoApply(Coupon coupon, BigDecimal orderAmount, LocalDateTime currentTime) {
        return coupon.getStartTime().isBefore(currentTime) && // Coupon is active
                coupon.getEndTime().isAfter(currentTime) && // Coupon is not expired
                coupon.getRemainingUsage() > 0 && // Has remaining usage
                coupon.getMinOrderValue().compareTo(orderAmount) <= 0; // Meets minimum order value
    }

    private Coupon toDomainObject(CouponEntity entity) {
        return new Coupon(
                entity.getCode(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getDiscountType(),
                entity.getUsageType(),
                entity.getDiscountValue(),
                entity.getMaxDiscountAmount(),
                entity.getMinOrderValue(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getRemainingUsage()
        );
    }

    private CouponEntity toEntity(Coupon coupon) {
        return CouponEntity.builder()
                .code(coupon.getCode())
                .title(coupon.getTitle())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .usageType(coupon.getUsageType())
                .discountValue(coupon.getDiscountValue())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .minOrderValue(coupon.getMinOrderValue())
                .startTime(coupon.getStartTime())
                .endTime(coupon.getEndTime())
                .remainingUsage(coupon.getRemainingUsage())
                .build();
    }

}
