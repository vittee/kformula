# KFormula
##### [English](README.md)

Engine อ่านสูตรคณิตศาสตร์ เขียนด้วย Kotlin, ทำงานบน JVM 

[![Kotlin 1.3.50](https://img.shields.io/badge/kotlin-1.3.50-blue)](http://kotlinlang.org)
[![MIT License](https://img.shields.io/github/license/vittee/kformula)](https://github.com/vittee/kformula/blob/master/LICENSE)


With KFormula, you can parse simple mathematical expression text and get the evaluated result.

You can add variables/constants, or beyond that, you can define your own function and expose the logic to the engine.

This way you can make your application be able to accept mathematical expression from user's input, or even from database. If the calculation should be made, simple change the expression to get the new calculation logic apply to your application with recompiling!

ติดตั้ง
-------

Maven:
```xml
    <dependency>
    	<groupId>xxx</groupId>
    	<artifactId>xxx</artifactId>
    	<version>0.0.1</version>
    </dependency>
```

Gradle:
```groovy
    compile 'xxx.xxx:0.0.1'
```

สารบัญ
-----------------
  * [ติดตั้ง](#ติดตั้ง)  
  * [การใช้งาน](#การใช้งาน)

การใช้งาน
=====

อย่างง่าย

-------------
วิธีที่ง่ายที่สุด คือการใช้ class `Formula`, ซึ่งมี function พื้นฐานพร้อมใช้งานแล้ว.

```kotlin
val code = "1+1"
val fx = Formula()
val program = fx.compile(code)
val result = program.eval()
println("result is $result")
```

Java:
```java
final String code = "1+1";
final Formula fx = new Formula();
final RootExpr program = fx.compile(code);
final BigDecimal result = program.eval();
System.out.println("result is " + result.toPlainString())
```
