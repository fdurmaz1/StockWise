package com.example.stockwise;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.BaseColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements SelectedStocksAdapter.OnStockSelectedListener {

    private RecyclerView recyclerView;
    ProgressBar progressBar;
    //search
    private SearchView searchView;
    private MatrixCursor cursor;
    private List<String> allStocks = new ArrayList<>();

    private List<String> filteredStocks = new ArrayList<>();
    private SelectedStocksAdapter selectedStocksAdapter;
    private List<String> selectedStocks = new ArrayList<>();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =   inflater.inflate(R.layout.fragment_home,container,false);

        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        progressBar = view.findViewById(R.id.progressBar2);

        // Create adapter if null
        if (selectedStocksAdapter == null) {
            selectedStocksAdapter = new SelectedStocksAdapter(requireContext(), selectedStocks, this);
            recyclerView.setAdapter(selectedStocksAdapter);
        }

        new PythonDataFetchTask().execute();

        StockSuggestionAdapter suggestionAdapter = new StockSuggestionAdapter(getActivity(), cursor, 0);
        searchView.setSuggestionsAdapter(suggestionAdapter);

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                @SuppressLint("Range") String selectedStock = cursor.getString(cursor.getColumnIndex("stock_name"));
                searchView.setQuery(selectedStock, true);

                return true;
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    searchView.getSuggestionsAdapter().changeCursor(null);
                } else {
                    filterStocks(newText);
                }
                return true;
            }
        });


        // Set up search functionality and handle search suggestions click
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                @SuppressLint("Range") String selectedStock = cursor.getString(cursor.getColumnIndex("stock_name"));
                addSelectedStock(selectedStock);
                return true;
            }

            // Other search-related code
        });


        // Inflate the layout for this fragment
        return view;

    }


    @Override
    public void onStockSelected(String selectedItem) {
        selectedStocks.remove(selectedItem); // Remove the selected item
        selectedStocksAdapter.notifyDataSetChanged(); // Refresh RecyclerView
    }

    //recyclerview
    private void addSelectedStock(String stock) {
        String[] parts = stock.split(" "); // Split the stock by whitespace
        if (parts.length > 1) {
            String selectedSymbol = parts[0];
            String selectedName = stock.substring(selectedSymbol.length()).trim();

            String combined = selectedSymbol + "\n" + selectedName; // Combine symbol and name

            selectedStocks.add(combined);
            selectedStocksAdapter.notifyDataSetChanged(); // Refresh the RecyclerView
        } else {
            // For suggestions, no splitting required
            selectedStocks.add(stock);
            selectedStocksAdapter.notifyDataSetChanged();
        }
    }


    private void filterStocks(String newText) {
        MatrixCursor filterCursor = new MatrixCursor(new String[]{BaseColumns._ID, "stock_name"});
        for (int i = 0; i < allStocks.size(); i++) {
            if (allStocks.get(i).toLowerCase().contains(newText.toLowerCase())) {
                filterCursor.addRow(new Object[]{i, allStocks.get(i)});
            }
        }
        searchView.getSuggestionsAdapter().changeCursor(filterCursor);
    }
    class StockSuggestionAdapter extends CursorAdapter {
        private final Context mContext;
        private final Cursor mCursor;
        StockSuggestionAdapter(Context context, Cursor cursor, int flags) {
            super(context, cursor, flags);
            mContext = context;
            mCursor = cursor;
        }

        @SuppressLint("Range")
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView text = view.findViewById(android.R.id.text1);
            text.setText(cursor.getString(cursor.getColumnIndex("stock_name")));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }


    }

    // AsyncTask to fetch Python data in the background
    private class PythonDataFetchTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            progressBar.setVisibility(View.VISIBLE);
            if (!Python.isStarted()) {
                Python.start(new AndroidPlatform(getActivity()));
            }

            Python py = Python.getInstance();
            PyObject pyObject = py.getModule("myscript");
            PyObject getStockSymbols = pyObject.callAttr("get_stock_symbols_with_names");
            List<String> stockList = new ArrayList<>();
            for (PyObject item : getStockSymbols.asList()) {
                stockList.add(item.toString());
            }
            return stockList;
        }

        @Override
        protected void onPostExecute(List<String> stockList) {
            cursor = new MatrixCursor(new String[]{BaseColumns._ID, "stock_name"});
            for (String stock : stockList) {
                allStocks.add(stock);
                cursor.addRow(new Object[]{allStocks.size() - 1, stock});
            }

            StockSuggestionAdapter suggestionAdapter = new StockSuggestionAdapter(getActivity(), cursor, 0);
            searchView.setSuggestionsAdapter(suggestionAdapter);
            progressBar.setVisibility(View.GONE);
        }
    }


}