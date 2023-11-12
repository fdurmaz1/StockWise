package com.example.stockwise;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdatePricesWorker extends Worker {

    public UpdatePricesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public Result doWork() {
        Log.d("UpdatePricesWorker", "Worker started");

        List<String> portfolioStocks = getPortfolioStocks();
        Log.d("UpdatePricesWorker", "Portfolio stocks: " + portfolioStocks);

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(getApplicationContext()));
            Log.d("UpdatePricesWorker", "Python started");
        }

        Python py = Python.getInstance();
        PyObject pyObject = py.getModule("myscript");

        for (String stockSymbol : portfolioStocks) {
            PyObject closePrice = pyObject.callAttr("get_recent_close_price", stockSymbol);
            Log.d("UpdatePricesWorker", "Stock: " + stockSymbol + ", Close Price: " + closePrice.toDouble());
            updateStockPriceInLocalDatabase(stockSymbol, closePrice.toDouble());
        }

        Log.d("UpdatePricesWorker", "Worker finished successfully");
        return Result.success();
    }

    private List<String> getPortfolioStocks() {
        List<String> portfolioStocks = new ArrayList<>();
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MySharedPref", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userid", -1);

        if (userId != -1) {
            // Assume we store portfolio stocks as a concatenated string
            String stocks = sharedPreferences.getString("portfolioStocks_" + userId, "");
            if (!stocks.isEmpty()) {
                portfolioStocks.addAll(Arrays.asList(stocks.split(","))); // Split by comma
            }
        }

        return portfolioStocks;
    }


    private void updateStockPriceInLocalDatabase(String stockSymbol, double price) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // For simplicity, stock prices are stored as "symbol:price"
        String existingData = sharedPreferences.getString("stockPrices", "");
        Map<String, String> priceMap = new HashMap<>();

        if (!existingData.isEmpty()) {
            for (String entry : existingData.split(",")) {
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    priceMap.put(parts[0], parts[1]);
                }
            }
        }

        // Update the price for the specific stock
        priceMap.put(stockSymbol, String.valueOf(price));

        // Convert the map back to a string
        StringBuilder updatedData = new StringBuilder();
        for (Map.Entry<String, String> entry : priceMap.entrySet()) {
            if (updatedData.length() > 0) updatedData.append(",");
            updatedData.append(entry.getKey()).append(":").append(entry.getValue());
        }

        editor.putString("stockPrices", updatedData.toString());
        editor.apply();
    }

}
