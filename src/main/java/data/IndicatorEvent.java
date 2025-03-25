package data;

public class IndicatorEvent extends Event<AnalyticData> implements Resettable{
    public IndicatorEvent(AnalyticData data) {
        super(data);
    }

    @Override
    public String toString() {
        return "IndicatorEvent{" +
                "data=" + data +
                '}';
    }

    @Override
    public void reset() {
        data = null;
    }
}
