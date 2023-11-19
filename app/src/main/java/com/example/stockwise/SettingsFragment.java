package com.example.stockwise;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import com.example.stockwise.user.Login;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SettingsFragment extends Fragment {

    private TextView txtFullName, txtUserName, txtEmail, txtPassword;
    private int userId; // User ID should be retrieved from SharedPreferences
    private Button btnSignOut;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize TextViews
        txtFullName = view.findViewById(R.id.txtFullName);
        txtUserName = view.findViewById(R.id.txtUserName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPassword = view.findViewById(R.id.txtPassword);
        btnSignOut = view.findViewById(R.id.btnSignOut);

        // Retrieve user ID from SharedPreferences (example shown)
        userId = getActivity().getSharedPreferences("MySharedPref", MODE_PRIVATE).getInt("userid", -1);
        Log.d("UserInfo", "User ID: " + userId); // Log the user ID

        if (userId != -1) {
            new FetchUserInfoTask().execute("http://192.168.1.78/LoginRegister/fetch_user_info.php?userid=" + userId);
        }

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutUser();
            }
        });

        return view;
    }

    private void signOutUser() {
        // Clear shared preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        redirectToLogin();
    }

    private void redirectToLogin() {
        // Replace with your actual navigation logic
        Intent intent = new Intent(getActivity(), Login.class);
        startActivity(intent);
        getActivity().finish();
    }

    private class FetchUserInfoTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                Log.d("UserInfo", "Response: " + response.toString()); // Log the response
                return response.toString();
            } catch (Exception e) {
                Log.e("UserInfo", "Error: ", e); // Log the error
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                txtFullName.setText(jsonObject.optString("fullname", "N/A"));
                txtUserName.setText(jsonObject.optString("username", "N/A"));
                txtEmail.setText(jsonObject.optString("email", "N/A"));
                // Password is typically not retrieved for display
            } catch (Exception e) {
                e.printStackTrace();
                // Handle error
            }
        }
    }
}
