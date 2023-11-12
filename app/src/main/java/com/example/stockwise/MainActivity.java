package com.example.stockwise;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.os.Bundle;
import android.util.Log;

import com.example.stockwise.databinding.ActivityMainBinding;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.portfolio) {
                replaceFragment(new PortfolioFragment());
            } else if (item.getItemId() == R.id.news) {
                replaceFragment(new NewsFragment());
            } else if (item.getItemId() == R.id.settings) {
                replaceFragment(new SettingsFragment());
            }
            return true;
        });

        scheduleUpdatePricesWorker();
    }

    private void scheduleUpdatePricesWorker() {
        PeriodicWorkRequest updatePricesWorkRequest =
                new PeriodicWorkRequest.Builder(UpdatePricesWorker.class, 24, TimeUnit.HOURS)
                        .addTag("updatePricesTag")
                        .build();

        // Enqueue the work request but only if it's not already scheduled
        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork("updatePrices",
                        ExistingPeriodicWorkPolicy.KEEP,
                        updatePricesWorkRequest);

        Log.d("MainActivity", "UpdatePricesWorker scheduled or already running");
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }



}