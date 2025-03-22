package data;

public class IndicatorEvent extends Event<AnalyticData> {
    public IndicatorEvent(AnalyticData data) {
        super(data);
    }

    @Override
    public String toString() {
        return "IndicatorEvent{" +
                "data=" + data +
                '}';
    }
}
