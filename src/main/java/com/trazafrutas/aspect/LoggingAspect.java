package com.trazafrutas.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}
    
    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceMethods() {}
    
    @Around("controllerMethods() || serviceMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        logger.debug("→ Iniciando {}.{}", className, methodName);
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            
            logger.debug("← Completado {}.{} en {}ms", className, methodName, (endTime - startTime));
            return result;
        } catch (Exception e) {
            logger.error("× Error en {}.{}: {}", className, methodName, e.getMessage(), e);
            throw e;
        }
    }
}