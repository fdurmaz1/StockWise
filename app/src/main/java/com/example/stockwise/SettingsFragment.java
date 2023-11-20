package com.example.stockwise;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

    private TextView txtFullName, txtUserName, txtEmail, txtPassword;
    private ImageView imgFullName, imgUserName, imgEmail, imgPassword;

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
        imgFullName = view.findViewById(R.id.imgFullName);
        imgUserName = view.findViewById(R.id.imgUserName);
        imgEmail = view.findViewById(R.id.imgEmail);
        imgPassword = view.findViewById(R.id.imgPassword);
        btnSignOut = view.findViewById(R.id.btnSignOut);

        // Retrieve user ID from SharedPreferences (example shown)
        userId = getActivity().getSharedPreferences("MySharedPref", MODE_PRIVATE).getInt("userid", -1);
        Log.d("SettingsFragment", "onCreateView: User ID retrieved: " + userId);

        if (userId != -1) {
            Log.d("SettingsFragment", "onCreateView: Fetching user info");
            new FetchUserInfoTask().execute("http://192.168.1.78/LoginRegister/fetch_user_info.php?userid=" + userId);
        }

        // Set listeners for ImageViews
        setUpEditListeners();

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutUser();
            }
        });

        return view;
    }

    private void setUpEditListeners() {
        imgFullName.setOnClickListener(v -> showEditDialog("fullname", txtFullName.getText().toString()));
        imgUserName.setOnClickListener(v -> showEditDialog("username", txtUserName.getText().toString()));
        imgEmail.setOnClickListener(v -> showEditDialog("email", txtEmail.getText().toString()));
        imgPassword.setOnClickListener(v -> showEditDialog("password", txtPassword.getText().toString()));
    }

    private void showEditDialog(String field, String currentValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + field);

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentValue);
        builder.setView(input);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateUserInfo(field, input.getText().toString());
                new FetchUserInfoTask().execute("http://192.168.1.78/LoginRegister/fetch_user_info.php?userid=" + userId);

            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateUserInfo(String field, String newValue) {
        // Get current values of all fields
        String currentFullName = txtFullName.getText().toString();
        String currentUserName = txtUserName.getText().toString();
        String currentEmail = txtEmail.getText().toString();
        String currentPassword = txtPassword.getText().toString(); // Adjust as per your logic

        // Update the value of the field that is being edited
        if ("fullname".equals(field)) {
            currentFullName = newValue;
        } else if ("username".equals(field)) {
            currentUserName = newValue;
        } else if ("email".equals(field)) {
            currentEmail = newValue;
        } else if ("password".equals(field)) {
            currentPassword = newValue; // Make sure this is handled securely
        }

        // Invoke UpdateUserInfoTask with all current values
        UpdateUserInfoTask(String.valueOf(userId), currentFullName, currentUserName, currentEmail, currentPassword);

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
            Log.d("SettingsFragment", "FetchUserInfoTask: Response received");
            if (result == null) {
                Log.e("SettingsFragment", "FetchUserInfoTask: Result is null");
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(result);
                txtFullName.setText(jsonObject.optString("fullname", "N/A"));
                txtUserName.setText(jsonObject.optString("username", "N/A"));
                txtEmail.setText(jsonObject.optString("email", "N/A"));
                Log.d("SettingsFragment", "FetchUserInfoTask: User info updated in UI");

                // Password is typically not retrieved for display
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("SettingsFragment", "FetchUserInfoTask: JSON parsing error", e);
                // Handle error
            }
        }
    }

    private void UpdateUserInfoTask(String userid, String fullname, String username, String email, String password) {
        Log.d("SettingsFragment", "UpdateUserInfoTask: Starting AsyncTask");

        // Async task to send data to server
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL("http://192.168.1.78/LoginRegister/update_user_info.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                    // POST data
                    String data = URLEncoder.encode("userid", "UTF-8") + "=" + URLEncoder.encode(userid, "UTF-8") +
                            "&" + URLEncoder.encode("fullname", "UTF-8") + "=" + URLEncoder.encode(fullname, "UTF-8") +
                            "&" + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8") +
                            "&" + URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8") +
                            "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");

                    writer.write(data);
                    writer.flush();
                    writer.close();
                    os.close();

                    // Get response
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
                super.onPostExecute(result);
                if (result == null) {
                    Log.e("SettingsFragment", "UpdateUserInfoTask: Result is null");
                    return;
                }
                Log.d("SettingsFragment", "UpdateUserInfoTask: Response received: " + result);
            }
        }.execute();
    }


}
