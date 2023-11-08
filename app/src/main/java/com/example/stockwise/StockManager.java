package com.example.stockwise;

import java.util.ArrayList;
import java.util.List;

public class StockManager {
    private static final StockManager instance = new StockManager();
    private List<String> selectedStocks = new ArrayList<>();

    public static StockManager getInstance() {
        return instance;
    }

    public List<String> getSelectedStocks() {
        return selectedStocks;
    }

    public void addStock(String stock) {
        selectedStocks.add(stock);
    }
}
