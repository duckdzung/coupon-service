package vn.zaloppay.couponservice.domain.model.discount;

import lombok.Getter;

@Getter
public enum DiscountType {
    PERCENT(new PercentDiscountStrategy()),
    FIXED(new FixedDiscountStrategy());
    
    private final DiscountStrategy strategy;
    
    DiscountType(DiscountStrategy strategy) {
        this.strategy = strategy;
    }
}
