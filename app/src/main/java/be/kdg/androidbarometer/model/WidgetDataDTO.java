package be.kdg.androidbarometer.model;

import java.util.Collections;
import java.util.List;

public class WidgetDataDTO {
    private String KeyValue;
    private String ItemName;
    private List<GraphValue> GraphValues;

    public WidgetDataDTO(String keyValue, String itemName, List<GraphValue> graphValues) {
        KeyValue = keyValue;
        ItemName = itemName;
        GraphValues = graphValues;
    }

    public String getKeyValue() {
        return KeyValue;
    }

    public String getItemName() {
        return ItemName;
    }

    public List<GraphValue> getGraphValues() {
        return GraphValues;
    }

    public void reverseList() {
        Collections.reverse(GraphValues);
    }
}
