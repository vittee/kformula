KFormula
==========
#####  [ภาษาไทย](README-th.md)

Mathematical expression engine written in Kotlin, running on JVM. 

[![Travis CI](https://img.shields.io/travis/vittee/kformula)](#)
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
    	<groupId>com.github.vittee.kformula</groupId>
    	<artifactId>kformula</artifactId>
    	<version>1.0.1</version>
    </dependency>
```

Gradle:
```groovy
repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    ...
    implementation 'com.github.vittee.kformula:kformula:1.0.1'
    ...
}
```

Table Of Contents
-----------------
  * [Install](#install)  
  * [Expression](#expression)
    - [Numeric literal](#numeric-literal)
    - [Percentage numeric literal](#percentage-numeric-literal)
    - [Boolean literal](#boolean)
    - [Variable](#variable)
    - [Supported operators](#supported-operators)
    - [Special operators](#special-operators)
        - [IN Range](#in-range)
        - [NOT IN Range](#not-in-range)
        - [Ternary](#ternary)
  * [Usage](#usage)
    - [Simple usage](#simple-usage)   
    - [Adding constant](#adding-constant)
    - [Adding variable](#adding-variable) 
    - [Adding external/dynamic variable](#adding-externaldynamic-variable)
    - [Built-in functions](#built-in-functions)
        - [abs](#abs)
        - [sum](#sum)
        - [average](#average)
        - [floor](#floor)
        - [ceil](#ceil)
        - [round](#round)
        - [min](#min)
        - [max](#max)
        - [clamp](#clamp)
        - [sqrt](#sqrt)
        - [add_percentage](#add_percentage)
        - [subtract_percentage](#subtract_percentage)    
    - [Adding function](#adding-function)
        - [Function without parameter](#function-without-parameter)
        - [Function with parameters](#function-with-parameters)
        - [Default parameters](#default-parameters)
        - [Variadic parameters](#variadic-parameters)
    - [Extending](#extending)
    
Expression
==========
    
Numeric literal
---------------
 Only base-10 decimal is supported, no fancy bin/oct/hex/scientific notations.
 
 All values will be stored as `BigDecimal` instances.
 
Percentage numeric literal
---------------
 KFormula support a special numeric ended with `%` sign.
 
 |  Literal  |  Value    |
 |-----------|-----------|
 |  `100%`   |   1.0     |
 |  `50%`    |   0.5      |
 
Boolean
---------------
KFormula operates the expressions as numbers, so `true` is `1`, `false` is `0` 
  
Variable
-----------------
Variable name must begin with `$` or `%` sign, followed by any unicode characters and underscores/dots.

Example or valid variable names:

| |
| ------------- |
| `$test`  |
| `$2pi`  |
| `%discount`  |
| `$record.value`  |
| `$ตัวแปร`  |
| `$変数`  |

Supported operators
-------------------
| Operation     | Operator      |
| ------------- | ------------- |
| Add           |       `+`     |
| Subtract      |       `-`     |
| Multiply      |       `*`     |
| Divide        |       `/`     |
| Exponent      |       `^`     |
| Modulo        |     `mod`     |
| Logical OR    |     `or`      |
| Logical AND   |    `and`      |
| Logical NOT   | `not` or  `!` |
| Equal         | `=` or `==`   |
| Not Equal     | `!=` or `<>`   |
| Greater than  |      `>`      |
| Less than     |      `<`      |
| Greater than or equal  |      `>=`   |
| Less than or equal     |      `<=`   |

Special Operators
-----------------

##### IN range

Syntax:
```
<expr> in <begin>...<end>
```

Returns `true` if `<expr>` is within the range starting from `<begin>` to `<end>`

It is equivalent to: 
```
(<expr> >= <begin>) and (<expr> <= <end>)
```

Example:
```
5 in 5..20
```
Returns `true`

```
20 in 5..20
```
Returns `true`

```
4 in 5..20     
```
Returns `false`

```
21 in 5..20
```
Returns `false`

---
##### NOT IN range

Syntax:
```
not in <begin>...<end>
!in <begin>...<end>
```

Returns `true` if `<expr>` is **NOT** within the range starting from `<begin>` to `<end>`

It is equivalent to: 
```
(<expr> < <begin>) or (<expr> > <end>)
```

Example:
```
5 not in 5..20
5 !in 5..20
```
Returns `false`

```
20 not in 5..20
20 !in 5..20
```
Returns `false`

```
4 not in 5..20
4 !in 5..20        
```
Returns `true`

```
21 not in 5..20
21 !in 5..20
```
Returns `true`

---
##### Ternary
Syntax #1:
```
if <condition> [then] <true expression> [else <false expression>]
```

Syntax #2 (function call-like):
```
IF(<condition>,<true expression>[,<false expression>])
```

The `<true expression>` will be evaluated only if the `<condition>` is `true`, otherwise `<false expression>` will be evaluated (if provided).

If the `<false expression>` is omitted and the `<condition>` is `false` it will return `0`

**Note**: Both the `<true expression>` and `<false expression>` will be evaluated [lazily](https://en.wikipedia.org/wiki/Lazy_evaluation). 

For the syntax #1, `then` can be omitted, but not recommended as it would reduce readability.

**Example**

Assuming that `$fee1` is `60` and `$fee2` is `30`

*Syntax #1*: 
```
if $weight > 200 then $fee1 else if $weight > 100 then $fee2
```

*Syntax #2*:
```
IF($weight > 200, $fee1, IF($weight > 100, $fee2))
```

*Mixed syntax #1*:
```
IF($weight > 200, $fee1, if $weight > 100 then $fee2)
```

*Mixed syntax #2*:
```
if $weight > 200 then $fee1 else IF($weight > 100, $fee2)
```

Returns `60` if `$weight` is greater than 200

Returns `30` if `$weight` is greater than 100

Returns `0` if `$weight` is less than or equal 100

---

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

Adding constant
--------------
Kotlin:
```kotlin
val fx = Formula().apply {
    addConstant("VALUE", 10)
}
```

Java:
```java
final Formula fx = new Formula();

fx.addConstant("VALUE", 10);
```

Adding variable
-------------

Kotlin:
```kotlin
val fx = Formula().apply {
    addVariable("\$test", 300)
    addVariable("%fifty", 0.5)
}
```

Java:
```java
final Formula fx = new Formula();

fx.addVariable("$test", 300);
fx.addVariable("%fifty", 0.5);
```

Adding external/dynamic variable
--------------------------------
Sometimes you may need to have a variable that its value is retrieved from the application when evaluated.

This could be done by adding a variable as an external variable, so a callback function must be specified instead of adding a fixed value to the variable.

Kotlin:
```kotlin
val fx = Formula().apply { 
    addExternalVariable("\$external") {
        getValue()
    }
```

Java (with Lambda):
```java
final Formula fx = new Formula();

fx.addExternalVariable("$external", s -> BigDecimal.valueOf(getValue()));
```

Built-in functions
------------------
- [abs](#abs)
- [sum](#sum)
- [average](#average)
- [floor](#floor)
- [ceil](#ceil)
- [round](#round)
- [min](#min)
- [max](#max)
- [clamp](#clamp)
- [sqrt](#sqrt)
- [add_percentage](#add_percentage)
- [subtract_percentage](#subtract_percentage)

##### abs
Returns the absolute value.

Syntax:
```
abs(value)
```

Example:
```
abs(-10)
```

Returns `10`

##### sum
Returns the sum of all values.

Syntax:
```
sum(...<values>)
```

Example:
```
sum(1,2,3,4,5)
```

Returns `15`

##### average
Returns the average of all values.

Syntax:
```
average(...<values>)
```

Example:
```
average(1,2,3,4,5)
```

Returns `3`

##### floor
Returns value rounded down to the nearest integer.

Syntax:
```
floor(<value>)
```

Example:
```
floor(5.321)
```

Returns `5`


##### ceil
Returns value rounded up to the nearest integer.

Syntax:
```
ceil(<value>)
```

Example:
```
ceil(5.321)
```

Returns `6`

##### round
Returns value rounded to `precision`.

Syntax:
```
round(<value>, [precision=0])
```

Example:
```
round(5.321)
```

Returns `5`

```
round(5.566)
```

Returns `6`

```
round(5.566, 1)
```

Returns `5.6`

##### min
Returns the minimum value.

Syntax:
```
min(...<values>)
```

Example:
```
min(5,1,4,2,3)
```

Returns `1`

##### max
Returns the maximum value.

Syntax:
```
max(...<values>)
```

Example:
```
max(5,1,4,2,3)
```

Returns `5`

##### clamp
Clamps the value within the inclusive `lower` and `upper` bounds.

Syntax:
```
clamp(<value>, <lower>, <upper>)
```

Example:
```
clamp(5, 10, 20)
```

Returns `5`

```
clamp(25, 10, 20)
```

Returns `20`

```
clamp(15, 10, 20)
```

Returns `15`

##### sqrt
Returns the square root of a number.

Syntax:
```
sqrt(<value>)
```

Example:
```
sqrt(9)
```

Returns `3`

```
sqrt(2)
```

Returns `1.414213562373095`

##### add_percentage
Add a percentage of the value.

Syntax:
```
add_percentage(<value>, <percentage>)
```

Example:
```
add_percentage(500, 0.5)
```

Returns `750`

```
add_percentage(500, 50%)
```

Returns `750`

```
add_percentage(500, +50%)
```

Returns `750`

```
add_percentage(500, -50%)
```

Returns `250`

##### subtract_percentage

Subtract a percentage of the value.

Syntax:
```
subtract_percentage(<value>, <percentage>)
```

Example:
```
subtract_percentage(500, 0.5)
```

Returns `250`

```
subtract_percentage(500, 50%)
```

Returns `250`

```
subtract_percentage(500, +50%)
```

Returns `250`

Example:
```
subtract_percentage(500, -50%)
```

Returns `750`

Adding function
---------------

Function can be added to the `Formula` instance by calling `addFunction` method.

Kotlin `addFunction` method definition:
```kotlin
fun addFunction(name: String, vararg signatures: String, handler: FunctionCallHandler)
```

Java `addFunction` method definition:
```java
public void addFunction(String name, String[] signatures, FunctionCallHandler handler)
```

##### Function without parameter

Kotlin:
```kotlin
val fx = Formula().apply {
    addFunction("one") {
        1.toBigDecimal()
    }
}
```

Java (with Lambda):
```java
final Formula fx = new Formula();

fx.addFunction("one", new String[]{}, args -> {
    return BigDecimal.valueOf(1);
});
```

Expression:
```
one()
```
*Evaluate to `1`*

```
one() + one()
```
*Evaluate to `2`*

##### Function with parameters

To add a function with parameters, just specify a list of parameter names via the `addFunction` method.

The parameter can then be accessed by name via `args` parameter in handler.

Kotlin:
```kotlin
val fx = Formula().apply {
    addFunction("add", "a", "b") { args ->
        args["a"] + args["b"]
    }
}
```
Java (with Lambda):
```java
final Formula fx = new Formula();

fx.addFunction("add", new String[]{"a", "b"}, args -> {
    BigDecimal a = args.get("a").eval();
    BigDecimal b = args.get("b").eval();

    return a.add(b)
});
```

Example:
```
add(1,2)
```

Returns `3`

```
add(1,add(2,4))
```

Returns `7`

##### Default parameters

Parameters can have default values by specifying parameter names using `<name>=<value>` format, e.g. `param2=100`

Kotlin:
```kotlin
val fx = Formula().apply {
    addFunction("add", "a", "b=1") { args ->
        args["a"] + args["b"]
    }
}
```
Java (with Lambda):
```java
final Formula fx = new Formula();

fx.addFunction("add", new String[]{"a", "b=1"}, args -> {
    BigDecimal a = args.get("a").eval();
    BigDecimal b = args.get("b").eval();

    return a.add(b)
});
```

Example:
```
add(1)
```

Returns `2`

```
add(1,2)
```

Returns `3`

##### Variadic parameters

Sometimes, the number of parameters is unknown, you can prefix the parameter name with `...` to make it variadic. (Also known as Rest parameters)

Kotlin:
```kotlin
val fx = Formula().apply {
    addFunction("accumulate", "init", "...all") { args ->
        val all = args["all"].rest.eval()
        args["init"] + all.reduce { sum, v -> sum.add(v) }
    }
}
```
Java (with Lambda):
```java
final Formula fx = new Formula();

fx.addFunction("accumulate", new String[]{"init", "...all"}, args -> {
    BigDecimal init = args.get("init").eval();

    List<Expr> exprs = args.get("all").getRest();

    return exprs.stream().map(Expr::eval).reduce(init, (sum, v) -> sum.add(v));
});
```


Extending
=========
TBD