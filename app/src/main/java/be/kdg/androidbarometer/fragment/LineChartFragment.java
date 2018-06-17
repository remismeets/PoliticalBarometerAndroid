package be.kdg.androidbarometer.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import be.kdg.androidbarometer.R;
import be.kdg.androidbarometer.activity.MainActivity;
import be.kdg.androidbarometer.model.Widget;
import be.kdg.androidbarometer.model.WidgetDataDTO;

public class LineChartFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    //Global attributes
    private View view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView lvLineCharts;
    private HashMap<Integer, Collection<String>> dateMap;
    private List<String> titleList;

    public LineChartFragment() {
        //Required empty public constructor
    }

    /**
     * Creates the LineChartFragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_line_chart, container, false);

        initialiseViews();
        addEventHandlers();

        return view;
    }

    /**
     * Refreshes all views on swipe down.
     */
    @Override
    public void onRefresh() {
        ((MainActivity) Objects.requireNonNull(getActivity())).synchronizeViews();
        loadWidgets();
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Initialise all views.
     */
    private void initialiseViews() {
        lvLineCharts = view.findViewById(R.id.lvLinecharts);
        loadWidgets();

        swipeRefreshLayout = view.findViewById(R.id.fragment_line_chart);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.colorPrimary), ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.colorPrimaryDark), ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.colorPrimaryDarkest));
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    /**
     * Adds event handlers to views.
     */
    private void addEventHandlers() {
        lvLineCharts.setOnScrollListener(new AbsListView.OnScrollListener() {
            private boolean scrollEnabled;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (lvLineCharts == null || lvLineCharts.getChildCount() == 0) ? 0 : lvLineCharts.getChildAt(0).getTop();

                boolean newScrollEnabled = firstVisibleItem == 0 && topRowVerticalPosition >= 0;

                if (null != swipeRefreshLayout && scrollEnabled != newScrollEnabled) {
                    swipeRefreshLayout.setEnabled(newScrollEnabled);
                    scrollEnabled = newScrollEnabled;
                }
            }
        });
    }

    /**
     * Loads all widgets trough WidgetFragment.
     */
    public void loadWidgets() {
        WidgetFragment widgetFragment = ((WidgetFragment)LineChartFragment.this.getParentFragment());
        Objects.requireNonNull(widgetFragment).retrieveWidgets();
    }

    /**
     * Generate all widgets based on the incoming widget list.
     */
    @SuppressLint("UseSparseArrays")
    public void generateWidgets(List<Widget> widgets) {
        ArrayList<LineData> lineData = new ArrayList<>();
        dateMap = new HashMap<>();
        titleList = new ArrayList<>();

        for (int i = 0; i < widgets.size(); i++) {
            lineData.add(generateData(widgets.get(i).getTitle(), widgets.get(i).getWidgetDataDTOS(), i));
        }

        ChartDataAdapter chartDataAdapter = new ChartDataAdapter(getActivity(), lineData);
        lvLineCharts.setAdapter(chartDataAdapter);
    }

    /**
     * Generate the data which will be placed in the widgets.
     */
    private LineData generateData(String title ,List<WidgetDataDTO> widgetDataDTOS, int index) {
        ArrayList<Entry> lineEntries;
        LineDataSet lineDataSet;
        LineData lineData = new LineData();
        Random random = new Random();
        titleList.add(title);

        for (int i = 0; i < widgetDataDTOS.size(); i++) {
            lineEntries = new ArrayList<>();
            List<String> dateList = new ArrayList<>();
            widgetDataDTOS.get(i).reverseList();
            for (int j = 0; j < widgetDataDTOS.get(i).getGraphValues().size(); j++) {
                lineEntries.add(new Entry(j, (float) widgetDataDTOS.get(i).getGraphValues().get(j).getNumberOfTimes()));
                dateList.add(j, widgetDataDTOS.get(i).getGraphValues().get(j).getValue());
            }
            dateMap.put(index, dateList);
            lineDataSet = new LineDataSet(lineEntries, widgetDataDTOS.get(i).getItemName());
            lineDataSet.setColor(view.getResources().getIntArray(R.array.color_array)[random.nextInt(view.getResources().getIntArray(R.array.color_array).length)]);
            lineDataSet.setLineWidth(2f);
            lineDataSet.setDrawValues(false);
            lineData.addDataSet(lineDataSet);
        }
        return lineData;
    }

    /**
     * Adapter which is used to format widget into listview and is implemented as innerclass.
     */
    private class ChartDataAdapter extends ArrayAdapter<LineData> {
        ChartDataAdapter(Context context, List<LineData> barDataList) {
            super(context, 0, barDataList);
        }

        @SuppressLint("InflateParams")
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LineData lineData = getItem(position);
            LineChart lineChart;
            TextView tvTitle;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_linechart, null);
                lineChart = convertView.findViewById(R.id.linechart);
                convertView.setTag(lineChart);
            } else {
                lineChart = (LineChart) convertView.getTag();
            }

            if (lineData != null) {
                lineData.setValueTextColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.nice_black));
            }

            tvTitle = convertView.findViewById(R.id.tvTitle);
            tvTitle.setText(titleList.get(position));

            lineChart.getDescription().setEnabled(false);
            lineChart.setDrawGridBackground(false);

            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setValueFormatter(new IndexAxisValueFormatter(dateMap.get(position)));
            xAxis.setDrawGridLines(false);

            lineChart.setData(lineData);

            lineChart.animateX(2000, Easing.EasingOption.EaseInOutSine);

            return convertView;
        }
    }
}
