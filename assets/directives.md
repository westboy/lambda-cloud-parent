# IntelliJ IDEA 常用 `//noinspection` inspectionId 列表

> 可以在代码前加 `//noinspection <inspectionId>` 屏蔽对应检查

---

## 1. 代码风格 / 规范

| inspectionId | 描述 |
|--------------|------|
| `UnusedDeclaration` | 未使用的变量、方法、类 |
| `UnusedAssignment` | 未使用的赋值 |
| `ConstantConditions` | 条件总是 true/false |
| `SimplifiableIfStatement` | if 可简化 |
| `MagicNumber` | 魔法数字 |
| `DuplicatedCode` | 重复代码 |
| `EqualsBetweenInconvertibleTypes` | 不可比较类型 equals |
| `SuspiciousMethodCalls` | 可疑方法调用 |
| `RawUseOfParameterizedType` | 泛型原始类型使用 |
| `unchecked` | 未检查的类型转换 |
| `RedundantThrows` | 冗余 throws 声明 |
| `RedundantCast` | 多余的类型转换 |
| `AssignmentToNull` | 给变量赋 null |
| `LocalVariableHidesMemberVariable` | 局部变量隐藏成员变量 |
| `FieldCanBeLocal` | 字段可以改成局部变量 |
| `MethodCanBeStatic` | 方法可改成静态 |
| `UnusedReturnValue` | 未使用返回值 |
| `OverlyComplexMethod` | 方法复杂度过高 |
| `NonConstantStringShouldBeStringBuffer` | 字符串拼接建议使用 StringBuilder |
| `SuspiciousNameCombination` | 可疑变量名组合（如 x 与 y 混用） |

---

## 2. Java / 语法检查

| inspectionId | 描述 |
|--------------|------|
| `BridgeMethod` | 桥方法问题 |
| `ClassReferencesSubclass` | 类引用子类 |
| `NullableProblems` | 可空性问题 |
| `InfiniteLoopStatement` | 无限循环语句 |
| `TailRecursion` | 尾递归警告 |
| `ParameterNameDiffersFromOverriddenMethod` | 参数名不同于重写方法 |
| `StaticFieldReferencedViaSubclass` | 通过子类引用静态字段 |
| `PackageAccessibility` | 包可见性问题 |
| `DefaultAnnotationParam` | 注解默认值缺失 |
| `FieldNameHidesMethodParameter` | 字段名隐藏方法参数 |

---

## 3. 测试 / 调试

| inspectionId | 描述 |
|--------------|------|
| `JUnitMalformedDeclaration` | JUnit 方法声明不正确 |
| `JUnitTestMethodWithParameters` | JUnit 测试方法不应带参数 |
| `TestClassWithoutTestMethods` | 测试类没有测试方法 |
| `DeprecatedJUnitTest` | 使用已废弃的 JUnit 注解 |
| `AssertEqualsReplaceableByAssertTrue` | assertEquals 可用 assertTrue 替代 |

---

## 4. 性能 / 内存

| inspectionId | 描述 |
|--------------|------|
| `MethodCallInLoopCondition` | 循环条件中调用方法 |
| `StringBufferWithoutInitialCapacity` | StringBuffer 没有初始容量 |
| `AccessStaticViaInstance` | 通过实例访问静态方法或字段 |
| `SynchronizeOnNonFinalField` | 同步非 final 字段 |
| `ArrayIncompatibleType` | 数组类型不兼容 |

---

## 5. 常用提示 / 代码质量

| inspectionId | 描述 |
|--------------|------|
| `UnusedParameters` | 未使用的参数 |
| `ParameterMayBeFinal` | 参数可以声明为 final |
| `MethodMayBeStatic` | 方法可以声明为 static |
| `ResultOfMethodCallIgnored` | 方法返回值被忽略 |
| `StringConcatenationInsideStringBufferAppend` | String 拼接可优化 |
| `EmptyCatchBlock` | 空的 catch 块 |
| `TooBroadScope` | 变量作用域过大 |
| `SwitchStatementWithTooFewBranches` | switch 分支太少 |
| `ClassWithoutNoArgConstructor` | 类没有无参构造函数 |

---

## 6. 使用示例

屏蔽单个检查：

```java
//noinspection UnusedAssignment
someVar = 123;
