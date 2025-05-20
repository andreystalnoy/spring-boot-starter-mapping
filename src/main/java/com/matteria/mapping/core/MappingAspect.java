package com.matteria.mapping.core;

import com.matteria.mapping.Mapping;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Function;

@Aspect
public class MappingAspect {
    private final MappingRegistry registry;

    public MappingAspect(MappingRegistry registry) {
        this.registry = registry;
    }

    @Around("@annotation(com.matteria.mapping.Mapping)")
    public Object interceptMapperMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        Type genericReturnType = method.getGenericReturnType();

        if (genericReturnType instanceof ParameterizedType parameterizedType) {
            Type[] typeArgs = parameterizedType.getActualTypeArguments();
            if (typeArgs.length == 2 && result instanceof Function<?, ?> function) {
                Class<?> inputClass = getClassFromType(typeArgs[0]);
                Class<?> outputClass = getClassFromType(typeArgs[1]);
                String mappingKey = method.getAnnotation(Mapping.class).value();
                registry.register(mappingKey, inputClass, outputClass, function);
            }
        }

        return result;
    }

    private Class<?> getClassFromType(Type type) {
        if (type instanceof Class<?> clazz) return clazz;
        if (type instanceof ParameterizedType pt) return (Class<?>) pt.getRawType();
        return Object.class;
    }
}
