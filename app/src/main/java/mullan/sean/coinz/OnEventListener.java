package mullan.sean.coinz;

/**
 *   Interface to be used for asynchronous tasks to identify when the task finishes
 *   and if it was a success or failure
 */
public interface OnEventListener<T> {

    /**
     *  Invoked when task successfully completes
     *  @param object Can be used to pass a parameter to the caller
     */
    void onSuccess(T object);

    /**
     * Invoked when task fails
     * @param e Task exception
     */
    void onFailure(Exception e);
}
