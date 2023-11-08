package com.example.stockwise;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SelectedStocksAdapter extends RecyclerView.Adapter<SelectedStocksAdapter.SelectedStocksViewHolder> {
    private List<String> selectedStocks;
    private PortfolioFragment portfolioFragment;


    // Constructor to set the data
    public SelectedStocksAdapter(List<String> selectedStocks) {
        this.selectedStocks = selectedStocks;
    }

    // ViewHolder for the adapter
    public static class SelectedStocksViewHolder extends RecyclerView.ViewHolder {
        TextView textStockSymbol;
        TextView textStockName;
        ImageView imageView4;
        public SelectedStocksViewHolder(View itemView) {
            super(itemView);
            textStockSymbol = itemView.findViewById(R.id.textStockSymbol);
            textStockName = itemView.findViewById(R.id.textStockName);
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
//        String stock = selectedStocks.get(position);
//        holder.textStockSymbol.setText(stock);
//        // Set other stock information if needed

        String combined = selectedStocks.get(position);
        String[] parts = combined.split("\n");

        holder.textStockSymbol.setText(parts[0]);
        holder.textStockName.setText(parts[1]);

        holder.imageView4.setOnClickListener(v -> {
            String selectedItem = selectedStocks.get(position);
            Toast.makeText(holder.itemView.getContext(), "Added to your portfolio", Toast.LENGTH_SHORT).show();

            StockManager.getInstance().addStock(selectedItem); // Add the selected stock to the shared list

            if (portfolioFragment != null) {
                portfolioFragment.addToPortfolio(selectedItem);
                Log.d("SelectedStocksAdapter", "Plus sign clicked");
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectedStocks.size();
    }
}
