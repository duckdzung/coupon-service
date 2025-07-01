package vn.zaloppay.couponservice.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.zaloppay.couponservice.infra.model.CouponEntity;

import java.util.Optional;

@Repository
public interface JpaCouponRepository extends JpaRepository<CouponEntity, Long>, JpaSpecificationExecutor<CouponEntity> {

    boolean existsByCode(String code);
    
    Optional<CouponEntity> findByCode(String code);
    
    void deleteByCode(String code);

    @Modifying
    @Query("UPDATE CouponEntity c SET c.remainingUsage = c.remainingUsage - 1 WHERE c.code = :code AND c.remainingUsage > 0")
    int decrementRemainingUsage(@Param("code") String code);

}
