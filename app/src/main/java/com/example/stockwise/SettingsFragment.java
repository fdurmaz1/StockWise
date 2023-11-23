package com.example.stockwise;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.stockwise.user.Login;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SettingsFragment extends Fragment {


    private EditText txtFullName, txtUserName, txtEmail, txtPassword;
    private Button btnUpdate, btnSignOut;
    private int userId;
    private boolean isPasswordEdited = false;
    private String originalPassword; // To store the original password

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize Views
        txtFullName = view.findViewById(R.id.txtFullName);
        txtUserName = view.findViewById(R.id.txtUserName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPassword = view.findViewById(R.id.txtPassword);
        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnSignOut = view.findViewById(R.id.btnSignOut);


        // Retrieve user ID from SharedPreferences
        userId = getActivity().getSharedPreferences("MySharedPref", MODE_PRIVATE).getInt("userid", -1);

        if (userId != -1) {
            new FetchUserInfoTask().execute("http://192.168.1.78/LoginRegister/fetch_user_info.php?userid=" + userId);
        }

        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Set the flag to true when the user edits the password
                isPasswordEdited = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed here
            }
        });

        btnUpdate.setOnClickListener(v -> updateUserInformation());
        btnSignOut.setOnClickListener(v -> signOutUser());

        return view;
    }


    private void updateUserInformation() {
        String fullName = txtFullName.getText().toString();
        String userName = txtUserName.getText().toString();
        String email = txtEmail.getText().toString();
        String password = isPasswordEdited ? txtPassword.getText().toString() : null;

        new UpdateUserInfoTask().execute(userId, fullName, userName, email, password);
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
                return response.toString();
            } catch (Exception e) {
                Log.e("UserInfo", "Error: ", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Log.e("SettingsFragment", "FetchUserInfoTask: Result is null");
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(result);
                // Update the EditText fields with the fetched information
                txtFullName.setText(jsonObject.optString("fullname", ""));
                txtUserName.setText(jsonObject.optString("username", ""));
                txtEmail.setText(jsonObject.optString("email", ""));

                // Assuming the password is not retrieved for security reasons
                // If you do get the password (hashed or tokenized), you can set it here
                // originalPassword = jsonObject.optString("passwordToken", "");

                Log.d("SettingsFragment", "FetchUserInfoTask: User info updated in UI");
            } catch (Exception e) {
                Log.e("SettingsFragment", "FetchUserInfoTask: JSON parsing error", e);
            }
        }
    }


    private class UpdateUserInfoTask extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {
            try {
                URL url = new URL("http://192.168.1.78/LoginRegister/update_user_info.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                // Building the data query
                String data = URLEncoder.encode("userid", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(params[0]), "UTF-8") +
                        "&" + URLEncoder.encode("fullname", "UTF-8") + "=" + URLEncoder.encode((String) params[1], "UTF-8") +
                        "&" + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode((String) params[2], "UTF-8") +
                        "&" + URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode((String) params[3], "UTF-8");

                // Only add the password parameter if it was edited
                if (params[4] != null) {
                    data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode((String) params[4], "UTF-8");
                }

                writer.write(data);
                writer.flush();
                writer.close();
                os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }


        @Override
        protected void onPostExecute(String result) {
            if (result != null && !result.contains("Failed")) { // Adjust this condition based on actual server response
                isPasswordEdited = false;
                Toast.makeText(getActivity(), "Information updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Failed to update information: " + result, Toast.LENGTH_SHORT).show();
            }
        }
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

}
