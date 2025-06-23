package vn.zaloppay.couponservice.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.zaloppay.couponservice.data.entities.CouponEntity;

@Repository
public interface JpaCouponRepository extends JpaRepository<CouponEntity, String> {

    @Modifying
    @Query("UPDATE CouponEntity c SET c.remainingUsage = c.remainingUsage - 1 WHERE c.code = :code AND c.remainingUsage > 0")
    int decrementRemainingUsage(@Param("code") String code);

}
