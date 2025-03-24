package vaildation;

import java.util.function.Predicate;

public abstract class RulePredicate<T> implements ValidationRule<T> {
    public static final String EMPTY_STRING = "";

    Predicate<T> predicate;
    String errorString;

    public RulePredicate(Predicate<T> predicate, String errorString) {
        this.errorString = errorString;
        this.predicate = predicate;
    }

    public String check(T item){
        if (item == null) {
            return "data is null";
        }

        if (!predicate.test(item)){
            return errorString;
        }

        return EMPTY_STRING;
    }


}
