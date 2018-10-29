package mullan.sean.coinz;

public interface OnEventListener<T> {
    void onSuccess(T object);
    void onFailure(Exception e);
}
