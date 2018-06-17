package be.kdg.androidbarometer.model;


import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class AlertDTO {
    private String Name;
    private boolean IsRead;
    private String TimeStamp;
    private int AlertType;

    public AlertDTO(String name, boolean isRead, String timeStamp, int alertType) {
        Name = name;
        IsRead = isRead;
        TimeStamp = timeStamp;
        AlertType = alertType;
    }

    public String getName() {
        return Name;
    }

    public boolean isRead() {
        return IsRead;
    }

    public String getTimeStamp() {
        return TimeStamp;
    }

    public int getAlertType() {
        return AlertType;
    }

    public String getHoursPast() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        DateTime dateTime = formatter.parseDateTime(TimeStamp);
        Period period = new Period(dateTime, DateTime.now());
        return String.valueOf(period.getHours());
    }
}
