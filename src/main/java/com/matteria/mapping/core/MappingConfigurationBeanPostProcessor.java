package com.matteria.mapping.core;

import com.matteria.mapping.Mapping;
import com.matteria.mapping.MappingConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Function;

public class MappingConfigurationBeanPostProcessor implements BeanPostProcessor, Ordered {
    private final MappingRegistry registry;

    public MappingConfigurationBeanPostProcessor(MappingRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();

        // Check if the bean's class (or its superclass) is annotated with @MappingConfiguration
        if (isAnnotatedWithMappingConfiguration(beanClass)) {
            processMappingConfigurationBean(bean, beanClass);
        }

        return bean;
    }

    private boolean isAnnotatedWithMappingConfiguration(Class<?> clazz) {
        // Check the class and its interfaces/superclasses for the annotation
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            if (current.isAnnotationPresent(MappingConfiguration.class)) {
                return true;
            }
            current = current.getSuperclass();
        }
        return false;
    }

    private void processMappingConfigurationBean(Object bean, Class<?> beanClass) {
        Method[] methods = getAllDeclaredMethods(beanClass);

        // First pass: Process methods without @Mapping parameters
        for (Method method : methods) {
            if (method.isAnnotationPresent(Mapping.class) && !hasMappingParameterAnnotations(method)) {
                registerMapperMethod(bean, method);
            }
        }

        // Second pass: Process methods with @Mapping parameters (nested mappers)
        // These depend on mappers registered in the first pass
        for (Method method : methods) {
            if (method.isAnnotationPresent(Mapping.class) && hasMappingParameterAnnotations(method)) {
                registerMapperMethod(bean, method);
            }
        }
    }

    private Method[] getAllDeclaredMethods(Class<?> clazz) {
        // Get methods from the actual class, not the proxy
        Class<?> targetClass = clazz;
        while (targetClass.getName().contains("$$")) {
            targetClass = targetClass.getSuperclass();
        }
        return targetClass.getDeclaredMethods();
    }

    private boolean hasMappingParameterAnnotations(Method method) {
        for (var paramAnnotations : method.getParameterAnnotations()) {
            for (var annotation : paramAnnotations) {
                if (annotation instanceof Mapping) {
                    return true;
                }
            }
        }
        return false;
    }

    private void registerMapperMethod(Object bean, Method method) {
        try {
            method.setAccessible(true);

            // Get the mapping key from the annotation
            Mapping mappingAnnotation = method.getAnnotation(Mapping.class);
            String mappingKey = mappingAnnotation.value();

            // Get the generic return type to extract input and output classes
            Type genericReturnType = method.getGenericReturnType();
            if (!(genericReturnType instanceof ParameterizedType)) {
                return; // Skip if return type is not parameterized
            }

            ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
            Type[] typeArgs = parameterizedType.getActualTypeArguments();
            if (typeArgs.length != 2) {
                return; // Skip if not Function<I, O>
            }

            Class<?> inputClass = getClassFromType(typeArgs[0]);
            Class<?> outputClass = getClassFromType(typeArgs[1]);

            // Check if this method has @Mapping parameters - if so, inject them
            Object[] args = resolveMethodParameters(method);

            // Invoke the method to get the mapper function
            Object result = method.invoke(bean, args);

            if (result instanceof Function<?, ?> function) {
                // Register the mapper in the registry
                registry.register(mappingKey, inputClass, outputClass, function);
            }
        } catch (Exception e) {
            // Silently skip methods that fail
        }
    }

    private Object[] resolveMethodParameters(Method method) throws Exception {
        var parameters = method.getParameters();
        var paramAnnotations = method.getParameterAnnotations();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            for (var annotation : paramAnnotations[i]) {
                if (annotation instanceof Mapping mappingAnnotation) {
                    String mappingKey = mappingAnnotation.value();
                    Type paramType = parameters[i].getParameterizedType();

                    if (paramType instanceof ParameterizedType parameterizedType) {
                        Type[] typeArgs = parameterizedType.getActualTypeArguments();
                        if (typeArgs.length == 2) {
                            Class<?> inputClass = getClassFromType(typeArgs[0]);
                            Class<?> outputClass = getClassFromType(typeArgs[1]);

                            // Get the required mapper from the registry
                            Function<?, ?> mapper = registry.get(mappingKey, inputClass, outputClass);
                            if (mapper == null) {
                                throw new IllegalStateException("Required mapper not found: " + mappingKey);
                            }
                            args[i] = mapper;
                        }
                    }
                }
            }
        }

        return args;
    }

    private Class<?> getClassFromType(Type type) {
        if (type instanceof Class<?> clazz) return clazz;
        if (type instanceof ParameterizedType pt) return (Class<?>) pt.getRawType();
        return Object.class;
    }

    @Override
    public int getOrder() {
        // Run after most other post processors but before application is fully started
        return Ordered.LOWEST_PRECEDENCE - 1000;
    }
}
