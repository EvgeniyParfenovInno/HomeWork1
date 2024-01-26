package ru.demo.test;

import ru.demo.annotation.AfterSuite;
import ru.demo.annotation.AfterTest;
import ru.demo.annotation.BeforeSuite;
import ru.demo.annotation.BeforeTest;
import ru.demo.annotation.CsvSource;
import ru.demo.annotation.Test;

public class TestClass {

    @BeforeTest
    public static void methodBeforeTest() {
        System.out.println("methodBeforeTest();");
    }

    @AfterTest
    public static void methodAfterTest() {
        System.out.println("methodAfterTest();");
    }

    @Test
    public static void methodTestDefault() {
        System.out.println("methodTestDefault();");
    }

    @Test(priority = 3)
    public static void methodTest3() {
        System.out.println("methodTest3();");
    }

    @Test(priority = 7)
    @CsvSource(value = "10, Java, 20, true")
    public static void methodTest7(int a, String b, int c, boolean d) {
        System.out.println("methodTest7();");
        System.out.println("a=" + a);
        System.out.println("b=" + b);
        System.out.println("c=" + c);
        System.out.println("d=" + d);
    }

    @BeforeSuite
    public static void methodBeforeSuite() {
        System.out.println("methodBeforeSuite();");
    }

    @AfterSuite
    public static void methodAfterSuite() {
        System.out.println("methodAfterSuite();");
    }

    private TestClass() {
    }
}
