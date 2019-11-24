KFormula
==========
##### [ภาษาไทย](README-th.md)

Mathematical expression engine written in Kotlin, running on JVM. 

[![Kotlin 1.3.50](https://img.shields.io/badge/kotlin-1.3.50-blue)](http://kotlinlang.org)
[![MIT License](https://img.shields.io/github/license/vittee/kformula)](https://github.com/vittee/kformula/blob/master/LICENSE)


With KFormula, you can parse simple mathematical expression text and get the evaluated result.

You can add variables/constants, or beyond that, you can define your own function and expose the logic to the engine.

This way you can make your application be able to accept mathematical expression from user's input, or even from database. If the calculation should be made, simple change the expression to get the new calculation logic apply to your application with recompiling!

Install
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

Table Of Contents
-----------------
  * [Install](#install)  
  * [Usage](#usage)

Usage
=====

Simple usage
-------------
The simplest way is to use `Formula` class, it has built-in functions ready for use.

Kotlin:
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

