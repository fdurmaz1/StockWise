package com.example.stockwise;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StockAnalysisFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StockAnalysisFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private static final String ARG_STOCK_SYMBOL = "stockSymbol";
    private static final String ARG_STOCK_NAME = "stockName";
    private String stockSymbol;
    private String stockName;

    public StockAnalysisFragment() {
        // Required empty public constructor
    }


    public static StockAnalysisFragment newInstance(String stockSymbol, String stockName) {
        StockAnalysisFragment fragment = new StockAnalysisFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STOCK_SYMBOL, stockSymbol);
        args.putString(ARG_STOCK_NAME, stockName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stockSymbol = getArguments().getString(ARG_STOCK_SYMBOL);
            stockName = getArguments().getString(ARG_STOCK_NAME);
            Log.d("StockAnalysisFragment", "Received Symbol: " + stockSymbol + ", Name: " + stockName);
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_analysis, container, false);
        TextView txtStockSymbolAnalysis = view.findViewById(R.id.txtStockSymbolAnalysis);
        TextView txtStockNameAnalysis = view.findViewById(R.id.txtStockNameAnalysis);

        // Set the stock symbol and name
        txtStockSymbolAnalysis.setText(stockSymbol);
        txtStockNameAnalysis.setText(stockName);
        Log.d("StockAnalysisFragment", "onCreateView - Symbol: " + stockSymbol + ", Name: " + stockName);

        Button btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            // Pop the current fragment from the stack, returning to the previous one
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });

        // Initialize your views and start the analysis
        return view;
    }
}