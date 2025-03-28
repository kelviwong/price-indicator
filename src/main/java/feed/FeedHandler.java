package feed;

import data.Price;

public interface FeedHandler<T> {
    Price process(T feed) throws Exception;
}
