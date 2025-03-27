package data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class Event<T> {
    long startNs;
    T data;

    public Event(T data) {
        this.data = data;
    }

    public Event() {

    }
}
