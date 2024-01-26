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
            var csvSourceMethods = Arrays.stream(methods).filter(m -> m.isAnnotationPresent(CsvSource.class)).collect(Collectors.toList());
            testMethods.sort((m1, m2) -> m2.getAnnotation(Test.class).priority() - m1.getAnnotation(Test.class).priority());

            if (beforeSuite.isPresent())
                beforeSuite.get().invoke(c, null);

            for (Method testMethod : testMethods) {
                for (Method beforeTestMethod : beforeTestMethods) {
                    beforeTestMethod.invoke(c, null);
                }
                testMethod.invoke(c, null);
                for (Method afterTestMethod : afterTestMethods) {
                    afterTestMethod.invoke(c, null);
                }
            }

            if (afterSuite.isPresent())
                afterSuite.get().invoke(c, null);

            for (Method method : csvSourceMethods) {
                var params = Optional.ofNullable(method.getAnnotation(CsvSource.class).value()).orElse("").split(", ");
                if (params.length != 4)
                    throw new RuntimeException("В аннотации @CsvSource строка должна иметь 4 параметра");
                int a_ = Integer.parseInt(params[0]);
                String b_ = params[1];
                int c_ = Integer.parseInt(params[2]);
                boolean d_ = Boolean.getBoolean(params[3]);
                method.invoke(c, a_, b_, c_, d_);
            }
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
