package lithe.aspect.inner;

/**
 * An exception caught during Aspect script execution
 */
public final class AspectException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Throwable target;

    public AspectException(Throwable t) {
        super((Throwable) null);
        this.target = t;
    }

    /**
     * The original exception caught during script execution
     *
     * @return
     */
    public Throwable getTarget() {
        return target;
    }

    @Override
    public synchronized Throwable getCause() {
        return target;
    }

}