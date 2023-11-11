package com.example.stockwise;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PortfolioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PortfolioFragment extends Fragment {

    private RecyclerView recyclerViewPortfolio;
    private static PortfolioStocksAdapter portfolioStocksAdapter;
    private static List<String> portfolioStocks = new ArrayList<>();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PortfolioFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PortfolioFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PortfolioFragment newInstance(String param1, String param2) {
        PortfolioFragment fragment = new PortfolioFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);
        recyclerViewPortfolio = view.findViewById(R.id.recyclerViewPortfolio);
        recyclerViewPortfolio.setLayoutManager(new LinearLayoutManager(getContext()));

        // Check for null before setting the adapter
        if (recyclerViewPortfolio != null) {
            recyclerViewPortfolio.setLayoutManager(new LinearLayoutManager(getContext()));
            portfolioStocksAdapter = new PortfolioStocksAdapter(StockManager.getInstance().getSelectedStocks());
            recyclerViewPortfolio.setAdapter(portfolioStocksAdapter);
        }
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySharedPref", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userid", -1); // Default to -1 if not found

        if (userId != -1) {
            new FetchPortfolioStocksTask(this, userId).execute();
        } else {
            // Handle the case where user ID is not found
        }

        return view;
    }


    private static class FetchPortfolioStocksTask extends AsyncTask<Void, Void, List<String>> {
        private WeakReference<PortfolioFragment> fragmentReference;
        private int userId;

        FetchPortfolioStocksTask(PortfolioFragment fragment, int userId) {
            this.fragmentReference = new WeakReference<>(fragment);
            this.userId = userId; // Store the user ID
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            // Implement network request here
            // For now, this is a placeholder for actual network logic
            return fetchStocksFromDatabase();
        }

        @Override
        protected void onPostExecute(List<String> stocks) {
            PortfolioFragment fragment = fragmentReference.get();
            if (fragment != null && stocks != null) {
                PortfolioStocksAdapter adapter = new PortfolioStocksAdapter(stocks);
                fragment.recyclerViewPortfolio.setAdapter(adapter);
            }
        }

        private List<String> fetchStocksFromDatabase() {
            List<String> stocks = new ArrayList<>();
            String urlString = "http://192.168.1.82/LoginRegister/get_user_portfolio.php?userid=" + userId;

            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read response
                    InputStream inputStream = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Parse the JSON response
                    JSONArray jsonArray = new JSONArray(response.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject stockObject = jsonArray.getJSONObject(i);
                        String stockSymbol = stockObject.getString("symbol"); // Adjust these keys to match your JSON response
                        String stockName = stockObject.getString("name");
                        stocks.add(stockSymbol + "\n" + stockName);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return stocks;
        }
    }


    public void addToPortfolio(String selectedItem) {
        if (!portfolioStocks.contains(selectedItem)) {
            portfolioStocks.add(selectedItem);
            if (portfolioStocksAdapter != null) {
                portfolioStocksAdapter.notifyDataSetChanged();
            }
        }
    }
}