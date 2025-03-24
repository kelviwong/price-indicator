package vaildation;

import data.Price;

public class VolumeValidationRule extends RulePredicate<Price> {
    public VolumeValidationRule() {
        super(item -> item.getVolume() > 0,
                "Volume is not greater than 0");
    }
}
