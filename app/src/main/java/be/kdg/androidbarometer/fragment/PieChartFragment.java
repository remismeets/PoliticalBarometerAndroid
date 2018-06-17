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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import be.kdg.androidbarometer.R;
import be.kdg.androidbarometer.activity.MainActivity;
import be.kdg.androidbarometer.model.GraphValue;
import be.kdg.androidbarometer.model.Widget;
import be.kdg.androidbarometer.model.WidgetDataDTO;

public class PieChartFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    //Global attributes
    private View view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView lvPieCharts;
    private List<String> titleList;

    public PieChartFragment() {
        //Required empty public constructor
    }

    /**
     * Creates the PieChartFragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_pie_chart, container, false);

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
        lvPieCharts = view.findViewById(R.id.lvPiecharts);
        loadWidgets();

        swipeRefreshLayout = view.findViewById(R.id.fragment_pie_chart);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.colorPrimary), ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.colorPrimaryDark), ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.colorPrimaryDarkest));
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    /**
     * Adds event handlers to views.
     */
    private void addEventHandlers() {
        lvPieCharts.setOnScrollListener(new AbsListView.OnScrollListener() {
            private boolean scrollEnabled;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (lvPieCharts == null || lvPieCharts.getChildCount() == 0) ? 0 : lvPieCharts.getChildAt(0).getTop();

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
        WidgetFragment widgetFragment = ((WidgetFragment) PieChartFragment.this.getParentFragment());
        Objects.requireNonNull(widgetFragment).retrieveWidgets();
    }

    /**
     * Generate all widgets based on the incoming widget list.
     */
    public void generateWidgets(List<Widget> widgets) {
        ArrayList<PieData> pieData = new ArrayList<>();
        titleList = new ArrayList<>();

        for (int i = 0; i < widgets.size(); i++) {
            pieData.add(generateData(widgets.get(i).getTitle(), widgets.get(i).getWidgetDataDTOS()));
        }

        ChartDataAdapter chartDataAdapter = new ChartDataAdapter(getActivity(), pieData);
        lvPieCharts.setAdapter(chartDataAdapter);
    }

    /**
     * Generate the data which will be placed in the widgets.
     */
    private PieData generateData(String name, List<WidgetDataDTO> widgetDataDTOS) {
        ArrayList<PieEntry> pieEntries;
        PieDataSet pieDataSet;
        PieData pieData = new PieData();
        titleList.add(name);

        for (int i = 0; i < widgetDataDTOS.size(); i++) {
            pieEntries = new ArrayList<>();
            double total = widgetDataDTOS.get(i).getGraphValues().stream().mapToDouble(GraphValue::getNumberOfTimes).sum();
            for (int j = 0; j < widgetDataDTOS.get(i).getGraphValues().size(); j++) {
                float piePiece = (float) (widgetDataDTOS.get(i).getGraphValues().get(j).getNumberOfTimes() / total * 100);
                pieEntries.add(new PieEntry(piePiece, widgetDataDTOS.get(i).getGraphValues().get(j).getValue()));
            }
            pieDataSet = new PieDataSet(pieEntries, "");
            pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            pieDataSet.setValueTextSize(15f);
            pieData.addDataSet(pieDataSet);
        }

        return pieData;
    }

    /**
     * Adapter which is used to format widget into listview and is implemented as innerclass.
     */
    private class ChartDataAdapter extends ArrayAdapter<PieData> {
        ChartDataAdapter(Context context, List<PieData> pieDataList) {
            super(context, 0, pieDataList);
        }

        @SuppressLint("InflateParams")
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            PieData pieData = getItem(position);
            PieChart pieChart;
            TextView tvTitle;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_piechart, null);
                pieChart = convertView.findViewById(R.id.piechart);
                convertView.setTag(pieChart);
            } else {
                pieChart = (PieChart) convertView.getTag();
            }

            tvTitle = convertView.findViewById(R.id.tvTitle);
            tvTitle.setText(titleList.get(position));

            if (pieData != null) {
                pieData.setValueTextColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.nice_black));
            }
            pieChart.getDescription().setEnabled(false);

            Legend legend = pieChart.getLegend();
            legend.setTextSize(15f);
            legend.setPosition(Legend.LegendPosition.LEFT_OF_CHART_CENTER);
            legend.setWordWrapEnabled(true);

            pieChart.setDrawHoleEnabled(false);
            pieChart.setData(pieData);

            pieChart.animateX(2000, Easing.EasingOption.EaseInOutCubic);

            return convertView;
        }
    }
}
