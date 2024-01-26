package ru.demo.test;

import ru.demo.annotation.AfterSuite;
import ru.demo.annotation.AfterTest;
import ru.demo.annotation.BeforeSuite;
import ru.demo.annotation.BeforeTest;
import ru.demo.annotation.CsvSource;
import ru.demo.annotation.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class TestRunner {
    public static void runTests(Class c) {
        var methods = c.getDeclaredMethods();

        try {
            checkAnnotationCountLessThen(1, methods, BeforeSuite.class);
            checkAnnotationCountLessThen(1, methods, AfterSuite.class);
            checkAnnotationForStaticMethod(methods, BeforeSuite.class);
            checkAnnotationForStaticMethod(methods, AfterSuite.class);

            if (Arrays.stream(methods).filter(m -> m.isAnnotationPresent(Test.class))
                    .map(m -> m.getAnnotation(Test.class).priority())
                    .anyMatch(v -> v < 1 || v > 10)) {
                throw new RuntimeException("Значение priority в аннотация @Test должно находиться в диапазоне от 1 до 10 ");
            }

            var beforeSuite = Arrays.stream(methods).filter(m -> m.isAnnotationPresent(BeforeSuite.class)).findFirst();
            var afterSuite = Arrays.stream(methods).filter(m -> m.isAnnotationPresent(AfterSuite.class)).findFirst();
            var testMethods = Arrays.stream(methods).filter(m -> m.isAnnotationPresent(Test.class)).collect(Collectors.toList());
            var beforeTestMethods = Arrays.stream(methods).filter(m -> m.isAnnotationPresent(BeforeTest.class)).collect(Collectors.toList());
            var afterTestMethods = Arrays.stream(methods).filter(m -> m.isAnnotationPresent(AfterTest.class)).collect(Collectors.toList());
            testMethods.sort((m1, m2) -> m2.getAnnotation(Test.class).priority() - m1.getAnnotation(Test.class).priority());

            if (beforeSuite.isPresent())
                beforeSuite.get().invoke(null, null);

            for (Method testMethod : testMethods) {
                for (Method beforeTestMethod : beforeTestMethods) {
                    beforeTestMethod.invoke(null, null);
                }
                if (testMethod.isAnnotationPresent(CsvSource.class)) {
                    var paramTypes = testMethod.getParameterTypes();
                    var params = Optional.ofNullable(testMethod.getAnnotation(CsvSource.class).value()).orElse("").split(", ");
                    if (params.length != paramTypes.length)
                        throw new RuntimeException("В аннотации @CsvSource строка должна иметь " + paramTypes.length + " параметра");
                    var arguments = new Object[paramTypes.length];
                    for (int paramIndex = 0; paramIndex < paramTypes.length; paramIndex++) {
                        if (int.class.equals(paramTypes[paramIndex])) {
                            arguments[paramIndex] = Integer.parseInt(params[paramIndex]);
                        } else if (boolean.class.equals(paramTypes[paramIndex])) {
                            arguments[paramIndex] = Boolean.getBoolean(params[paramIndex]);
                        } else {
                            arguments[paramIndex] = params[paramIndex];
                        }
                    }
                    testMethod.invoke(null, arguments);
                } else {
                    testMethod.invoke(null, null);
                }
                for (Method afterTestMethod : afterTestMethods) {
                    afterTestMethod.invoke(null, null);
                }
            }

            if (afterSuite.isPresent())
                afterSuite.get().invoke(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkAnnotationCountLessThen(int count, Method[] methods, Class checkClass) {
        if (Arrays.stream(methods).filter(m -> m.isAnnotationPresent(checkClass)).count() > count)
            throw new RuntimeException("Аннотация @" + checkClass.getSimpleName() + " встречается более " + count + " раз!");
    }

    private static void checkAnnotationForStaticMethod(Method[] methods, Class checkClass) {
        if (Arrays.stream(methods).filter(m -> m.isAnnotationPresent(checkClass))
                .anyMatch(m -> !Modifier.isStatic(m.getModifiers())))
            throw new RuntimeException("Аннотация @" + checkClass.getSimpleName() + " может применяться только к статическим методам");
    }


    private TestRunner() {
    }
}
