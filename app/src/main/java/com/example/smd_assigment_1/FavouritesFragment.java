package com.example.smd_assigment_1;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FavouritesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favourites, container, false);

        RecyclerView recycler = root.findViewById(R.id.recyclerFavourites);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        FavouritesAdapter adapter = new FavouritesAdapter(requireContext());
        recycler.setAdapter(adapter);

        // Load from SQLite favourites immediately.
        adapter.reloadFromDb();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh favourites when coming back from other screens.
        View root = getView();
        if (root == null) return;
        RecyclerView recycler = root.findViewById(R.id.recyclerFavourites);
        if (recycler.getAdapter() instanceof FavouritesAdapter) {
            FavouritesAdapter adapter = (FavouritesAdapter) recycler.getAdapter();
            adapter.reloadFromDb();
        }
    }
}

