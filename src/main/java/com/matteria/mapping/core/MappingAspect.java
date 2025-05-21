package com.matteria.mapping.core;

import com.matteria.mapping.Mapping;
import com.matteria.mapping.MappingException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Function;

@Aspect
public class MappingAspect {
    private final MappingRegistry registry;

    public MappingAspect(MappingRegistry registry) {
        this.registry = registry;
    }

    @Around("@annotation(com.matteria.mapping.Mapping) || execution(* *(.., @com.matteria.mapping.Mapping (*), ..))")
    public Object interceptMapperMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Object[] args = joinPoint.getArgs();
        Object[] processedArgs = injectMappersIntoParameters(method, args);

        Object result = joinPoint.proceed(processedArgs);

        // Register the returned mapper if method has @Mapping annotation
        if (method.isAnnotationPresent(Mapping.class)) {
            registerMapper(method, result);
        }

        return result;
    }

    public Object[] injectMappersIntoParameters(Method method, Object[] args) {
        Parameter[] parameters = method.getParameters();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] processedArgs = args.clone();

        for (int i = 0; i < parameters.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof Mapping mappingAnnotation) {
                    String mappingKey = mappingAnnotation.value();
                    Type paramType = parameters[i].getParameterizedType();
                    if (paramType instanceof ParameterizedType parameterizedType) {
                        Type[] typeArgs = parameterizedType.getActualTypeArguments();
                        if (typeArgs.length == 2) {
                            Class<?> inputClass = getClassFromType(typeArgs[0]);
                            Class<?> outputClass = getClassFromType(typeArgs[1]);

                            Function<?, ?> mapper = registry.get(mappingKey, inputClass, outputClass);
                            if (mapper == null) {
                                throw new MappingException("No mapper found for " + inputClass.getSimpleName() +
                                        " to " + outputClass.getSimpleName() + " with key '" + mappingKey + "'");
                            }
                            processedArgs[i] = mapper;
                        }
                    }
                }
            }
        }
        return processedArgs;
    }

    private void registerMapper(Method method, Object result) {
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
    }

    private Class<?> getClassFromType(Type type) {
        if (type instanceof Class<?> clazz) return clazz;
        if (type instanceof ParameterizedType pt) return (Class<?>) pt.getRawType();
        return Object.class;
    }
}
