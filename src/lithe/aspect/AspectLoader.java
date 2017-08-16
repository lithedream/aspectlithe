package lithe.aspect;

import lithe.aspect.inner.AspectKey;
import lithe.aspect.inner.AspectScript;

import java.util.Collection;
import java.util.Map;

/**
 * The interface that you need to implement to provide AspectLithe of a "configuration loader" object.
 * You should call AspectLithe.register(your implementation of AspectLoader) at the start of your application.
 */
public interface AspectLoader {

    /**
     * You should return a Collection of Object[] with the aspect configuration.
     * Every Object[] should have 4 strings inside: class name, method name, parameters, script content.
     * You should implement this method or loadAspectsMap, because AspectLithe will call both and load every row you give it.
     * There is no difference in implementing this method or loadAspectsMap, you really need only one of them.
     *
     * @return
     */
    Collection<Object[]> loadAspectsArray();

    /**
     * You should return a Map of AspectKey and AspectScript with the aspect configuration.
     * You should implement this method or loadAspectsArray, because AspectLithe will call both and load every entry you give it.
     * There is no difference in implementing this method or loadAspectsArray, you really need only one of them.
     *
     * @return
     */
    Map<AspectKey, AspectScript> loadAspectsMap();

    /**
     * You should return the time interval that needs to pass for aspect configuration to be reloaded, in milliseconds.
     *
     * @return
     */
    long getReloadIntervalMillis();

    /**
     * You should return true if the AspectLithe system should work, or false if you need it to shut down.
     *
     * @return
     */
    boolean isEnabled();

}
