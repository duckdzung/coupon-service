package vn.zaloppay.couponservice.data.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.zaloppay.couponservice.core.entities.Coupon;
import vn.zaloppay.couponservice.core.entities.UsageType;
import vn.zaloppay.couponservice.core.entities.discount.DiscountType;
import vn.zaloppay.couponservice.core.repositories.ICouponRepository;
import vn.zaloppay.couponservice.data.entities.CouponEntity;
import vn.zaloppay.couponservice.data.repositories.specifications.CouponSpecification;
import vn.zaloppay.couponservice.presenter.config.logging.Limer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Limer(enabledLogLatency = true)
public class CouponRepository implements ICouponRepository {

    private final JpaCouponRepository jpaCouponRepository;

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
    public Page<Coupon> findAll(DiscountType discountType, UsageType usageType, Pageable pageable) {
        Specification<CouponEntity> spec = CouponSpecification.withFilters(discountType, usageType);
        Page<CouponEntity> entityPage = jpaCouponRepository.findAll(spec, pageable);
        return entityPage.map(this::toDomainObject);
    }

    @Override
    public List<Coupon> findEligibleCoupons(BigDecimal orderAmount, LocalDateTime currentTime) {
        Specification<CouponEntity> spec = CouponSpecification.isAvailable(orderAmount, currentTime);
        List<CouponEntity> entities = jpaCouponRepository.findAll(spec);
        return entities.stream()
                .map(this::toDomainObject)
                .toList();
    }

    @Override
    public Page<Coupon> findAvailableCoupons(BigDecimal orderAmount, DiscountType discountType, LocalDateTime currentTime, Pageable pageable) {
        Specification<CouponEntity> spec = CouponSpecification.withAvailabilityFilters(orderAmount, discountType, currentTime);
        Page<CouponEntity> entityPage = jpaCouponRepository.findAll(spec, pageable);
        return entityPage.map(this::toDomainObject);
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
