package be.kdg.androidbarometer.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import net.danlew.android.joda.JodaTimeAndroid;

import java.io.IOException;

import be.kdg.androidbarometer.R;
import be.kdg.androidbarometer.fragment.AboutUsFragment;
import be.kdg.androidbarometer.fragment.WidgetFragment;
import be.kdg.androidbarometer.fragment.NotificationsFragment;
import be.kdg.androidbarometer.fragment.UserSettingsFragment;
import be.kdg.androidbarometer.model.Token;
import be.kdg.androidbarometer.model.UserInformation;
import be.kdg.androidbarometer.other.InstanceIdService;
import be.kdg.androidbarometer.other.RestClient;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //Global attributes
    private static final String GETUSERINFO_URL = "http://10.134.216.25:8011/api/Android/UserInfo";
    private static final String POSTUSERINFO_URL = "http://10.134.216.25:8011/api/Android/UserInfo";
    private static final String POSTDEVICETOKEN_URL = "http://10.134.216.25:8011/api/Android/DeviceToken";

    private static final int REQUEST_LOGIN = 0;
    private Token token;
    private CompositeDisposable compositeDisposable;
    private MenuItem previousMenuItem;

    private TextView tvFirstName;
    private TextView tvLastName;
    private ImageView ivProfilePicture;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    /**
     * Creates the MainActivity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        JodaTimeAndroid.init(this);

        readSharedPrefs();
        initialiseViews();

        if (token.isSignedIn()) {
            synchronizeViews();
            configureFirebase();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, REQUEST_LOGIN);
        }
    }

    /**
     * Recreates the MainActivity with right credentials.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                recreate();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Closes drawer if open, otherwise do normal back press.
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Inflates the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Configures the menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mnuLogOut) {
            writeSharedPrefs(false);
            recreate();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Places selected fragment on MainActivity.
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        item.setCheckable(true);
        item.setChecked(true);
        if (previousMenuItem != null) {
            previousMenuItem.setChecked(false);
        }
        previousMenuItem = item;

        switch (id) {
            case R.id.nav_home:
                placeFragment(WidgetFragment.class);
                toolbar.setTitle(R.string.my_widgets);
                break;
            case R.id.nav_notifications:
                placeFragment(NotificationsFragment.class);
                toolbar.setTitle(R.string.notifications);
                break;
            case R.id.nav_settings:
                placeFragment(UserSettingsFragment.class);
                toolbar.setTitle(R.string.user_settings);
                break;
            case R.id.nav_about_us:
                placeFragment(AboutUsFragment.class);
                toolbar.setTitle(R.string.about_us);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Removes disposables.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    /**
     * Initialise all views.
     */
    private void initialiseViews() {
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        compositeDisposable = new CompositeDisposable();

        //Inflate header for initialisation of views on header
        View headerView = LayoutInflater.from(this).inflate(R.layout.nav_header_main, navigationView, false);
        navigationView.addHeaderView(headerView);
        tvFirstName = headerView.findViewById(R.id.tvFirstName);
        tvLastName = headerView.findViewById(R.id.tvLastName);
        ivProfilePicture = headerView.findViewById(R.id.ivProfilePicture);

        //Place home fragment at launch
        placeFragment(WidgetFragment.class);
        toolbar.setTitle(R.string.my_widgets);
    }

    /**
     * Reads token from shared preferences.
     */
    private void readSharedPrefs() {
        SharedPreferences sharedPref = (MainActivity.this).getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("token", "");
        token = new Token();
        if (!json.equals("")) {
            token = gson.fromJson(json, Token.class);
        }
    }

    /**
     * Clears token from shared preferences if log out.
     */
    private void writeSharedPrefs(boolean signedIn) {
        SharedPreferences sharedPref = (this).getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();

        if (!signedIn) {
            token.setSignedIn(false);
            token.setAccess_token("");
            token.setToken_type("");
        }

        String tokenJson = gson.toJson(token);
        editor.putString(getString(R.string.token), tokenJson);
        editor.apply();
    }


    /**
     * Updates views with user info.
     */
    public void synchronizeViews() {
        if (token.isSignedIn()) {
            retrieveUserInfo();
        } else {
            readSharedPrefs();
            if (token.isSignedIn()) {
                retrieveUserInfo();
            } else {
                Toast.makeText(this, getResources().getString(R.string.error_sync_message), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Retrieves user info trough api call.
     */
    public void retrieveUserInfo() {
        Observable<UserInformation> observable = Observable.create(subscriber -> {
            try {
                UserInformation userInformation = new RestClient(this).getData(GETUSERINFO_URL, UserInformation.class, token.getToken_type(), token.getAccess_token());
                subscriber.onNext(userInformation);
            } catch (IOException ioe) {
                subscriber.onError(ioe);
            }
        });
        compositeDisposable.add(observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(info -> {
            tvFirstName.setText(info.getFirstName());
            tvLastName.setText(info.getLastName());
            if (!info.getProfilePicture().equals("")) {
                byte[] photoArray = Base64.decode(info.getProfilePicture(), Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(photoArray, 0, photoArray.length);

                // Create the RoundedBitmapDrawable.
                RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(), bmp);
                roundDrawable.setCircular(true);
                ivProfilePicture.setImageDrawable(roundDrawable);
            }
        }, exception -> Toast.makeText(this, getResources().getString(R.string.error_sync_message), Toast.LENGTH_LONG).show()));
    }

    /**
     * Generic method for placing the right fragment on MainActivity.
     */
    private <T extends Fragment> void placeFragment(Class<T> type) {
        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment fragment = type.newInstance();
            ft.replace(R.id.c_main, fragment);
            ft.commit();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates user info in database trough api call.
     */
    public boolean sendUserInfo(String firstName, String lastName, String profilePicture) {
        RestClient restClient = new RestClient(MainActivity.this);

        AsyncTask<Void, Void, String> execute = new ExecuteNetworkOperationPostUserInfo(restClient, firstName, lastName, profilePicture);
        execute.execute();
        return true;
    }

    /**
     * Updates user device token in database trough api call.
     */
    public boolean configureFirebase() {
        InstanceIdService instanceIdService = new InstanceIdService();
        instanceIdService.onTokenRefresh();
        FirebaseMessaging.getInstance().subscribeToTopic("general");
        FirebaseMessaging.getInstance().subscribeToTopic("weeklyreview");
        RestClient restClient = new RestClient(MainActivity.this);

        AsyncTask<Void, Void, String> execute = new ExecuteNetworkOperationPostDeviceToken(restClient, instanceIdService.getToken());
        execute.execute();
        return true;
    }

    /**
     * This subclass handles the network operations in a new thread.
     * It starts the progress bar, makes the API call, and ends the progress bar.
     */
    public class ExecuteNetworkOperationPostUserInfo extends AsyncTask<Void, Void, String> {

        private RestClient restClient;
        private ProgressDialog progressDialog;
        private String firstname;
        private String lastname;
        private String profilePicture;

        ExecuteNetworkOperationPostUserInfo(RestClient restClient, String firstname, String lastname, String profilePicture) {
            this.restClient = restClient;
            this.firstname = firstname;
            this.lastname = lastname;
            this.profilePicture = profilePicture;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(MainActivity.this, R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getResources().getString(R.string.saving));
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                String payload = String.format("{\"FirstName\":\"%s\",\"LastName\":\"%s\",\"ProfilePicture\":\"%s\"}", firstname, lastname, profilePicture);
                if (!restClient.postData(POSTUSERINFO_URL, payload, token.getToken_type(), token.getAccess_token())) {
                    throw new Exception();
                }
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.error_save), Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
        }
    }

    /**
     * This subclass handles the network operations in a new thread.
     */
    public class ExecuteNetworkOperationPostDeviceToken extends AsyncTask<Void, Void, String> {

        private RestClient restClient;
        private String deviceToken;

        ExecuteNetworkOperationPostDeviceToken(RestClient restClient, String deviceToken) {
            this.restClient = restClient;
            this.deviceToken = deviceToken;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                String payload = String.format("{\"DeviceToken\":\"%s\"}", deviceToken);
                if (!restClient.postData(POSTDEVICETOKEN_URL, payload, token.getToken_type(), token.getAccess_token())) {
                    throw new Exception();
                }
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.error_general), Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }
}
