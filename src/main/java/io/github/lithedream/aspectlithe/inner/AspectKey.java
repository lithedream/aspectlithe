package io.github.lithedream.aspectlithe.inner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A key for the aspects map.
 * It should specify a class Fully Qualified Name, methodName, and a list of class FQNs of parameters
 */
public final class AspectKey implements Serializable {

    private String className;

    private String method;

    private List<String> parameters;

    /**
     * Creates a new AspectKey.
     * chainedParamsString should be a string with class FQN of parameters chained by the , character.
     * You can pass * to represent every method "methodName" of class "className".
     * You can pass null of empty String if there should be no parameters.
     *
     * @param className
     * @param methodName
     * @param chainedParamsString
     */
    public AspectKey(String className, String methodName, String chainedParamsString) {
        this.className = className;
        this.method = methodName;
        this.parameters = (chainedParamsString != null && chainedParamsString.length() > 0) ? Arrays.asList(chainedParamsString.split(",")) : new ArrayList<String>();
    }

    /**
     * Creates a new AspectKey.
     * parameters should be a List of class FQN of parameters.
     * You can pass * as the only element, to represent every method "methodName" of class "className".
     * You can pass an empty List if there should be no parameters.
     *
     * @param className
     * @param method
     * @param parameters
     */
    public AspectKey(String className, String method, List<String> parameters) {
        this.className = className;
        this.method = method;
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AspectKey)) {
            return false;
        }

        AspectKey casted = (AspectKey) obj;
        return className.equals(casted.className) && method.equals(casted.method) && parameters.equals(casted.parameters);
    }

    @Override
    public int hashCode() {
        return className.hashCode();
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

}
