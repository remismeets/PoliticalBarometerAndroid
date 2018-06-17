package be.kdg.androidbarometer.model;

public class GraphValue {
    private String Value;
    private double NumberOfTimes;

    public GraphValue(String value, double numberOfTimes) {
        Value = value;
        NumberOfTimes = numberOfTimes;
    }

    public String getValue() {
        return Value;
    }

    public double getNumberOfTimes() {
        return NumberOfTimes;
    }
}
