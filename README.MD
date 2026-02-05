# AspectLithe

Lightweight runtime AOP-like hooks for Java, powered by BeanShell.

AspectLithe lets you intercept selected method calls with a tiny explicit snippet, run a script at runtime, and decide whether to:

- continue normal method execution, or
- return a custom value and bypass the original logic.

---

## Why AspectLithe

Full AOP stacks are powerful, but sometimes too heavy for simple dynamic behavior.

AspectLithe is for when you want:

- runtime-configurable behavior (from DB/files/remote config),
- no bytecode weaving / no Java agent,
- explicit, readable interception points in code,
- very small API surface.

---

## Install

Maven (SOON TO BE ADDED, MISSING RIGHT NOW):

```xml
<dependency>
  <groupId>io.github.lithedream</groupId>
  <artifactId>aspectlithe</artifactId>
  <version>2.0.0</version>
</dependency>
```

AspectLithe uses BeanShell (`bsh.*`) for script execution.

---

## Java Compatibility

- Target release: Java 8
- CI tested on: Java 8, 11, 17, 21

---

## Quick Start

### 1) Register your loader at startup

```java
AspectLithe.register(new MyAspectLoader());
```

### 2) Add a join point inside methods you want to intercept

```java
import io.github.lithedream.aspectlithe.AspectLithe;
import io.github.lithedream.aspectlithe.JoinPoint;

public class MyService {

public Integer compute(String userId, Long amount) {
    for (JoinPoint $ : AspectLithe
            .$(MyService.class, this, "compute")
            .o(String.class, "userId", userId)
            .o(Long.class, "amount", amount)
            .run()) {
            // AspectLithe will try to find an aspect for this invocation, execute it, and the aspect will tell if the method should be resumed, or a value must be returned.
            // AspectLithe can be used to execute code before normal method execution (with full view of caller instance and parameter values), or to replace the method body with a custom script
        return (Integer) $.returnValue();
    }

    // original method logic
    return amount.intValue();
}

}
```

Equivalent style:

```java
JoinPoint $ = AspectLithe.$(MyService.class, this, "compute")
    .o(String.class, "userId", userId)
    .o(Long.class, "amount", amount);

if ($.runIf()) {
    return (Integer) $.returnValue();
}
```

---

## Implement `AspectLoader`

`AspectLoader` is your config source (DB, file, API, etc).

You can return aspects as:

- `Collection<Object[]>` (`class`, `method`, `params`, `script`), and/or
- `Map<AspectKey, AspectScript>`.

```java
import io.github.lithedream.aspectlithe.AspectLoader;
import io.github.lithedream.aspectlithe.inner.AspectKey;
import io.github.lithedream.aspectlithe.inner.AspectScript;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public final class MyAspectLoader implements AspectLoader {

    @Override
    public Collection<Object[]> loadAspectsArray() {
        return Collections.emptyList();
    }

    @Override
    public Map<AspectKey, AspectScript> loadAspectsMap() {
        return Collections.singletonMap(
            new AspectKey(
                "com.example.MyService",
                "compute",
                "java.lang.String,java.lang.Long"
            ),
            new AspectScript(
                "if (amount < 0L) { "+
                "     return 0;     "+
                "} else {           "+
                "     return $jp;   "+
                "}";
            )
        );
    }

    @Override
    public long getReloadIntervalMillis() {
        return 3600000; // the time between invocations that AspectLithe waits before reloading aspects
    }

    @Override
    public boolean isEnabled() {
        return true; // if it's false, AspectLithe will not run
    }
}
```

---

## Script Runtime Context

Inside each script, AspectLithe injects:

- `$this` -> caller instance
- `$jp` -> current `JoinPoint`
- named parameters from `.o(Class, name, value)`

It also evaluates:

- `static import <caller-class>.*;`
- `import <caller-package>.*;`
- `setAccessibility(true);` (private member access)

---

## Matching Rules

Aspect lookup order:

1. wildcard parameters key (`*`)
2. exact parameter type list (FQNs in declaration order)

`AspectKey` = `(className, methodName, parameters)` where parameters are either:

- `*`, or
- comma-separated fully-qualified class names.

---

## Reloading

- Aspects are cached in memory.
- They reload automatically based on `getReloadIntervalMillis()`.
- You can force reload with `AspectLithe.reload(...)`.

---

## Error Behavior

- BeanShell eval failures are wrapped in `AspectException`.
- Runtime exceptions thrown by script target code are propagated.

---

## Design Notes / Limitations

- Interception is explicit (manual instrumentation in methods).
- This is runtime scripting, not compile-time or load-time weaving.
- BeanShell 2.0b6 syntax/feature limits apply.
- `AspectLithe.register(...)` sets a static global loader.

---

## Words from the author, back when I started this project

It works by writing special blocks of code (like in the example) at the start of your important methods (the one you may need to rewrite / inspect at runtime),
where all the method's coordinates are formalized in my notation (Caller class, caller instance, method name, followed by class/name/value of parameters).

When called, AspectLithe looks into its configuration if you have defined a script for those coordinates, and it executes it.
The script can force a return value for the outer method (thus totally replacing its logic),
or can resume the outer method execution (when you need to log something, or do something with the parameters before actual method execution).

Beanshell 2.0b6 is used to execute the script content; because of this, you need it your classpath, and you cannot use generics or varargs in your scripts.
Inside the script, you will find $this as the reference of the caller object, and $jp as the JoinPoint instance, and the parameters of the method (with the same name)
You can call private methods and fields. Do not call "this" because it is a special BeanShell object.

At the end of the script, you can return an object, and it will be returned instead of the original method's logic,
or you can return $jp and tell AspectLithe to continue with the original method.

Instead of returning $jp, you can call somewhere $jp.continueAfter(), and at the end of the script the original method will be resumed.

---

## License

MIT. See [`LICENSE`](LICENSE).

---

## Author

lithedream
