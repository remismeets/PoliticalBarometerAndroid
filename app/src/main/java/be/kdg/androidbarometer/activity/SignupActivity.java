package be.kdg.androidbarometer.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import be.kdg.androidbarometer.R;
import be.kdg.androidbarometer.model.Token;
import be.kdg.androidbarometer.other.RestClient;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity {
    //Global attributes
    private static final String POSTREGISTER_URL = "http://10.134.216.25:8011/api/Android/Register";
    private static final String POSTTOKEN_URL = "http://10.134.216.25:8011/Token";

    @BindView(R.id.etEmail)
    EditText etEmail;
    @BindView(R.id.etPassword)
    EditText etPassword;
    @BindView(R.id.etReenterPassword)
    EditText etReenterPassword;
    @BindView(R.id.btnSignUp)
    Button btnSignUp;
    @BindView(R.id.lnLogin)
    TextView lnLogin;

    /**
     * Creates the SignupActivity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        addEventHandlers();
    }

    /**
     * Disables the back button to return to MainActivity.
     */
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    /**
     * Adds event handlers to views.
     */
    private void addEventHandlers() {
        btnSignUp.setOnClickListener(v -> signup());

        lnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        });
    }

    /**
     * Writes token to shared preferences for later use.
     */
    private void writeSharedPrefs(Token token) {
        SharedPreferences sharedPref = (SignupActivity.this).getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String tokenJson = gson.toJson(token);
        editor.putString(getString(R.string.token), tokenJson);
        editor.apply();
    }

    /**
     * Handles user sign up.
     */
    public void signup() {
        if (validate()) {
            btnSignUp.setEnabled(false);

            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            RestClient restClient = new RestClient(SignupActivity.this);

            AsyncTask<Void, Void, String> execute = new SignupActivity.ExecuteNetworkOperation(restClient, email, password);
            execute.execute();
        }
    }

    /**
     * Gets called when user signs in successfully and closes sign up activity.
     */
    public void onSignupSuccess() {
        btnSignUp.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    /**
     * Gets called when user fails to sign up and shows toast.
     */
    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.error_register_message), Toast.LENGTH_LONG).show();

        btnSignUp.setEnabled(true);
    }

    /**
     * Validates if given credentials are correct.
     */
    public boolean validate() {
        boolean valid = true;

        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String reEnterPassword = etReenterPassword.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getResources().getString(R.string.error_invalid_mail));
            valid = false;
        } else {
            etEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 4) {
            etPassword.setError(getResources().getString(R.string.error_invalid_password));
            valid = false;
        } else {
            etPassword.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || !(reEnterPassword.equals(password))) {
            etReenterPassword.setError(getResources().getString(R.string.error_invalid_password_match));
            valid = false;
        } else {
            etReenterPassword.setError(null);
        }
        return valid;
    }

    /**
     * This subclass handles the network operations in a new thread.
     * It starts the progress bar, makes the API call, and ends the progress bar.
     */
    public class ExecuteNetworkOperation extends AsyncTask<Void, Void, String> {

        private RestClient restClient;
        private Token token;
        private ProgressDialog progressDialog;
        private String username;
        private String password;

        ExecuteNetworkOperation(RestClient restClient, String username, String password) {
            this.restClient = restClient;
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(SignupActivity.this, R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getResources().getString(R.string.register));
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                String payload = String.format("{\"Email\":\"%s\",\"Password\":\"%s\"}", username, password);
                if(restClient.postData(POSTREGISTER_URL, payload)) {
                    payload = String.format("username=%s&password=%s&grant_type=password", username, password);
                    token = restClient.postData(POSTTOKEN_URL, payload, Token.class);
                }
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.error_general), Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            if (token != null) {
                token.setSignedIn(true);
                writeSharedPrefs(token);
                onSignupSuccess();
            }
            else {
                onSignupFailed();
            }
        }
    }
}
