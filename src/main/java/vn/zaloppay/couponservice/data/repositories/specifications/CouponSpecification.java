package vn.zaloppay.couponservice.data.repositories.specifications;

import org.springframework.data.jpa.domain.Specification;
import vn.zaloppay.couponservice.core.entities.UsageType;
import vn.zaloppay.couponservice.core.entities.discount.DiscountType;
import vn.zaloppay.couponservice.data.entities.CouponEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CouponSpecification {

    private CouponSpecification() {}

    public static Specification<CouponEntity> hasDiscountType(DiscountType discountType) {
        return (root, query, criteriaBuilder) -> {
            if (discountType == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("discountType"), discountType);
        };
    }

    public static Specification<CouponEntity> hasUsageType(UsageType usageType) {
        return (root, query, criteriaBuilder) -> {
            if (usageType == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("usageType"), usageType);
        };
    }

    public static Specification<CouponEntity> isActive(LocalDateTime currentTime) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), currentTime),
                criteriaBuilder.greaterThan(root.get("endTime"), currentTime)
        );
    }

    public static Specification<CouponEntity> hasRemainingUsage() {
        return (root, query, criteriaBuilder) -> 
                criteriaBuilder.greaterThan(root.get("remainingUsage"), 0);
    }

    public static Specification<CouponEntity> meetMinOrderValue(BigDecimal orderAmount) {
        return (root, query, criteriaBuilder) -> {
            if (orderAmount == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("minOrderValue"), orderAmount);
        };
    }

    public static Specification<CouponEntity> isAvailable(BigDecimal orderAmount, LocalDateTime currentTime) {
        return Specification.where(isActive(currentTime))
                .and(hasRemainingUsage())
                .and(meetMinOrderValue(orderAmount));
    }

    public static Specification<CouponEntity> withFilters(DiscountType discountType, UsageType usageType) {
        return Specification.where(hasDiscountType(discountType))
                .and(hasUsageType(usageType));
    }

    public static Specification<CouponEntity> withAvailabilityFilters(BigDecimal orderAmount, 
                                                                      DiscountType discountType, 
                                                                      LocalDateTime currentTime) {
        return Specification.where(isAvailable(orderAmount, currentTime))
                .and(hasDiscountType(discountType));
    }
} 