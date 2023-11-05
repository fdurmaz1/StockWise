package com.example.stockwise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SelectedStocksAdapter extends RecyclerView.Adapter<SelectedStocksAdapter.SelectedStocksViewHolder>{
    private List<String> selectedStocks;

    // Constructor to set the data
    public SelectedStocksAdapter(List<String> selectedStocks) {
        this.selectedStocks = selectedStocks;
    }

    // ViewHolder for the adapter
    public static class SelectedStocksViewHolder extends RecyclerView.ViewHolder {
        TextView textStockSymbol;
        TextView textStockName;
        public SelectedStocksViewHolder(View itemView) {
            super(itemView);
            textStockSymbol = itemView.findViewById(R.id.textStockSymbol);
            textStockName = itemView.findViewById(R.id.textStockName);
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
//        String stock = selectedStocks.get(position);
//        holder.textStockSymbol.setText(stock);
//        // Set other stock information if needed

        String combined = selectedStocks.get(position);
        String[] parts = combined.split("\n");

        holder.textStockSymbol.setText(parts[0]);
        holder.textStockName.setText(parts[1]);
    }

    @Override
    public int getItemCount() {
        return selectedStocks.size();
    }
}
