package com.example.stockwise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.util.List;

public class PortfolioStocksAdapter extends RecyclerView.Adapter<PortfolioStocksAdapter.PortfolioStocksViewHolder>{

    private List<String> portfolioStocks;


    public PortfolioStocksAdapter(List<String> portfolioStocks) {
        this.portfolioStocks = portfolioStocks;
    }

    @NonNull
    @Override
    public PortfolioStocksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_item, parent, false);
        return new PortfolioStocksViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PortfolioStocksViewHolder holder, int position) {
        String combined = portfolioStocks.get(position);
        String[] parts = combined.split("\n");

        holder.textStockSymbol.setText(parts[0]);
        holder.textStockName.setText(parts[1]);

        String selectedSymbol = parts[0]; // Get the stock symbol

        // Fetch close price for the current symbol and update the respective TextView
        double closePrice = getRecentClosePrice(selectedSymbol);

        if (closePrice != -1) {
            String formattedClosePrice = String.format("%.2f", closePrice);
            holder.textStockClosePrice.setText(formattedClosePrice);
        } else {
            holder.textStockClosePrice.setText("N/A"); // Set default message when close price isn't available
        }
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

    public static class PortfolioStocksViewHolder extends RecyclerView.ViewHolder {
        TextView textStockSymbol;
        TextView textStockName;
        TextView textStockClosePrice;

        public PortfolioStocksViewHolder(View itemView) {
            super(itemView);
            textStockSymbol = itemView.findViewById(R.id.textStockSymbol);
            textStockName = itemView.findViewById(R.id.textStockName);
            textStockClosePrice = itemView.findViewById(R.id.textStockClosePrice);
        }
    }

    @Override
    public int getItemCount() {
        return portfolioStocks.size();
    }
}
