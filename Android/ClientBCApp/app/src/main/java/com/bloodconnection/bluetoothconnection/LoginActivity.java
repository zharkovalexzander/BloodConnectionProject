package com.bloodconnection.bluetoothconnection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import add.bloodconnection.common.misc.PersistantStorage;
import butterknife.ButterKnife;
import butterknife.Bind;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.btn_login) Button _loginButton;
    @Bind(R.id.link_signup) TextView _signupLink;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        PersistantStorage.init(this);
        
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
        //Log.w("responseWW", retrieve() == null ? "empty" : retrieve());
        if(retrieve() != null) {
            onLoginSuccess(retrieve());
        }
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        //progressDialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        LoginTask lt = new LoginTask(password, email, progressDialog);
        lt.execute();

    }

    private void save(String save) {
        /*SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.id), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.id), save);
        editor.commit();*/
        PersistantStorage.addProperty("id", save);
    }

    private String retrieve() {
        /*SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.id), Context.MODE_PRIVATE);
        String mac = sharedPref.getString(getString(R.string.id), null);*/
        String mac = PersistantStorage.getProperty("id");
        return mac;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess(String value) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(intent, REQUEST_SIGNUP);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    class LoginTask extends AsyncTask<Void, Void, Void> {

        public String pass, mail;
        public ProgressDialog pd;

        public LoginTask(String pass, String mail, ProgressDialog pd) {
            this.pass = pass;
            this.mail = mail;
            this.pd = pd;
        }

        protected Void doInBackground(Void ... voids) {
            try {
                JSONObject json = new JSONObject();
                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(15, TimeUnit.SECONDS);
                client.setReadTimeout(15, TimeUnit.SECONDS);
                try {
                    RequestBody body = new FormEncodingBuilder()
                            .add("pass", pass)
                            .add("email", mail)
                            .build();

                    Request request = new Request.Builder()
                            .url("http://192.168.43.253:8080/user/find/auth")
                            .post(body)
                            .build();

                    Response response = client.newCall(request).execute();
                    final String rep = response.body().string();
                    //Log.w("responseWW", Integer.valueOf(rep).toString());
                    if(Integer.valueOf(rep) != -1) {
                        //Log.w("responseWW", "here");
                        //pd.dismiss();
                        if(retrieve() == null) {
                            save(Integer.valueOf(rep).toString());
                        }
                        onLoginSuccess(Integer.valueOf(rep).toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {}
            return null;
        }

        protected void onPostExecute(Void voids) {
            // здесь можете обрабатывать ошибки при работе с сетью
        }
    }
}
