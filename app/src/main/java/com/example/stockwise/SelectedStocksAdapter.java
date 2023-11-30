package com.example.stockwise;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.vishnusivadas.advanced_httpurlconnection.PutData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class SelectedStocksAdapter extends RecyclerView.Adapter<SelectedStocksAdapter.SelectedStocksViewHolder> {
    private final Context context;
    private List<String> selectedStocks;
    private OnStockSelectedListener listener;
    private PortfolioFragment portfolioFragment;

    interface OnStockSelectedListener {
        void onStockSelected(String selectedItem);
    }

    // Constructor to set the data
    public SelectedStocksAdapter(Context context, List<String> selectedStocks, OnStockSelectedListener listener) {
        this.context = context; // Initialize the context
        this.selectedStocks = selectedStocks;
        this.listener = listener;
    }

    // ViewHolder for the adapter
    public static class SelectedStocksViewHolder extends RecyclerView.ViewHolder {
        TextView textStockSymbol;
        TextView textStockName;
        TextView textStockClosePrice;
        ImageView imgPriceChange;
        ImageView imageView4;
        public SelectedStocksViewHolder(View itemView) {
            super(itemView);
            textStockSymbol = itemView.findViewById(R.id.textStockSymbol);
            textStockName = itemView.findViewById(R.id.textStockName);
            textStockClosePrice = itemView.findViewById(R.id.textStockClosePrice);
            imageView4 = itemView.findViewById(R.id.imageView4);
            imgPriceChange = itemView.findViewById(R.id.imgPriceChange);
            // Initialize other views here if required
        }
    }

    @NonNull
    @Override
    public SelectedStocksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_item, parent, false);
        return new SelectedStocksViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedStocksViewHolder holder, int position) {
        String combined = selectedStocks.get(position);
        String[] parts = combined.split("\n");

        holder.textStockSymbol.setText(parts[0]);
        holder.textStockName.setText(parts[1]);

        Double[] closePriceData = getRecentClosePrice(parts[0]);
        Double closePrice = closePriceData[0];
        Double priceChange = closePriceData[1];

        if (closePrice != null) {
            double roundedPriceChange = Math.round(priceChange * 100.0) / 100.0;
            holder.textStockClosePrice.setText(String.format("%.2f", closePrice));

            if (roundedPriceChange > 0) {
                holder.textStockClosePrice.setTextColor(Color.parseColor("#008000")); // Green for positive change
                holder.imgPriceChange.setImageResource(R.drawable.baseline_arrow_upward_24);
            } else if (roundedPriceChange < 0) {
                holder.textStockClosePrice.setTextColor(Color.parseColor("#FF0000")); // Red for negative change
                holder.imgPriceChange.setImageResource(R.drawable.baseline_arrow_downward_24);
            } else {
                holder.textStockClosePrice.setTextColor(Color.parseColor("#000000")); // Black for no change
                holder.imgPriceChange.setImageResource(R.drawable.neutral_symbol); // Neutral icon
            }
        } else {
            holder.textStockClosePrice.setText("N/A");
            holder.textStockClosePrice.setTextColor(Color.parseColor("#000000")); // Reset to default color
            holder.imgPriceChange.setImageResource(R.drawable.neutral_symbol); // Neutral icon for missing data
        }


        holder.imageView4.setOnClickListener(v -> {
            SharedPreferences sh = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
            int userId = sh.getInt("userid", -1);
            Log.d("StockCheck", "User ID: " + userId);

            if (userId != -1) {
                Log.d("StockCheck", "Initiating check for stock: " + parts[0]);
                isStockInPortfolio(parts[0], userId, isInPortfolio -> {
                    Log.d("StockCheck", "Received isInPortfolio: " + isInPortfolio);
                    if (!isInPortfolio) {
                        Log.d("StockCheck", "Stock already in portfolio, not adding: " + parts[0]);
                        Toast.makeText(context, "This stock is already in your portfolio", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("StockCheck", "Stock not in portfolio, adding: " + parts[0]);
                        addStockToPortfolio(parts[0], parts[1], userId, position);
                    }
                });
            } else {
                Log.d("StockCheck", "Invalid User ID");
            }
        });
    }

    private void addStockToPortfolio(String symbol, String name, int userId, int position) {
        // Code to add stock to the portfolio
        Toast.makeText(context, symbol + " added to your portfolio", Toast.LENGTH_SHORT).show();
        String selectedItem = selectedStocks.get(position);
        if (listener != null) {
            listener.onStockSelected(selectedItem);
        }
        StockManager.getInstance().addStock(selectedItem);
        if (portfolioFragment != null) {
            portfolioFragment.addToPortfolio(selectedItem);
        }
        // Add the stock to the database
        String[] field = new String[]{"userid", "symbol", "name"};
        String[] data = new String[]{String.valueOf(userId), symbol, name};
        PutData putData = new PutData("http://192.168.1.79/LoginRegister/add_portfolio_entry.php", "POST", field, data);
        if (putData.startPut()) {
            if (putData.onComplete()) {
                String result = putData.getResult();
                // Handle the response from adding to the database
            }
        }
    }


    private Double[] getRecentClosePrice(String symbol) {
        Python py = Python.getInstance();
        PyObject pyObject = py.getModule("myscript");
        PyObject recentClosePriceData = pyObject.callAttr("get_recent_close_price", symbol);

        Double[] closePriceData = new Double[2];
        closePriceData[0] = null; // Default to null
        closePriceData[1] = 0.0; // Default price change to 0.0

        try {
            String RCPDstring = recentClosePriceData.toString().replace("(", "").replace(")", "");
            System.out.println(RCPDstring);

            if (!"None, None".equals(RCPDstring)) {
                String[] tokens = RCPDstring.split(",");
                closePriceData[0] = Double.parseDouble(tokens[0]);
                closePriceData[1] = Double.parseDouble(tokens[1]);
            }

        } catch (PyException e) {
            e.printStackTrace(); // Handle any exceptions here
        }

        return closePriceData;
    }



    @Override
    public int getItemCount() {
        return selectedStocks.size();
    }

    public interface StockCheckListener {
        void onStockChecked(boolean isInPortfolio);
    }



    // Method to check if the stock already exists in the user's portfolio
    private void isStockInPortfolio(String symbol, int userId, StockCheckListener listener) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    String urlString = "http://192.168.1.79/LoginRegister/check_stock_in_portfolio.php?userid=" + userId + "&symbol=" + URLEncoder.encode(symbol, "UTF-8");
                    Log.d("StockCheck", "Checking URL: " + urlString);
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    int responseCode = conn.getResponseCode();
                    Log.d("StockCheck", "HTTP Response Code: " + responseCode);

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        Log.d("StockCheck", "Response: " + response.toString().trim());

                        // Check if the response indicates the stock is already in the portfolio
                        return response.toString().trim().equals("Stock not in portfolio");
                    }
                } catch (Exception e) {
                    Log.e("StockCheck", "Error checking stock in portfolio", e);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                listener.onStockChecked(result);
            }
        }.execute();
    }




}