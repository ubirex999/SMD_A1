package com.example.smd_assigment_1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PlaceholderFragment extends Fragment {

    private static final String ARG_TEXT = "text";

    public static PlaceholderFragment newInstance(String text) {
        PlaceholderFragment f = new PlaceholderFragment();
        Bundle b = new Bundle();
        b.putString(ARG_TEXT, text);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_placeholder, container, false);
        TextView tv = root.findViewById(R.id.tvPlaceholder);
        String text = getArguments() != null ? getArguments().getString(ARG_TEXT, "Placeholder") : "Placeholder";
        tv.setText(text);
        return root;
    }
}

