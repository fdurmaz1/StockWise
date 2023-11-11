package com.example.stockwise;

import android.content.Context;
import android.content.SharedPreferences;
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

        ImageView imageView4;
        public SelectedStocksViewHolder(View itemView) {
            super(itemView);
            textStockSymbol = itemView.findViewById(R.id.textStockSymbol);
            textStockName = itemView.findViewById(R.id.textStockName);
            textStockClosePrice = itemView.findViewById(R.id.textStockClosePrice);
            imageView4 = itemView.findViewById(R.id.imageView4);
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

        String selectedSymbol = parts[0]; // Get the stock symbol
        String selectedName = parts[1];

        // Fetch close price for the current symbol and update the respective TextView
        double closePrice = getRecentClosePrice(selectedSymbol);

        if (closePrice != -1) {
            String formattedClosePrice = String.format("%.2f", closePrice);
            holder.textStockClosePrice.setText(formattedClosePrice);
        } else {
            holder.textStockClosePrice.setText("N/A"); // Set default message when close price isn't available
        }

        // Plus sign click listener
        holder.imageView4.setOnClickListener(v -> {

            Toast.makeText(context, selectedSymbol + " AAND " + selectedName + " ADDED", Toast.LENGTH_SHORT).show();
            String selectedItem = selectedStocks.get(position);
            if (listener != null) {
                listener.onStockSelected(selectedItem);
            }

            // Add the selected stock to the shared list
            StockManager.getInstance().addStock(selectedItem);
            // Notify any attached fragment or activity of the stock addition
            // Replace 'portfolioFragment' with your actual fragment instance
            if (portfolioFragment != null) {
                portfolioFragment.addToPortfolio(selectedItem);
            }
            SharedPreferences sh = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
            int userId = sh.getInt("userid", -1);
            if (userId != -1) {
            String[] field = new String[]{"userid", "symbol", "name"};
            String[] data = new String[]{String.valueOf(userId), selectedSymbol, selectedName};
            PutData putData = new PutData("http://192.168.1.82/LoginRegister/add_portfolio_entry.php", "POST", field, data);
            if (putData.startPut()) {
                if (putData.onComplete()) {
                    String result = putData.getResult();
                    // Handle the response
                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
                }
            }
            }
        });

    }

    private double getRecentClosePrice(String symbol) {
        Python py = Python.getInstance();
        PyObject pyObject = py.getModule("myscript");
        PyObject getRecentClosePrice = pyObject.callAttr("get_recent_close_price", symbol);

        double closePrice = -1; // Set a default value in case of failure

        try {
            // Attempt to retrieve the close price from the Python script
            closePrice = getRecentClosePrice.toDouble();
        } catch (PyException e) {
            e.printStackTrace(); // Handle any exceptions here
        }

        return closePrice;
    }


    @Override
    public int getItemCount() {
        return selectedStocks.size();
    }
}
