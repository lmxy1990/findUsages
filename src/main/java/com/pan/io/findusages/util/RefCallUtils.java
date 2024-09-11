package com.pan.io.findusages.util;

import java.lang.reflect.Method;
import java.util.Objects;

public class RefCallUtils {


    public static Object invokeStaticMethod(Class<?> cls, String methodName, Object... args) {
        Method[] methods = cls.getDeclaredMethods();

        for (Method method : methods) {
            if (!Objects.equals(method.getName(), methodName)) {
                continue;
            }
            Class<?>[] paramTypes = method.getParameterTypes();
            if (args == null ? paramTypes.length != 0 : paramTypes.length != args.length) {
                continue;
            }
            // check param types
            boolean match = checkParamTypes(args, paramTypes);
            if (!match) {
                continue;
            }
            try {
                method.setAccessible(true);
                return method.invoke(null, args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static boolean checkParamTypes(Object[] args, Class<?>[] paramTypes) {
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            Object arg = args[i];
            if (!paramType.isInstance(arg)) {
                return false;
            }
        }
        return true;
    }

    public static Object invokeMethod(Object obj, String methodName, Object... args) {
        Method[] methods = obj.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (!Objects.equals(method.getName(), methodName)) {
                continue;
            }
            Class<?>[] paramTypes = method.getParameterTypes();
            if (args == null ? paramTypes.length != 0 : paramTypes.length != args.length) {
                continue;
            }
            // check param types
            boolean match = checkParamTypes(args, paramTypes);
            if (!match) {
                continue;
            }
            try {
                method.setAccessible(true);
                return method.invoke(obj, args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
