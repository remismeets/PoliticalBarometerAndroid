package be.kdg.androidbarometer.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
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

public class BarChartFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    //Global attributes
    private View view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView lvBarcharts;
    private HashMap<Integer, Collection<String>> dateMap;
    private List<String> titleList;

    public BarChartFragment() {
        //Required empty public constructor
    }

    /**
     * Creates the BarChartFragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_bar_chart, container, false);

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
        lvBarcharts = view.findViewById(R.id.lvBarcharts);
        loadWidgets();

        swipeRefreshLayout = view.findViewById(R.id.fragment_bar_chart);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.colorPrimary), ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.colorPrimaryDark), ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.colorPrimaryDarkest));
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    /**
     * Adds event handlers to views.
     */
    private void addEventHandlers() {
        lvBarcharts.setOnScrollListener(new AbsListView.OnScrollListener() {
            private boolean scrollEnabled;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (lvBarcharts == null || lvBarcharts.getChildCount() == 0) ? 0 : lvBarcharts.getChildAt(0).getTop();

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
        WidgetFragment widgetFragment = ((WidgetFragment)BarChartFragment.this.getParentFragment());
        Objects.requireNonNull(widgetFragment).retrieveWidgets();
    }

    /**
     * Generate all widgets based on the incoming widget list.
     */
    @SuppressLint("UseSparseArrays")
    public void generateWidgets(List<Widget> widgets) {
        ArrayList<BarData> barData = new ArrayList<>();
        dateMap = new HashMap<>();
        titleList = new ArrayList<>();

        for (int i = 0; i < widgets.size(); i++) {
            barData.add(generateData(widgets.get(i).getTitle(), widgets.get(i).getWidgetDataDTOS(), i));
        }

        ChartDataAdapter chartDataAdapter = new ChartDataAdapter(getActivity(), barData);
        lvBarcharts.setAdapter(chartDataAdapter);
    }

    /**
     * Generate the data which will be placed in the widgets.
     */
    private BarData generateData(String title, List<WidgetDataDTO> widgetDataDTOS, int index) {
        ArrayList<BarEntry> barEntries;
        BarDataSet barDataSet;
        BarData barData = new BarData();
        Random random = new Random();
        titleList.add(title);

        for (int i = 0; i < widgetDataDTOS.size(); i++) {
            barEntries = new ArrayList<>();
            List<String> dateList = new ArrayList<>();
            widgetDataDTOS.get(i).reverseList();
            for (int j = 0; j < widgetDataDTOS.get(i).getGraphValues().size(); j++) {
                barEntries.add(new BarEntry(j, (float) widgetDataDTOS.get(i).getGraphValues().get(j).getNumberOfTimes()));
                dateList.add(j, widgetDataDTOS.get(i).getGraphValues().get(j).getValue());
            }
            dateMap.put(index, dateList);
            barDataSet = new BarDataSet(barEntries, widgetDataDTOS.get(i).getItemName());
            barDataSet.setColor(view.getResources().getIntArray(R.array.color_array)[random.nextInt(view.getResources().getIntArray(R.array.color_array).length)]);
            barDataSet.setBarShadowColor(Color.rgb(203, 203, 203));
            barDataSet.setDrawValues(false);
            barData.setBarWidth(0.9f);
            barData.addDataSet(barDataSet);
        }
        return barData;
    }

    /**
     * Adapter which is used to format widget into listview and is implemented as innerclass.
     */
    private class ChartDataAdapter extends ArrayAdapter<BarData> {
        ChartDataAdapter(Context context, List<BarData> barDataList) {
            super(context, 0, barDataList);
        }

        @SuppressLint("InflateParams")
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            BarData barData = getItem(position);
            BarChart barChart;
            TextView tvTitle;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_barchart, null);
                barChart = convertView.findViewById(R.id.barchart);
                convertView.setTag(barChart);
            } else {
                barChart = (BarChart) convertView.getTag();
            }

            if (barData != null) {
                barData.setValueTextColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.nice_black));
                if (barData.getDataSetCount() >= 2) {
                    barData.groupBars(0.00f, 0.04f, 0.02f);
                }
            }

            tvTitle = convertView.findViewById(R.id.tvTitle);
            tvTitle.setText(titleList.get(position));

            barChart.getDescription().setEnabled(false);
            barChart.setDrawGridBackground(false);

            XAxis xAxis = barChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setValueFormatter(new IndexAxisValueFormatter(dateMap.get(position)));
            xAxis.setDrawGridLines(false);

            YAxis leftAxis = barChart.getAxisLeft();
            leftAxis.setLabelCount(5, false);
            leftAxis.setSpaceTop(15f);

            YAxis rightAxis = barChart.getAxisRight();
            rightAxis.setLabelCount(5, false);
            rightAxis.setSpaceTop(15f);

            barChart.setData(barData);
            barChart.setFitBars(true);

            barChart.animateX(2000, Easing.EasingOption.EaseInOutCubic);

            return convertView;
        }
    }
}
