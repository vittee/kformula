# KFormula
#####  [English](README.md)

Engine สำหรับอ่านสูตรคณิตศาสตร์ เขียนด้วย Kotlin, ทำงานบน JVM 

[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)
[![Travis CI](https://img.shields.io/travis/vittee/kformula)]()
[![Kotlin 1.3.60](https://img.shields.io/badge/kotlin-1.3.60-blue)](http://kotlinlang.org)
[![MIT License](https://img.shields.io/github/license/vittee/kformula)](https://github.com/vittee/kformula/blob/master/LICENSE)


ใช้ KFormula เพื่ออ่านสูตรคณิตศาสตร์ใน String　และคำนวณเพื่อหาผลลัพธ์

คุณสามารถเพิ่มตัวแปรและค่าคงที่ ยังไม่พอ ยังสามารถเพิ่มฟังก์ชันให้มันได้ด้วย

ด้วยวิธีนี้ จะทำให้ application ของคุณสามารถอ่านสูตรคณิตศาสตร์ที่ user เป็นคนใส่เข้ามา หรือแม้กระทั่งเก็บสูตรเอาไว้ในฐานข้อมูล เมื่อใดก็ตามหากต้องการเปลี่ยนวิธีการคำนวณ ก็เพียงแค่เปลี่ยนสูตรได้เลย โดยไม่ต้องคอมไพล์ใหม่

การติดตั้ง
-------

Gradle:
```groovy
repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    ...
    implementation 'com.github.vittee.kformula:kformula:1.0.3'
    ...
}
```

สารบัญ
-----------------
  * [การติดตั้ง](#การติดตั้ง)  
  * [Expression](#expression)
    - [ตัวเลข](#ตัวเลข)
    - [ตัวเลขร้อยละ](#ตัวเลขร้อยละ)
    - [Boolean literal](#boolean)
    - [ตัวดำเนินการ](#ตัวดำเนินการ)
    - [ตัวดำเนินการพิเศษ](#ตัวดำเนินการพิเศษ)
        - [IN Range](#in-range)
        - [NOT IN Range](#not-in-range)
        - [Ternary](#ไตรภาค-ternary)
  * [การใช้งาน](#การใช้งาน)
    - [การใช้งานทั่วไป](#การใช้งานทั่วไป)   
    - [การเพิ่มค่าคงที่](#การเพิ่มค่าคงที่)
    - [การเพิ่มตัวแปร](#การเพิ่มตัวแปร) 
    - [การเพิ่มตัวแปรภายนอก](#การเพิ่มตัวแปรภายนอก)
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
    - [การเพิ่มฟังก์ชัน](#การเพิ่มฟังก์ชัน)
        - [ฟังก์ชันทื่ไม่มี parameter](#ฟังก์ชันทื่ไม่มี-parameter)
        - [ฟังก์ชันทื่มี parameter](#ฟังก์ชันทื่มี-parameter)
        - [ค่าปริยาย สำหรับ parameters](#ค่าปริยาย-สำหรับ-parameters)
        - [Variadic parameters](#variadic-parameters)
    - [Extending](#extending)
    
Expression
==========
    
ตัวเลข
---------------
ตัวเลขเขียนในรูปแบบเลขฐาน 10 เท่านั้น
 
ค่าตัวเลขทุกค่าจะถูกเก็บเป็น instance ของ class `BigDecimal`
 
ตัวเลขร้อยละ
---------------
สามารถเขียนตัวเลขให้อยู่ในรูปแบบ ร้อยละ โดยใส่เครื่องหมาย `%` ตามหลังตัวเลข
 
 |  รูปแบบ  |  ค่า    |
 |-----------|-----------|
 |  `100%`   |   1.0     |
 |  `50%`    |   0.5      |
 
ตัวเลขร้อยละ สามารถนำไปใช้คำนวณร้อยละได้ [การดำเนินการเลขร้อยละ](#การดำเนินการเลขร้อยละ)
 
Boolean
---------------
เนื่องจากการทำงานภายใน จะเป็นการกระทำเชิงตัวเลขทั้งหมด ดังนั้น `true` มีค่าเป็น `1` และ `false` มีค่าเป็น `0` 
  
ตัวแปร
-----------------
ชื่อตัวแปรจะต้องขึ้นต้นด้วยสัญลักษณ์ `$` หรือ `%` และตามด้วยตัวอักษรใดๆ รวมทั้ง `_` และ `.`

ตัวอย่างชื่อตัวแปร:

| |
| ------------- |
| `$test`  |
| `$2pi`  |
| `%discount`  |
| `$record.value`  |
| `$ตัวแปร`  |
| `$変数`  |
-----------------

ตัวแปรที่มีชื่อขึ้นต้นด้วยสัญลักษณ์ `%` สามารถนำไปใช้คำนวณร้อยละได้ [การดำเนินการเลขร้อยละ](#การดำเนินการเลขร้อยละ)

ตัวดำเนินการ
-------------------
| ชื่อ     | สัญลักษณ์      |
| ------------- | ------------- |
| บวก           |       `+`     |
| ลบ      |       `-`     |
| คูณ      |       `*`     |
| หาร        |       `/`     |
| ยกกำลัง      |       `^`     |
| หารเอาเฉพาะเศษ       |     `mod`     |
| หรือ    |     `or`      |
| และ   |    `and`      |
| นิเสธ   | `not` or  `!` |
| เท่ากับ         | `=` or `==`   |
| ไม่เท่ากับ     | `!=` or `<>`   |
| มากกว่า  |      `>`      |
| น้อยกว่า     |      `<`      |
| มากกว่าหรือเท่ากับ  |      `>=`   |
| น้อยกว่าหรือเท่ากับ     |      `<=`   |

ตัวดำเนินการพิเศษ
-----------------

##### IN range

Syntax:
```
<expr> in <เริ่ม>...<สิ้นสุด>
```

คืนค่า `true` ถ้า `<expr>` มีค่าตั้งแต่ `<เริ่ม>` จนถึง `<สิ้นสุด>`

มีผลเช่นเดียวกันกับ: 
```
(<expr> >= <เริ่ม>) and (<expr> <= <สิ้นสุด>)
```

ตัวอย่าง:
```
5 in 5..20
```
คืนค่า `true`

```
20 in 5..20
```
คืนค่า `true`

```
4 in 5..20     
```
คืนค่า `false`

```
21 in 5..20
```
คืนค่า `false`

---
##### NOT IN range

Syntax:
```
not in <begin>...<end>
!in <begin>...<end>
```

คืนค่า `true` ถ้า `<expr>` **ไม่ได้** มีค่าอยู่ในช่วงตั้งแต่ `<เริ่มต้น>` จนถึง `<สิ้นสุด>`

มีผลเช่นเดียวกันกับ: 
```
(<expr> < <begin>) or (<expr> > <end>)
```

ตัวอย่าง:
```
5 not in 5..20
5 !in 5..20
```
คืนค่า `false`

```
20 not in 5..20
20 !in 5..20
```
คืนค่า `false`

```
4 not in 5..20
4 !in 5..20        
```
คืนค่า `true`

```
21 not in 5..20
21 !in 5..20
```
คืนค่า `true`

##### IN set
Syntax:
```
<expr> in [<elements>]
```
คืนค่า `true` ถ้า `<expr>` เป็นสมาชิกของ set ที่กำหนดโดย `<elements>`

ตัวอย่าง:
```
5 in [5,10,15,20]
```
คืนค่า `true`

```
20 in [5,10,15,20]
```
คืนค่า `true`

```
4 in [5,10,15,20]     
```
คืนค่า `false`

```
12 in [5,10,15,20]     
```
คืนค่า `false`

```
21 in [5,10,15,20]     
```
คืนค่า `false`

---

##### NOT IN set

Syntax:
```
not in [<elements>]
!in [<elements>]
```

คืนค่า `true` ถ้า `<expr>` **ไม่ได้เป็น**สมาชิกของ set ที่กำหนดโดย `<elements>`

ตัวอย่าง:
```
5 not in [5,10,15,20]
5 !in [5,10,15,20]
```
คืนค่า `false`

```
20 not in [5,10,15,20]
20 !in [5,10,15,20]
```
คืนค่า `false`

```
4 not [5,10,15,20]
4 !in [5,10,15,20]      
```
คืนค่า `true`

```
12 not [5,10,15,20]
12 !in [5,10,15,20]      
```
คืนค่า `true`

```
21 not [5,10,15,20]
21 !in [5,10,15,20]
```
คืนค่า `true`


---
##### ไตรภาค (Ternary)
Syntax #1:
```
if <condition> [then] <true expression> [else <false expression>]
```

Syntax #2 (เหมือนเรียกใช้ฟังก์ชัน):
```
IF(<condition>,<true expression>[,<false expression>])
```

`<true expression>` จะถูกเรียกหาค่า เมื่อ `<condition>` เป็น `true`, หากไม่ใช่ จะเรียกหาค่าของ `<false expression>` แทน (ถ้ามี)

ถ้าหากไม่ได้ระบุ `<false expression>` เอาไว้ และ `<condition>` มีค่าเป็น `false` จะคืนค่าเป็น `0`

**Note**: ทั้ง `<true expression>` และ `<false expression>` จะถูกประเมินค่าแบบ [lazy](https://en.wikipedia.org/wiki/Lazy_evaluation). 

สำหรับ syntax #1, สามารถละเว้นการเขียน `then` ได้ แต่ไม่แนะนำ เพราะจะทำให้อ่านยาก

**ตัวอย่าง**

สมมติให้ `$fee1` มีค่าเป็น `60` และ `$fee2` มีค่าเป็น `30`

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

คืนค่า `60` ถ้า `$weight` มีค่ามากกว่า 200

คืนค่า `30` ถ้า `$weight` มีค่ามากกว่า 100

คืนค่า `0` ถ้า `$weight` มีค่าน้อยกว่าหรือเท่ากับ 100

---

การดำเนินการเลขร้อยละ
---------------------
ตัวแปรที่มีชื่อขึ้นต้นด้วยสัญลักษณ์ `%`  รวมทั้งตัวเลขที่ลงท้ายด้วยสัญลักษณ์ `%` มีความหมายว่าเป็นค่าร้อยละ และสามารถนำไปใช้คิดคำนวณ *เพิ่ม* หรือ *ลด* กับตัวเลขอื่น ๆ ได้

เฉพาะการดำเนินการ `+` และ `-` เท่านั้นที่มีผลต่อค่าร้อยละที่อยู่ทางด้านขวามือ, ตัวอย่างเช่น:

| Expression | Result |
|------------|--------|
| 30 + 50%   |   45   |
| 400 - 50%  |  200   |
| 120 + %fifty |  180 | 
| 400 - %discount |  300 |

*สมมติให้ตัวแปร `%fifty` มีค่าเป็น `0.5` และ `%discount` มีค่าเป็น `0.25`* 

**Note:** การดำเนินการอื่นๆ นอกเหนือจากนี้ ที่กระทำกับค่าร้อยละ จะเป็นพียงการดำเนินการทางคณิตศาสตร์ตามปกติเท่านั้น และผลลัพธ์ที่ได้ก็จะยังคงเป็นค่าร้อยละอยู่เช่นเดิม

การใช้งาน
=====

การใช้งานทั่วไป
-------------
วิธีการใช้งานโดยทั่วไป จะเรียกผ่าน class `Formula` ซึ่งมีฟังก์ชันพื้นฐานพร้อมใช้งานแล้ว

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

การเพิ่มค่าคงที่
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

การเพิ่มตัวแปร
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

การเพิ่มตัวแปรภายนอก
--------------------------------
ในบางครั้ง อาจจะมีความจำเป็นจะต้องดึงค่าของตัวแปรมาจากแหล่งอื่น ในจังหวะตอนที่มีการเรียกหาค่า 
ตัวแปรชนิดนี้เรียกว่าตัวแปรภายนอก ซึ่งแทนที่จะกำหนดค่าตายตัวให้กับตัวแปร ก็จะเป็นการกำหนด callback function ที่จะถูกเรียกใช้ในภายหลัง

Kotlin:
```kotlin
val fx = Formula().apply { 
    addExternalVariable("\$external") {
        getValue().toBigDecimal()
    }
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
คืนค่าสัมบูรณ์

Syntax:
```
abs(value)
```

ตัวอย่าง:
```
abs(-10)
```

คืนค่า `10`

##### sum
ค่าค่าผลรวมของทุกค่า

Syntax:
```
sum(...<values>)
```

ตัวอย่าง:
```
sum(1,2,3,4,5)
```

คืนค่า `15`

##### average
คืนค่าเฉลี่ย

Syntax:
```
average(...<values>)
```

ตัวอย่าง:
```
average(1,2,3,4,5)
```

คืนค่า `3`

##### floor
ปัดเศษลง

Syntax:
```
floor(<value>)
```

ตัวอย่าง:
```
floor(5.321)
```

คืนค่า `5`


##### ceil
ปัดเศษขึ้น

Syntax:
```
ceil(<value>)
```

ตัวอย่าง:
```
ceil(5.321)
```

คืนค่า `6`

##### round
ปัดเศษ ให้เหลือทศนิยมตามที่ระบุไว้โดย `precision`.

Syntax:
```
round(<value>, [precision=0])
```

ตัวอย่าง:
```
round(5.321)
```

คืนค่า `5`

```
round(5.566)
```

คืนค่า `6`

```
round(5.566, 1)
```

คืนค่า `5.6`

##### min
หาค่าต่ำสุด

Syntax:
```
min(...<values>)
```

ตัวอย่าง:
```
min(5,1,4,2,3)
```

คืนค่า `1`

##### max
หาค่าสูงสุด

Syntax:
```
max(...<values>)
```

ตัวอย่าง:
```
max(5,1,4,2,3)
```

คืนค่า `5`

##### clamp
ทำให้ค่าที่กำหนด ให้อยู่ในช่วงตั้งแต่`lower` จนถึง `upper` bounds.

Syntax:
```
clamp(<value>, <lower>, <upper>)
```

ตัวอย่าง:
```
clamp(5, 10, 20)
```

คืนค่า `5`

```
clamp(25, 10, 20)
```

คืนค่า `20`

```
clamp(15, 10, 20)
```

คืนค่า `15`

##### sqrt
หาค่ารากที่สอง

Syntax:
```
sqrt(<value>)
```

ตัวอย่าง:
```
sqrt(9)
```

คืนค่า `3`

```
sqrt(2)
```

คืนค่า `1.414213562373095`

##### add_percentage
เพิ่มร้อยละให้กับ `value`

Syntax:
```
add_percentage(<value>, <percentage>)
```

ตัวอย่าง:
```
add_percentage(500, 0.5)
```

คืนค่า `750`

```
add_percentage(500, 50%)
```

คืนค่า `750`

```
add_percentage(500, +50%)
```

คืนค่า `750`

```
add_percentage(500, -50%)
```

คืนค่า `250`

##### subtract_percentage

หักร้อยละออกจาก `value`

Syntax:
```
subtract_percentage(<value>, <percentage>)
```

ตัวอย่าง:
```
subtract_percentage(500, 0.5)
```

คืนค่า `250`

```
subtract_percentage(500, 50%)
```

คืนค่า `250`

```
subtract_percentage(500, +50%)
```

คืนค่า `250`

```
subtract_percentage(500, -50%)
```

คืนค่า `750`

การเพิ่มฟังก์ชัน
---------------

สามารถเพิ่มฟังก์ชันให้กับ instance ของ `Formula` โดยการเรียก method `addFunction`

Kotlin `addFunction` method definition:
```kotlin
fun addFunction(name: String, vararg signatures: String, handler: FunctionCallHandler)
```

Java `addFunction` method definition:
```java
public void addFunction(String name, String[] signatures, FunctionCallHandler handler)
```

##### ฟังก์ชันทื่ไม่มี parameter

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
*คืนค่า `1`*

```
one() + one()
```
*คืนค่า `2`*

##### ฟังก์ชันทื่มี parameter

การเพิ่มฟังก์ชันที่มี parameter สามารถทำได้โดยการระบุรายชื่อของ parameter ในตอนเรียก method `addFunction`

จากนั้นใน handler เรียกหา parameter ที่ระบุไว้ โดยผ่านทาง`args`

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

ตัวอย่าง:
```
add(1,2)
```

คืนค่า `3`

```
add(1,add(2,4))
```

คืนค่า `7`

##### ค่าปริยาย สำหรับ parameters

Parameter สามารถมีค่าปริยายได้ โดยการกำหนดชื่อ parameter ในรูปแบบ `<ชื่อ>=<ค่า>` ตัวอย่างเช่น `param2=100`

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

ตัวอย่าง:
```
add(1)
```

คืนค่า `2`

```
add(1,2)
```

คืนค่า `3`

##### Variadic parameters
บางครั้ง จำนวน parameter ก็ไม่มีจำนวนที่แน่นอน เพื่อให้ parameter สามารถรับค่าได้หลาย ๆ ค่า จะต้องใส่เครื่องหมาย `...` ไว้ด้านหน้าของชื่อ parameter

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