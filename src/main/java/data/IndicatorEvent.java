package data;

public class IndicatorEvent extends Event<AnalyticData> implements Resettable {
    public IndicatorEvent(AnalyticData data) {
        super(data);
    }

    StringBuilder sb = new StringBuilder();

    @Override
    public String toString() {
        sb.setLength(0);
        //avoid double toString
        String append = sb.append("IndicatorEvent{")
                .append("data=")
                .append(data.getVwap()).append(",")
                .append(data.getCurrency()).append(",")
                .append(data.getFirstDataTime()).append(",")
                .append(data.getTotalVol()).append(",")
                .append(data.getTotalPriceVol()).append(",")
                .append(")").toString();
        return append;
    }

    @Override
    public void reset() {
        data = null;
    }
}
