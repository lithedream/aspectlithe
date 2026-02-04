package io.github.lithedream.aspectlithe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.github.lithedream.aspectlithe.inner.AspectException;

/**
 * Defines a single join point, represented by a class name, method name, and sequence of classes of parameters
 */
public final class JoinPoint {

    protected Class<? extends Object> callerClass;

    protected Object callerInstance;

    protected String callerMethodName;

    protected Object returnValue = null;

    protected List<Class<?>> parameterClasses = new ArrayList<Class<?>>();

    protected List<String> parameterNames = new ArrayList<String>();

    protected List<Object> parameterValues = new ArrayList<Object>();

    private boolean flContinueAfter = false;

    /**
     * Defines a new join point for the defined class, instance, and method name
     *
     * @param callerClass
     * @param callerInstance
     * @param methodName
     */
    public JoinPoint(Class<? extends Object> callerClass, Object callerInstance, String methodName) {
        this.callerClass = callerClass == null ? callerInstance.getClass() : callerClass;
        this.callerInstance = callerInstance;
        this.callerMethodName = methodName;
    }

    /**
     * Adds a parameter to the declaration of the join point of the current method
     *
     * @param paramClass
     * @param paramName
     * @param paramValue
     * @param <T>
     * @return
     */
    public <T> JoinPoint o(Class<T> paramClass, String paramName, T paramValue) {
        parameterClasses.add(paramClass);
        parameterNames.add(paramName != null ? paramName : "_" + parameterNames.size());
        parameterValues.add(paramValue);
        return this;
    }

    /**
     * Run with this current AspectLoader (if you don't want or cannot call AspectLithe.register(...) at the application startup), in a snippet like
     * <p>
     * <pre>
     * {@code
     *   public RETURN_TYPE METHOD_NAME (PARAMETERS...) {
     *      JoinPoint $ = AspectLithe.$(THIS.class, this, "METHOD_NAME").o(PARAMETER1.class, "parameter1", parameter1)....o(PARAMETERN.class, "parameterN", parameterN);
     *      if ($.runIf(YOUR_IMPLEMENTATION_OF_ASPECT_LOADER.getInstance())) {
     *        return (RETURN_TYPE_OF_THIS_METHOD) $.returnValue; // or simpy return; if void
     *      }
     *
     *      // ... rest of original method
     *
     *   }
     * }</pre>
     *
     * @param al
     * @return
     * @throws AspectException
     */
    public boolean runIf(AspectLoader al) throws AspectException {
        return AspectLithe.run(this, al);
    }

    /**
     * Run with the default AspectLoader (the one passed by calling AspectLithe.register(...) at the application startup), in a snippet like
     * <p>
     * <pre>
     * {@code
     *   public RETURN_TYPE METHOD_NAME (PARAMETERS...) {
     *      JoinPoint $ = AspectLithe.$(THIS.class, this, "METHOD_NAME").o(PARAMETER1.class, "parameter1", parameter1)....o(PARAMETERN.class, "parameterN", parameterN);
     *      if ($.runIf() {
     *        return (RETURN_TYPE_OF_THIS_METHOD) $.returnValue; // or simpy return; if void
     *      }
     *
     *      // ... rest of original method
     *
     *   }
     * }</pre>
     *
     * @return
     * @throws AspectException
     */
    public boolean runIf() throws AspectException {
        return AspectLithe.run(this);
    }

    /**
     * Run with the default AspectLoader (the one passed by calling AspectLithe.register(...) at the application startup), in a snippet like
     * <p>
     * <pre>
     * {@code
     *   public RETURN_TYPE METHOD_NAME (PARAMETERS...) {
     *      for (JoinPoint $ : AspectLithe.$(THIS.class, this, "METHOD_NAME").o(PARAMETER1.class, "parameter1", parameter1)....o(PARAMETERN.class, "parameterN", parameterN).run()) {
     *          return (RETURN_TYPE_OF_THIS_METHOD) $.returnValue(); // or simpy return; if void
     *      }
     *
     *      // ... rest of original method
     *
     *   }
     * }</pre>
     *
     * @return
     * @throws AspectException
     */
    public Collection<JoinPoint> run() throws AspectException {
        if (AspectLithe.run(this)) {
            return Collections.singletonList(this);
        }
        return Collections.emptyList();
    }

    /**
     * Returns the result object of the script invocation
     *
     * @return
     */
    public Object returnValue() {
        return returnValue;
    }

    /**
     * Tells AspectLithe if the original method should be resumed after the script end
     *
     * @return
     */
    public boolean shouldContinueAfter() {
        return flContinueAfter;
    }

    /**
     * Call this inside the script to tell AspectLithe to continue with the original method after the script end.
     * Doesn't stop script execution, it's checked only when the script ends and AspectLithe looks if the original method should be resumed
     * <p>
     * <pre>
     * {@code
     *      $jp.continueAfter();
     * }</pre>
     */
    public void continueAfter() {
        this.flContinueAfter = true;
    }

}
