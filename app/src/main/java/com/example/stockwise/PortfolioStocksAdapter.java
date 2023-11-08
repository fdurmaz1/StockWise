package com.example.stockwise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
    }



    public static class PortfolioStocksViewHolder extends RecyclerView.ViewHolder {
        TextView textStockSymbol;
        TextView textStockName;

        public PortfolioStocksViewHolder(View itemView) {
            super(itemView);
            textStockSymbol = itemView.findViewById(R.id.textStockSymbol);
            textStockName = itemView.findViewById(R.id.textStockName);
        }
    }

    @Override
    public int getItemCount() {
        return portfolioStocks.size();
    }
}
