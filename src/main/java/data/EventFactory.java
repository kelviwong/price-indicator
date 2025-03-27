package data;

import common.TimeProvider;

public class EventFactory {

    TimeProvider timeProvider;

    public EventFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public Event getEvent(EventType eventType) {
        Event<?> event;
        if (eventType == EventType.PRICE) {
            event = new PriceEvent();
        } else {
            event = new IndicatorEvent();
        }

        event.setStartNs(timeProvider.now());
        return event;
    }
}
