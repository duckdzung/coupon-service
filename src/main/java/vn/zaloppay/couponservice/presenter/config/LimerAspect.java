package vn.zaloppay.couponservice.presenter.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LimerAspect {

    @Pointcut("@within(limer)")
    public void limerClass(Limer limer) {}

    @Around("limerClass(limer)")
    public Object logExecution(ProceedingJoinPoint joinPoint, Limer limer) throws Throwable {
        long start = System.currentTimeMillis();
        String service = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String method = joinPoint.getSignature().getName();

        Object result = joinPoint.proceed();

        long duration = System.currentTimeMillis() - start;

        if (limer.enabledLogLatency()) {
            log.info("{}.{} executed in {}ms", service, method, duration);
        }

        if (limer.enabledLogInOut()) {
            String req = JsonMapper.toJson(joinPoint.getArgs());
            String res = JsonMapper.toJson(result);
            log.info("{}.{} [REQ]: {}, [RES]: {}", service, method, req, res);
        }

        return result;
    }
} 