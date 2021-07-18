package de.atos.solumversion.components;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class TelemetryLogger {

    @Around("execution(* de.atos.solumversion..*.* (..))")
    public Object logBeforeAndAfterServiceMethods(ProceedingJoinPoint pjp) throws Throwable {
        log.debug("{} start", pjp.getSignature());
        Object result = pjp.proceed();
        log.debug("{} end", pjp.getSignature());
        return result;
    }
}
