package mms.project.qr_attendance;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ActivityLogin extends AppCompatActivity {

    private static final String URL_LOGIN = "https://www.kusss.jku.at/kusss/login.action";
    private static final String LOGIN_FAILED = "KUSSS | Login gescheitert";
    public static final String KEY_MATRNR = "MatrNr";
    public static final String KEY_NAME = "Name";
    private UserLoginTask mAuthTask = null;

    // UI references
    private AutoCompleteTextView mMatrNrView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mMatrNrView = findViewById(R.id.matrnr);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });

        Button mMatrNrSignInButton = findViewById(R.id.matrnr_sign_in_button);
        mMatrNrSignInButton.setOnClickListener(view -> attemptLogin());
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }
        // Reset errors.
        mMatrNrView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String matrNr = mMatrNrView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid matrNr.
        if (TextUtils.isEmpty(matrNr)) {
            mMatrNrView.setError(getString(R.string.error_field_required));
            focusView = mMatrNrView;
            cancel = true;
        } else if (!isMatrNrValid(matrNr)) {
            mMatrNrView.setError(getString(R.string.error_invalid_matrNr));
            focusView = mMatrNrView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(matrNr, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isMatrNrValid(String matrNr) {
        return matrNr.matches("^k[0-9]+");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mMatrNr;
        private final String mPassword;

        UserLoginTask(String matrNr, String password) {
            mMatrNr = matrNr;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String USER_AGENT = "Mozilla/5.0";
                URL url = new URL(URL_LOGIN);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("User-Agent", USER_AGENT);
                conn.setRequestProperty("Accept-Language", "UTF-8");

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("j_username", mMatrNr)
                        .appendQueryParameter("j_password", mPassword);
                String query = builder.build().getEncodedQuery();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                if (query != null)
                    writer.write(query);
                writer.flush();
                os.flush();
                writer.close();
                os.close();
                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader rd = new BufferedReader(new InputStreamReader(in));
                String line;
                StringBuilder responseBuffer = new StringBuilder();
                while ((line = rd.readLine()) != null) {
                    responseBuffer.append(line).append("\n");
                }
                in.close();
                rd.close();
                String response = responseBuffer.toString();
                if (response.contains(LOGIN_FAILED)) {
                    return false;
                } else {
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(KEY_MATRNR, mMatrNr);
                setResult(RESULT_OK, returnIntent);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_login_data));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

