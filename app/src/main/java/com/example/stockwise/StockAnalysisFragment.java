package com.example.stockwise;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

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

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(getContext()));
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

        if (getArguments() != null) {
            stockSymbol = getArguments().getString(ARG_STOCK_SYMBOL);
            stockName = getArguments().getString(ARG_STOCK_NAME);
            showStockPrediction(stockSymbol);
            showStockPredictionPlot(stockSymbol);
        }

        // Initialize your views and start the analysis
        return view;
    }

    // Inside StockAnalysisFragment class
    private void showStockPrediction(String stockSymbol) {
        new AsyncTask<Void, Void, Double>() {
            @Override
            protected Double doInBackground(Void... voids) {
                try {
                    Python py = Python.getInstance();
                    PyObject pyObject = py.getModule("myscript");
                    return pyObject.callAttr("predict_stock_price", stockSymbol).toDouble();
                } catch (PyException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Double prediction) {
                if (prediction != null) {
                    TextView txtPrediction = getActivity().findViewById(R.id.txtPrediction);
                    String formattedPrediction = String.format("%.2f", prediction);
                    txtPrediction.setText("Predicted Close Price: $" + formattedPrediction);
                } else {
                    // Handle the case where prediction is null
                    // Show an error message or a default state
                }
            }


        }.execute();
    }

    // Inside StockAnalysisFragment class
    private void showStockPredictionPlot(String stockSymbol) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    Python py = Python.getInstance();
                    PyObject pyObject = py.getModule("myscript");
                    return pyObject.callAttr("predict_stock_price_plot", stockSymbol).toString();
                } catch (PyException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String plotBase64) {
                if (plotBase64 != null) {
                    ImageView imgPlot = getActivity().findViewById(R.id.imgPlot);

                    // Decode the base64 string to a Bitmap and set it to the ImageView
                    byte[] decodedString = Base64.decode(plotBase64, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imgPlot.setImageBitmap(decodedByte);
                } else {
                    // Handle the case where plotBase64 is null
                    // Show an error message or a default state
                }
            }
        }.execute();
    }

}