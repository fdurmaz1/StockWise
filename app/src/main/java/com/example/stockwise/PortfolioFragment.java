package com.example.stockwise;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        // Make sure the correct ID is used for the RecyclerView in fragment_portfolio layout
        recyclerViewPortfolio = view.findViewById(R.id.recyclerViewPortfolio);

        // Check for null before setting the adapter
        if (recyclerViewPortfolio != null) {
            recyclerViewPortfolio.setLayoutManager(new LinearLayoutManager(getContext()));
            portfolioStocksAdapter = new PortfolioStocksAdapter(StockManager.getInstance().getSelectedStocks());
            recyclerViewPortfolio.setAdapter(portfolioStocksAdapter);
        }

        return view;
    }


    // Function to add selected item to the PortfolioFragment's list

    public void addToPortfolio(String selectedItem) {
        if (!portfolioStocks.contains(selectedItem)) {
            portfolioStocks.add(selectedItem);
            if (portfolioStocksAdapter != null) {
                portfolioStocksAdapter.notifyDataSetChanged();
            }
        }
    }
}