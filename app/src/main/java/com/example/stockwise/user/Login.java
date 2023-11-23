package com.example.stockwise.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stockwise.MainActivity;
import com.example.stockwise.R;
import com.google.android.material.textfield.TextInputEditText;
import com.vishnusivadas.advanced_httpurlconnection.PutData;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class Login extends AppCompatActivity {

    TextInputEditText textInputEditTextUsername, textInputEditTextPassword;
    Button buttonLogin;
    TextView textViewSignUp;
    static ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textInputEditTextUsername = findViewById(R.id.username);
        textInputEditTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewSignUp = findViewById(R.id.signUpText);
        progressBar = findViewById(R.id.progress);

        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignUp.class);
                startActivity(intent);
                finish();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = textInputEditTextUsername.getText().toString();
                String password = textInputEditTextPassword.getText().toString();

                if (!username.isEmpty() && !password.isEmpty()) {
                    progressBar.setVisibility(View.VISIBLE);

                    PerformNetworkRequest task = new PerformNetworkRequest(Login.this);
                    task.execute(username, password);
                } else {
                    Toast.makeText(getApplicationContext(), "All fields are required.", Toast.LENGTH_SHORT).show();
                }
            }
        });



//        buttonLogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String fullname, username,password, email;
//                username = String.valueOf(textInputEditTextUsername.getText());
//                password = String.valueOf(textInputEditTextPassword.getText());
//
//                if (!username.equals("") && !password.equals("")) {
//                    progressBar.setVisibility(View.VISIBLE);
//                    Handler handler = new Handler(Looper.getMainLooper());
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            //Starting Write and Read data with URL
//                            //Creating array for parameters
//                            String[] field = new String[2];
//                            field[0] = "username";
//                            field[1] = "password";
//                            //Creating array for data
//                            String[] data = new String[2];
//                            data[0] = username;
//                            data[1] = password;
//                            PutData putData = new PutData("http://192.168.1.83/LoginRegister/login.php", "POST", field, data);
//                            if (putData.startPut()) {
//                                if (putData.onComplete()) {
//                                    progressBar.setVisibility(View.GONE);
//                                    String result = putData.getResult();
//                                    if (result.equals("Login Success")){
//                                        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
//                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                                        startActivity(intent);
//                                        finish();
//                                    }else {
//                                        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
//                                    }
//                                    Log.i("PutData", result);
//                                }
//                            }
//                            //End Write and Read data with URL
//                        }
//                    });
//                }else {
//                    Toast.makeText(getApplicationContext(), "All fields are required.", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });


    }

    private static class PerformNetworkRequest extends AsyncTask<String, Void, String> {
        private final WeakReference<Login> activityReference;

        PerformNetworkRequest(Login context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            Login activity = activityReference.get();

            if (activity == null || activity.isFinishing()) {
                return null;
            }

            String username = strings[0];
            String password = strings[1];

            String result = ""; // Store the response from the network request

            try {
                String[] field = new String[2];
                field[0] = "username";
                field[1] = "password";

                String[] data = new String[2];
                data[0] = username;
                data[1] = password;

                PutData putData = new PutData("http://192.168.1.78/LoginRegister/login.php", "POST", field, data);

                if (putData.startPut()) {
                    if (putData.onComplete()) {
                        result = putData.getResult();
                    }
                }
            } catch (Exception e) {
                // Handle exceptions or errors as needed
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Login activity = activityReference.get();

            if (activity == null || activity.isFinishing()) {
                return;
            }

            activity.progressBar.setVisibility(View.GONE);

            if (result != null && result.contains("Login Success")) {
                // Assuming the server response is a JSON object with the userid
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    int userId = jsonObject.getInt("userid"); // Adjust this key as per your actual JSON response

                    // Store the userid in SharedPreferences
                    SharedPreferences sharedPreferences = activity.getSharedPreferences("MySharedPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("userid", userId);
                    editor.apply();

                    Toast.makeText(activity.getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
                    activity.startActivity(intent);
                    activity.finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                    // Handle JSON parsing error
                }
            } else {
                Toast.makeText(activity.getApplicationContext(), "Username or Password wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

}