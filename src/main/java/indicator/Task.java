package indicator;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class Task<T> implements Runnable {
    T data;
}
