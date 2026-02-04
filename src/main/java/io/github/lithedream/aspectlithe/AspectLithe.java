package io.github.lithedream.aspectlithe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.TargetError;
import io.github.lithedream.aspectlithe.inner.AspectException;
import io.github.lithedream.aspectlithe.inner.AspectKey;
import io.github.lithedream.aspectlithe.inner.AspectScript;

/**
 * Entry point of the AspectLithe library
 */
public final class AspectLithe {

    private static final AtomicLong lastLoadTime = new AtomicLong(-1);

    private static final Map<AspectKey, AspectScript> aspectMap = new ConcurrentHashMap<AspectKey, AspectScript>();

    private static AspectLoader staticAspectLoader = null;

    /**
     * Defines a JoinPoint
     *
     * @param callerClass
     * @param callerInstance
     * @param methodName
     * @param <T>
     * @return
     */
    public static <T> JoinPoint $(Class<T> callerClass, T callerInstance, String methodName) {
        return new JoinPoint(callerClass, callerInstance, methodName);
    }

    protected static boolean run(JoinPoint jp) throws AspectException {
        return run(jp, staticAspectLoader);
    }

    protected static boolean run(JoinPoint jp, AspectLoader al) throws AspectException {
        if (jp.callerClass == null || al == null) {
            return false;
        }

        if (System.currentTimeMillis() > lastLoadTime.get() + al.getReloadIntervalMillis()) {
            reload(false, al);
        }

        AspectScript a = null;
        if (!aspectMap.isEmpty()) {
            AspectKey key = new AspectKey(jp.callerClass.getName(), jp.callerMethodName, Collections.singletonList("*"));
            a = aspectMap.get(key);
            if (a == null) {
                List<String> parameters = new ArrayList<>();
                for (Class<?> class1 : jp.parameterClasses) {
                    parameters.add(class1 != null ? class1.getName() : "");
                }
                key.setParameters(parameters);
                a = aspectMap.get(key);
            }
        }
        if (a == null) {
            return false;
        }
        if (a.getScript() != null) {
            try {
                Interpreter interpreter = new Interpreter();
                interpreter.set("$this", jp.callerInstance);
                interpreter.set("$jp", jp);
                for (int p = 0; p < jp.parameterValues.size(); p++) {
                    interpreter.set(jp.parameterNames.get(p), jp.parameterValues.get(p));
                }
                interpreter.eval("static import " + jp.callerClass.getName() + ".*;");
                if (jp.callerClass.getPackage() != null) {
                    interpreter.eval("import " + jp.callerClass.getPackage().getName() + ".*;");
                }
                interpreter.eval("setAccessibility(true);");

                jp.returnValue = interpreter.eval(a.getScript());

                if (jp.shouldContinueAfter() || jp.returnValue == jp) {
                    return false;
                }
            } catch (TargetError e) {
                throw e.getTarget() instanceof RuntimeException ? (RuntimeException) e.getTarget()
                        : new AspectException(e.getTarget() != null ? e.getTarget() : e);
            } catch (EvalError e) {
                throw new AspectException(e);
            }
        }
        return true;
    }

    /**
     * Force the reload of the aspect configuration, with the AspectLoader parameter, or with the staticAspectLoader if null
     *
     * @param al
     */
    public static void reload(AspectLoader al) {
        reload(true, al != null ? al : staticAspectLoader);
    }

    private synchronized static void reload(boolean force, AspectLoader al) {
        if (force || (System.currentTimeMillis() > lastLoadTime.get() + al.getReloadIntervalMillis())) {
            Map<AspectKey, AspectScript> mapFound = new LinkedHashMap<>();
            Collection<Object[]> ar = al.loadAspectsArray();
            if (ar != null) {
                for (Object[] objects : ar) {
                    String clas = (String) objects[0];
                    String method = (String) objects[1];
                    String params = (String) objects[2];
                    String script = (String) objects[3];
                    mapFound.put(new AspectKey(clas, method, params), new AspectScript(script));
                }
            }
            Map<AspectKey, AspectScript> m = al.loadAspectsMap();
            if (m != null) {
                mapFound.putAll(m);
            }

            aspectMap.keySet().retainAll(mapFound.keySet());
            aspectMap.putAll(mapFound);
            lastLoadTime.set(System.currentTimeMillis());
        }
    }

    /**
     * Call this at the startup of your application to define a static AspectLoader
     *
     * @param al
     */
    public static void register(AspectLoader al) {
        staticAspectLoader = al;
    }

}