package vn.zaloppay.couponservice.app.config.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Limer {

    boolean enabledLogInOut() default false;

    boolean enabledLogLatency() default false;

} 