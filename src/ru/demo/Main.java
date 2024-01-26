package ru.demo;

import ru.demo.test.TestClass;
import ru.demo.test.TestRunner;

public class Main {

    public static void main(String[] args) {
        TestRunner.runTests(TestClass.class);
    }
}
