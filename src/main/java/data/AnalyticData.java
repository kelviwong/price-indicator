package data;

import lombok.Data;

@Data
public class AnalyticData {

    public AnalyticData(WritableMutableCharSequence currency) {
        this.currency = currency;
    }

    private double vwap;
    private long totalVol;
    private double totalPriceVol;
    private WritableMutableCharSequence currency;
    private long firstDataTime;
}
