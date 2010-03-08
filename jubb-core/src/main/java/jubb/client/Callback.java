package jubb.client;

public interface Callback<T extends Object> {
    public void invoke(T job);
}