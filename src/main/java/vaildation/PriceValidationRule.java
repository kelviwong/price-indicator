package vaildation;

import data.Price;

import java.util.function.Predicate;

public class PriceValidationRule extends RulePredicate<Price> {
    public PriceValidationRule() {
        super(item -> item.getPrice() > 0,
                "Price is not greater than 0");
    }
}
