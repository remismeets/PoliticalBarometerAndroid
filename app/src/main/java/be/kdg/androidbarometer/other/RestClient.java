package be.kdg.androidbarometer.other;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import be.kdg.androidbarometer.model.AlertDTO;
import be.kdg.androidbarometer.model.Widget;

public class RestClient {
    private Context context;

    public RestClient(Context context) {
        this.context = context;
    }

    private boolean checkNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = Objects.requireNonNull(connectivityManager).getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Used for token retrieval.
     */
    public <T> T postData(String stringUrl, String payload, Class<T> type) throws IOException {
        if (checkNetwork()) {
            URL url = new URL(stringUrl);
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();

            uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            uc.setRequestMethod("POST");
            uc.setDoInput(true);
            uc.setInstanceFollowRedirects(false);
            uc.connect();
            OutputStreamWriter writer = new OutputStreamWriter(uc.getOutputStream(), "UTF-8");
            writer.write(payload);
            writer.close();

            if (uc.getResponseCode() == 200) {
                Reader reader = new InputStreamReader(uc.getInputStream());
                Gson gson = new GsonBuilder().create();
                T t = gson.fromJson(reader, type);
                reader.close();

                uc.disconnect();
                return t;
            }
        }
        return null;
    }

    /**
     * Used for creating user.
     */
    public boolean postData(String stringUrl, String payload) throws IOException {
        if (checkNetwork()) {
            URL url = new URL(stringUrl);
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();

            uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            uc.setRequestProperty("Accept","application/json");
            uc.setRequestMethod("POST");
            uc.setDoInput(true);
            uc.setDoOutput(true);

            uc.connect();

            DataOutputStream os = new DataOutputStream(uc.getOutputStream());
            os.writeBytes(payload);

            os.flush();
            os.close();

            if (uc.getResponseCode() != 200) {
                uc.disconnect();
                return false;
            }

            uc.disconnect();
            return true;
        }
        return false;
    }

    /**
     * Used for updating user
     * User for updating device token of user
     */
    public boolean postData(String stringUrl, String payload, String tokenType, String token) throws IOException {
        if (checkNetwork()) {
            URL url = new URL(stringUrl);
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();

            uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            uc.setRequestProperty("Accept","application/json");
            uc.setRequestProperty("Authorization", String.format("%s %s", tokenType, token));
            uc.setRequestMethod("POST");
            uc.setDoInput(true);
            uc.setDoOutput(true);

            uc.connect();

            DataOutputStream os = new DataOutputStream(uc.getOutputStream());
            os.writeBytes(payload);

            os.flush();
            os.close();

            if (uc.getResponseCode() != 200) {
                uc.disconnect();
                return false;
            }

            uc.disconnect();
            return true;
        }
        return false;
    }

    /**
     * Used for retrieving user info
     */
    public <T> T getData(String stringUrl, Class<T> type, String tokenType, String token) throws IOException {
        if (checkNetwork()) {
            URL url = new URL(stringUrl);
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();

            uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            uc.setRequestProperty("Authorization", String.format("%s %s", tokenType, token));
            uc.setRequestMethod("GET");
            uc.setDoInput(true);
            uc.setInstanceFollowRedirects(false);
            uc.connect();

            Reader reader = new InputStreamReader(uc.getInputStream());
            Gson gson = new GsonBuilder().create();
            T t = gson.fromJson(reader, type);
            reader.close();
            uc.disconnect();
            return t;
        }
        return null;
    }

    /**
     * Used for retrieving widgets
     */
    public List<Widget> getWidgets(String stringUrl, String tokenType, String token) throws IOException {
        if (checkNetwork()) {
            URL url = new URL(stringUrl);
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();

            uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            uc.setRequestProperty("Authorization", String.format("%s %s", tokenType, token));
            uc.setRequestMethod("GET");
            uc.setDoInput(true);
            uc.setInstanceFollowRedirects(false);
            uc.connect();

            Reader reader = new InputStreamReader(uc.getInputStream());
            if (uc.getResponseCode() != 200) {
                uc.disconnect();
                return null;
            }
            Gson gson = new GsonBuilder().create();
            List<Widget> widgets = gson.fromJson(reader, new TypeToken<List<Widget>>(){}.getType());
            reader.close();
            uc.disconnect();
            return widgets;
        }
        return null;
    }

    /**
     * Used for retrieving alerts.
     */
    public List<AlertDTO> getAlerts(String stringUrl, String tokenType, String token) throws IOException {
        if (checkNetwork()) {
            URL url = new URL(stringUrl);
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();

            uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            uc.setRequestProperty("Authorization", String.format("%s %s", tokenType, token));
            uc.setRequestMethod("GET");
            uc.setDoInput(true);
            uc.setInstanceFollowRedirects(false);
            uc.connect();

            Reader reader = new InputStreamReader(uc.getInputStream());
            if (uc.getResponseCode() != 200) {
                uc.disconnect();
                return null;
            }
            Gson gson = new GsonBuilder().create();
            List<AlertDTO> alertDTOS = gson.fromJson(reader, new TypeToken<List<AlertDTO>>(){}.getType());
            reader.close();
            uc.disconnect();
            return alertDTOS;
        }
        return null;
    }
}
