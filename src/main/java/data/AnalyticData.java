package data;

import lombok.Data;

@Data
public class AnalyticData {

    public AnalyticData(String currency) {
        this.currency = currency;
    }

    private double vwap;
    private long totalVol;
    private double totalPriceVol;
    private String currency;
    private long firstDataTime;
}
