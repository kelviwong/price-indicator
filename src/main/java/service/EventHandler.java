package service;
public interface EventHandler<T>{

    void handle(T event) throws Exception;

}
