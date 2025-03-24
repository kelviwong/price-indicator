package vaildation;

public interface ValidationRule<T> {
    String check(T value);
}
