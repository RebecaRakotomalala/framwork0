package mg.itu.prom16;

import java.util.*;
import java.lang.reflect.Method;

public class Mapping {
    private final String key;
    private final Class<?> controllerClass;
    private final Method method;

    public Mapping(String key, Class<?> controllerClass, Method method) {
        this.key = key;
        this.controllerClass = controllerClass;
        this.method = method;
    }

    public String getKey() {
        return key;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public Method getMethod() {
        return method;
    }
}
