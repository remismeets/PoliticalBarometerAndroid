package be.kdg.androidbarometer.model;

import java.util.List;

public class Widget {
    private String Title;
    private int GraphType;
    private List<WidgetDataDTO> WidgetDataDtos;

    public Widget(String title, int graphType, List<WidgetDataDTO> widgetDataDTOS) {
        Title = title;
        GraphType = graphType;
        this.WidgetDataDtos = widgetDataDTOS;
    }

    public String getTitle() {
        return Title;
    }

    public int getGraphType() {
        return GraphType;
    }

    public List<WidgetDataDTO> getWidgetDataDTOS() {
        return WidgetDataDtos;
    }
}
