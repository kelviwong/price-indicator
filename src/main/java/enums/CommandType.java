package enums;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum CommandType {
    PRICE_ADD("addprice"), Exit("exit");

    // this is used for cache the value
    private static final Map<String, CommandType> enumMap = new HashMap<>();

    static {
        for (CommandType side : values()) {
            enumMap.put(side.value, side);
        }
    }

    public final String value;

    CommandType(String value) {
        this.value = value;
    }

    public static Optional<CommandType> valueFrom(String value) {
        return Optional.ofNullable(enumMap.get(value));
    }
}
