# How does annotation work in java ? 

In Java, an annotation is a special type of syntax that can be used to provide additional information about a program element, such as a class, method, or variable. Annotations are defined using the @interface keyword and can be applied to various parts of a Java program, such as classes, methods, variables, and packages.


Here is an example of defining and using an annotation in Java:



```java 
// Define the annotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {
  String value() default "";
}

// Use the annotation
public class Example {
  @Test("test1")
  public void testMethod1() {
    // code goes here
  }

  @Test("test2")
  public void testMethod2() {
    // code goes here
  }
}
```

In this example, the @Test annotation is defined to apply to methods and is retained at runtime. The annotation has a single element, value, which has a default value of "". The @Test annotation is then applied to two methods, testMethod1() and testMethod2(), and specifies a different value for each method.

Annotations are useful for providing additional information about a program element, such as metadata, and can be used to automate tasks such as code generation, testing, or documentation. They are a powerful feature of Java that allows you to add custom behavior to your code.