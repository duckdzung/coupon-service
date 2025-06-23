package vn.zaloppay.couponservice.core.entities.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal calculateDiscount(BigDecimal orderAmount, BigDecimal discountValue, BigDecimal maxDiscountAmount) {
        BigDecimal percentDiscount = orderAmount
                .multiply(discountValue)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        // Apply max discount limit if specified
        if (maxDiscountAmount != null && percentDiscount.compareTo(maxDiscountAmount) > 0) {
            return maxDiscountAmount;
        }
        
        return percentDiscount;
    }
} 