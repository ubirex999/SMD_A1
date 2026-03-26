package com.example.smd_assigment_1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.TextView;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        TextView tvHello = root.findViewById(R.id.tvHello);
        tvHello.setText("Hello Ubaid");

        // Deals section — horizontal scrolling RecyclerView
        RecyclerView dealsRv = root.findViewById(R.id.recyclerDeals);
        dealsRv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        dealsRv.setAdapter(new DealsAdapter(requireContext(), ProductCatalog.getDeals()));

        // Recommended section — vertical grid (unchanged)
        RecyclerView recommendedRv = root.findViewById(R.id.recyclerRecommended);
        recommendedRv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recommendedRv.setAdapter(new RecommendedAdapter(requireContext(), ProductCatalog.getRecommended()));

        return root;
    }
}


