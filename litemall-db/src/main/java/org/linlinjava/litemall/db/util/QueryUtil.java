package org.linlinjava.litemall.db.util;

import com.google.common.base.CaseFormat;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class QueryUtil {

    @SneakyThrows
    public static <T> T constructExampleInstance(Object t, Class<T> exampleClass) {
        T exampleInstance = exampleClass.newInstance();
        Method orMethod = exampleClass.getDeclaredMethod("or");
        Object criteriaInstance = orMethod.invoke(exampleInstance);
        Class<?> criteriaClass = criteriaInstance.getClass();
        for (Field field : t.getClass().getDeclaredFields()) {
            if (!Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                Object value = field.get(t);
                if (value != null) {
                    String fieldName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, field.getName());
                    Method andEqualToMethod = criteriaClass.getDeclaredMethod("and" + fieldName + "EqualTo", field.getType());
                    andEqualToMethod.invoke(criteriaInstance, value);
                } else if (field.getName().equals("deleted")) {
                    // 如果deleted字段没有设置，默认设置为false
                    String fieldName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, field.getName());
                    Method andEqualToMethod = criteriaClass.getDeclaredMethod("and" + fieldName + "EqualTo", field.getType());
                    andEqualToMethod.invoke(criteriaInstance, false);
                }
                field.setAccessible(accessible);

            }
        }
        return exampleInstance;
    }
}