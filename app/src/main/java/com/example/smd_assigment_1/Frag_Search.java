package com.example.smd_assigment_1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Frag_Search extends Fragment {

    // SharedPreferences key used for storing search history
    private static final String SearchHashKey = "search_history_prefs";
    private static final String KEY_QUERIES = "saved_queries";
    private static final String DB_URL = "https://smd-assigment-1-default-rtdb.asia-southeast1.firebasedatabase.app";

    private RecyclerView recyclerHistory;
    private HistoryAdapter historyAdapter;
    private List<String> historyList = new ArrayList<>();

    private RecyclerView recyclerResults;
    private TextView tvNoResults;
    private RecommendedAdapter resultsAdapter;
    private final List<Product> resultProducts = new ArrayList<>();
    private final List<Product> allProducts = new ArrayList<>();
    private DatabaseReference productsRef;
    private ValueEventListener productsListener;

    public Frag_Search() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_frag__search, container, false);

        TextInputEditText etSearch = root.findViewById(R.id.etSearchBar);
        ImageView btnBack = root.findViewById(R.id.bt_back_from_Search);
        TextView btnClearAll = root.findViewById(R.id.bt_clearall);
        recyclerHistory = root.findViewById(R.id.recyclerHistory);
        recyclerResults = root.findViewById(R.id.recyclerResults);
        tvNoResults = root.findViewById(R.id.tvNoResults);

        // Setup history RecyclerView
        recyclerHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        historyAdapter = new HistoryAdapter();
        recyclerHistory.setAdapter(historyAdapter);

        // Setup results RecyclerView (clickable product cards)
        recyclerResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        resultsAdapter = new RecommendedAdapter(requireContext(), resultProducts);
        recyclerResults.setAdapter(resultsAdapter);

        productsRef = FirebaseDatabase.getInstance(DB_URL).getReference("products");
        startProductsListener();

        // Load saved history
        loadHistory();

        // Search action (keyboard search button)
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
                if (!query.isEmpty()) {
                    performSearch(query);
                    etSearch.setText("");
                    hideKeyboard(etSearch);
                }
                return true;
            }
            return false;
        });

        // Back arrow — hide keyboard
        btnBack.setOnClickListener(v -> hideKeyboard(v));

        // Clear All button
        btnClearAll.setOnClickListener(v -> {
            SharedPreferences prefs = requireContext().getSharedPreferences(SearchHashKey, Context.MODE_PRIVATE);
            prefs.edit().remove(KEY_QUERIES).apply();
            historyList.clear();
            historyAdapter.notifyDataSetChanged();
        });

        return root;
    }

    /**
     * Searches through all recommended products using a linear search algorithm.
     * If the product name contains the query (case-insensitive), it is considered found.
     */
    private void performSearch(String query) {
        // Save query to history
        saveQueryToHistory(query);

        String q = query.trim().toLowerCase();
        resultProducts.clear();

        for (int i = 0; i < allProducts.size(); i++) {
            Product p = allProducts.get(i);
            String name = p.name != null ? p.name.toLowerCase() : "";
            String type = p.type != null ? p.type.toLowerCase() : "";
            String desc = p.description != null ? p.description.toLowerCase() : "";
            if (name.contains(q) || type.contains(q) || desc.contains(q)) {
                resultProducts.add(p);
            }
        }

        resultsAdapter.notifyDataSetChanged();

        boolean hasResults = !resultProducts.isEmpty();
        recyclerResults.setVisibility(hasResults ? View.VISIBLE : View.GONE);
        tvNoResults.setVisibility(hasResults ? View.GONE : View.VISIBLE);
    }

    private void startProductsListener() {
        if (productsRef == null) return;
        if (productsListener != null) {
            productsRef.removeEventListener(productsListener);
        }

        productsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allProducts.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Product product = data.getValue(Product.class);
                    if (product != null) {
                        allProducts.add(product);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return;
                tvNoResults.setText("No results found");
                tvNoResults.setVisibility(View.VISIBLE);
                recyclerResults.setVisibility(View.GONE);
            }
        };
        productsRef.addValueEventListener(productsListener);
    }

    /**
     * Saves a search query into SharedPreferences and refreshes the history list.
     */
    private void saveQueryToHistory(String query) {
        SharedPreferences prefs = requireContext().getSharedPreferences(SearchHashKey, Context.MODE_PRIVATE);
        Set<String> existing = prefs.getStringSet(KEY_QUERIES, new HashSet<>());

        // Create a new set to avoid mutating the returned set
        Set<String> updated = new HashSet<>(existing);
        updated.add(query);

        prefs.edit().putStringSet(KEY_QUERIES, updated).apply();

        // Refresh displayed list
        loadHistory();
    }

    /**
     * Loads search history from SharedPreferences into the RecyclerView.
     */
    private void loadHistory() {
        SharedPreferences prefs = requireContext().getSharedPreferences(SearchHashKey, Context.MODE_PRIVATE);
        Set<String> saved = prefs.getStringSet(KEY_QUERIES, new HashSet<>());
        historyList.clear();
        historyList.addAll(saved);
        if (historyAdapter != null) {
            historyAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Hides the soft keyboard.
     */
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (productsRef != null && productsListener != null) {
            productsRef.removeEventListener(productsListener);
            productsListener = null;
        }
    }

    // ---- Inner RecyclerView Adapter for history items ----

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HVH> {

        class HVH extends RecyclerView.ViewHolder {
            TextView tvQuery;

            HVH(@NonNull View itemView) {
                super(itemView);
                tvQuery = itemView.findViewById(R.id.tvHistoryQuery);
            }
        }

        @NonNull
        @Override
        public HVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_search_history, parent, false);
            return new HVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull HVH holder, int position) {
            String query = historyList.get(position);
            holder.tvQuery.setText(query);

            // Tapping a history item re-runs that search
            holder.itemView.setOnClickListener(v -> performSearch(query));
        }

        @Override
        public int getItemCount() {
            return historyList.size();
        }
    }
}