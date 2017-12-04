package com.bloodconnection.bluetoothconnection;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;


import butterknife.ButterKnife;
import butterknife.Bind;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    static boolean value = false;

    @Bind(R.id.input_name) EditText _nameText;
    @Bind(R.id.input_address) EditText _addressText;
    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_mobile) EditText _mobileText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.input_reEnterPassword) EditText _reEnterPasswordText;
    @Bind(R.id.btn_signup) Button _signupButton;
    @Bind(R.id.link_login) TextView _loginLink;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.Theme_AppCompat);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String name = _nameText.getText().toString();
        String country = _addressText.getText().toString();
        String email = _emailText.getText().toString();
        String age = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();

        if(!isOnline()) {
            Toast.makeText(getBaseContext(), "Please turn on network", Toast.LENGTH_LONG).show();
            return;
        } else {
            RetrieveFeedTask rfT = new RetrieveFeedTask(name, age, country, password, email, progressDialog);
            rfT.execute();
        }
    }


    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String address = _addressText.getText().toString();
        String email = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (address.isEmpty()) {
            _addressText.setError("Enter Valid Address");
            valid = false;
        } else {
            _addressText.setError(null);
        }


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (mobile.isEmpty()) {
            _mobileText.setError("Enter Valid Mobile Number");
            valid = false;
        } else {
            _mobileText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError("Password Do not match");
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
        }

        return valid;
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private boolean checkServer() {
        try (Socket s = new Socket("192.168.1.4", 8080)){
            return true;
        } catch (IOException ex) { /* ignore */ }
        return false;
    }

    class RetrieveFeedTask extends AsyncTask<Void, Void, Void> {

        public String name, age, country, pass, mail;
        public ProgressDialog pd;

        public RetrieveFeedTask(String name, String age, String country, String pass, String mail, ProgressDialog pd) {
            this.name = name;
            this.age = age;
            this.country = country;
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

                String result = null;
                try {
                    /*json.put("id", -1);
                    json.put("name", name);
                    json.put("country", country);
                    json.put("age", Integer.valueOf(age));
                    json.put("password", pass);
                    json.put("email", mail);*/
                    RequestBody body = new FormEncodingBuilder()
                            .add("name", name)
                            .add("country", country)
                            .add("age", age)
                            .add("pass", pass)
                            .add("mail", mail)
                            .build();
                    //RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());
                    Request request = new Request.Builder()
                            .url("http://192.168.43.253:8080/user/add")
                            .post(body)
                            .build();

                    Response response = client.newCall(request).execute();
                    result = response.body().string();
                    //Log.w("responseWW", result);
                    if(result.indexOf("success") != -1) {
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        onSignupSuccess();
                                        pd.dismiss();
                                    }
                                }, 3000);
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