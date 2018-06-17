package be.kdg.androidbarometer.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import be.kdg.androidbarometer.R;

import be.kdg.androidbarometer.model.Token;
import be.kdg.androidbarometer.model.Widget;
import be.kdg.androidbarometer.other.RestClient;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class WidgetFragment extends Fragment {
    //Global attributes
    private static final String GETWIDGETS_URL = "http://10.134.216.25:8011/api/Android/Widgets";

    private View view;
    private Token token;
    private CompositeDisposable compositeDisposable;
    private Adapter adapter;

    public WidgetFragment() {
        //Required empty public constructor
    }

    /**
     * Creates the WidgetFragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_widget, container, false);

        readSharedPrefs();
        initialiseViews();

        return view;
    }

    /**
     * Initialise all views.
     */
    private void initialiseViews() {
        compositeDisposable = new CompositeDisposable();

        ViewPager viewPager = view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        TabLayout tabs = view.findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    /**
     * Reads token from shared preferences.
     */
    private void readSharedPrefs() {
        SharedPreferences sharedPref = Objects.requireNonNull(this.getActivity()).getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("token", "");
        token = new Token();
        if (!json.equals("")) {
            token = gson.fromJson(json, Token.class);
        }
    }

    /**
     * Adds fragments to tabs.
     */
    private void setupViewPager(ViewPager viewPager) {
        adapter = new Adapter(getChildFragmentManager(), view);
        adapter.addFragment(new LineChartFragment(), R.drawable.ic_line_chart);
        adapter.addFragment(new BarChartFragment(), R.drawable.ic_bar_chart);
        adapter.addFragment(new PieChartFragment(), R.drawable.ic_pie_chart);
        viewPager.setAdapter(adapter);
    }

    /**
     * Retrieves widgets trough api call.
     */
    public void retrieveWidgets() {
        Observable<List<Widget>> observable = Observable.create(subscriber -> {
            try {
                List<Widget> widgets = new RestClient(getActivity()).getWidgets(GETWIDGETS_URL, token.getToken_type(), token.getAccess_token());
                if (widgets == null) {
                    return;
                }
                subscriber.onNext(widgets);
            } catch (IOException ioe) {
                subscriber.onError(ioe);
            }
        });
        compositeDisposable.add(observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(this::insertWidgets, exception -> {}));
    }

    /**
     * Filters widgets and gives filtered list to every widget fragment.
     */
    public void insertWidgets(List<Widget> widgets) {
        List<Widget> lineCharts = widgets.stream().filter(x -> x.getGraphType() == 1).collect(Collectors.toList());
        List<Widget> bartCharts = widgets.stream().filter(x -> x.getGraphType() == 2).collect(Collectors.toList());
        List<Widget> pieCharts = widgets.stream().filter(x -> x.getGraphType() == 3 || x.getGraphType() == 4).collect(Collectors.toList());

        ((LineChartFragment)adapter.getItem(0)).generateWidgets(lineCharts);
        ((BarChartFragment)adapter.getItem(1)).generateWidgets(bartCharts);
        ((PieChartFragment)adapter.getItem(2)).generateWidgets(pieCharts);
    }

    /**
     * Adapter which is used to format fragment into fragment pager and is implemented as innerclass.
     */
    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<Integer> mFragmentIconList = new ArrayList<>();
        private View view;

        Adapter(FragmentManager manager, View view) {
            super(manager);
            this.view = view;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, int icon) {
            mFragmentList.add(fragment);
            mFragmentIconList.add(icon);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            SpannableStringBuilder sb = new SpannableStringBuilder(" ");

            Drawable drawable = ContextCompat.getDrawable(view.getContext(), mFragmentIconList.get(position));
            Objects.requireNonNull(drawable).setBounds(0, 0, 110, 110);
            ImageSpan imageSpan = new ImageSpan(drawable);
            sb.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return sb;
        }
    }
}
