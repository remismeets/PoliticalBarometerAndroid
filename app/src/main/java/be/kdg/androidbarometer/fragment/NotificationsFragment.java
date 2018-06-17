package be.kdg.androidbarometer.fragment;


import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;


import java.io.IOException;
import java.util.List;
import java.util.Objects;

import be.kdg.androidbarometer.R;
import be.kdg.androidbarometer.activity.MainActivity;
import be.kdg.androidbarometer.model.AlertDTO;
import be.kdg.androidbarometer.model.Token;
import be.kdg.androidbarometer.other.RestClient;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class NotificationsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    //Global attributes
    private static final String GETNOTIFICATIONS_URL = "http://10.134.216.25:8011/api/Android/Alerts";

    private View view;
    private Token token;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView lvNotifications;
    private CompositeDisposable compositeDisposable;

    public NotificationsFragment() {
        //Required empty public constructor
    }

    /**
     * Creates the NotificationsFragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_notifications, container, false);

        readSharedPrefs();
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
        retrieveNotifications();
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Initialise all views.
     */
    private void initialiseViews() {
        compositeDisposable = new CompositeDisposable();
        lvNotifications = view.findViewById(R.id.lvNotifications);
        retrieveNotifications();

        swipeRefreshLayout = view.findViewById(R.id.fragment_notifications);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.colorPrimary), ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.colorPrimaryDark), ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.colorPrimaryDarkest));
        swipeRefreshLayout.setOnRefreshListener(this);
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
     * Adds event handlers to views.
     */
    private void addEventHandlers() {
        lvNotifications.setOnScrollListener(new AbsListView.OnScrollListener() {
            private boolean scrollEnabled;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (lvNotifications == null || lvNotifications.getChildCount() == 0) ? 0 : lvNotifications.getChildAt(0).getTop();

                boolean newScrollEnabled = firstVisibleItem == 0 && topRowVerticalPosition >= 0;

                if (null != swipeRefreshLayout && scrollEnabled != newScrollEnabled) {
                    swipeRefreshLayout.setEnabled(newScrollEnabled);
                    scrollEnabled = newScrollEnabled;
                }
            }
        });
    }

    /**
     * Retrieves notifications trough api call.
     */
    public void retrieveNotifications() {
        Observable<List<AlertDTO>> observable = Observable.create(subscriber -> {
            try {
                List<AlertDTO> alertDTOS = new RestClient(getActivity()).getAlerts(GETNOTIFICATIONS_URL, token.getToken_type(), token.getAccess_token());
                if (alertDTOS == null) {
                    return;
                }
                subscriber.onNext(alertDTOS);
            } catch (IOException ioe) {
                subscriber.onError(ioe);
            }
        });
        compositeDisposable.add(observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(alerts -> {
            NotificationAdapter notificationAdapter = new NotificationAdapter(getActivity(), alerts);
            lvNotifications.setAdapter(notificationAdapter);
            }, exception -> Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show()));
    }

    /**
     * Adapter which is used to format notifications into listview and is implemented as innerclass.
     */
    private class NotificationAdapter extends ArrayAdapter<AlertDTO> {
        NotificationAdapter(Context context, List<AlertDTO> notificationList) {
            super(context, 0, notificationList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            AlertDTO alertDTO = getItem(position);
            TextView tvSubject;
            TextView tvTimeStamp;
            ImageView ivDot;
            ImageView ivIcon;

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_notification, null);
            tvSubject = convertView.findViewById(R.id.tvSubject);
            tvTimeStamp = convertView.findViewById(R.id.tvTimeStamp);
            ivDot = convertView.findViewById(R.id.ivDot);
            ivIcon = convertView.findViewById(R.id.ivIcon);

            switch (Objects.requireNonNull(alertDTO).getAlertType()) {
                case 1: tvSubject.setText(String.format("%s is nu trending", alertDTO.getName()));
                        ivIcon.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.ic_trending));
                break;
                case 2: tvSubject.setText(alertDTO.getName());
                       ivIcon.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.ic_weekly_review));
                break;
                default: tvSubject.setText(alertDTO.getName());
            }
            tvTimeStamp.setText(String.format("Ongeveer %s uur geleden", alertDTO.getHoursPast()));
            if (alertDTO.isRead()) {
                ivDot.setImageDrawable(null);
            }

            return convertView;
        }
    }
}
