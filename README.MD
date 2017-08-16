# aspectlithe

## Synopsis

Java library for lightweight "aspect like" functionality

## Motivation

I like aspect-oriented programming ideas, but usually I cannot (or don't want to) import the behemoth of a full AOP framework in my projects.
This is my solution to have "aspect like" functionality in a project.

## Details

It works by writing special blocks of code (like in the next example) at the start of your important methods (the one you may need to rewrite / inspect at runtime),
where all the method's coordinates are formalized in my notation (Caller class, caller instance, method name, followed by class/name/value of parameters).

When called, AspectLithe looks into its configuration if you have defined a script for those coordinates, and he executes it.
The script can force a return value for the outer method (thus totally replacing its logic),
or can resume the outer method execution (when you need to log something, or do something with the parameters before actual method execution).

Beanshell 2.0b6 is used to execute the script content; because of this, you need it your classpath, and you cannot use generics or varargs in your scripts.
Inside the script, you will find $this as the reference of the caller object, and $jp as the JoinPoint instance, and the parameters of the method (with the same name)
You can call private methods and fields. Do not call "this" because it is a special Beanshell object.

At the end of the script, you can return an object, and it will be returned instead of the original method's logic,
or you can return $jp and tell AspectLithe to continue with the original method.

Instead of returning $jp, you can call somewhere $jp.continueAfter(), and at the end of the script the original method will be resumed.

Please write me if you like my project, my email is lithedr3am@gmail.com.
I'm not good with documentation, but if you want to use this I will help you as best as I can.


## Code Example

```java
public class Test {

	public static void main(String[] args) {
		AspectLithe.register(TestLoader.getInstance());

		...
		// rest of application startup

	}


	public Integer testMethod(String parameter1, Long parameter2) {
        for (JoinPoint $ : AspectLithe.$(Test.class, this, "testMethod").o(String.class, "parameter1", parameter1).o(Long.class, "parameter2", parameter2).run()) {
	        // AspectLithe will try to find an aspect for this invocation, execute it, and the aspect will tell if the method should be resumed, or a value must be returned.
	        // AspectLithe can be used to execute code before normal method execution (with full view of caller instance and parameter values), or to replace the method body with a custom script
            return (Integer) $.returnValue();
        }

        ...
        // rest of method invocation.
    }


}



public class TestLoader implements AspectLoader {

	private static TestLoader instance = null;

    // This is the singleton that AspectLithe calls to get the configuration
	public static TestLoader getInstance() {
		if (instance == null) {
			instance = new TestLoader();
		}
		return instance;
	}

	@Override
	public Collection<Object[]> loadAspectsArray() {
        List<Object[]> aspects= ...
        // you are free to load your aspects from your database, or from anything else
		return aspects;
	}

	@Override
	public Map<AspectKey, AspectScript> loadAspectsMap() {
	    Map<AspectKey, AspectScript> aspects= ...
        // you are free to load your aspects from your database, or from anything else
        // this method is equivalent to loadAspectsArray, and may be useful if you prefer to load aspects as complex objects insteaf of Object[]
  		return aspects;
	}

	@Override
	public long getReloadIntervalMillis() {
		return 3600000; //the time between invocations that AspectLithe waits before reloading aspects
	}

	@Override
	public boolean isEnabled() {
		return true; //if it's false, AspectLithe will not run
	}

}

```

## Author

* **lithedream**
lithedr3am@gmail.com

## License

LGPL-2.1
