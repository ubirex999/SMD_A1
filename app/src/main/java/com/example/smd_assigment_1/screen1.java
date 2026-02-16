package com.example.smd_assigment_1;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.content.Intent;
import android.widget.LinearLayout;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link screen1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class screen1 extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public screen1() {
        // Required empty public constructor
    }
    LinearLayout sc;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment screen1.
     */
    // TODO: Rename and change types and number of parameters
    public static screen1 newInstance(String param1, String param2) {
        screen1 fragment = new screen1();
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

        View view = inflater.inflate(R.layout.fragment_screen1, container, false);

        sc = view.findViewById(R.id.screen1);

        sc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), detail_buy.class);


                intent.putExtra("image", R.drawable.headphones);
                intent.putExtra("price", "$59.99");
                intent.putExtra("name", "Sony Premium Wireless");
                intent.putExtra("model", "Model: WH-1000XM5");
                intent.putExtra("detail" , "The Best Noise Canceling Wireless Headphones, HD NC Processor QN3, 12 Microphones, Adaptive NC Optimizer, Mastered by Engineers, Studio-Quality, 30-Hour Battery, Black");

                startActivity(intent);
            }
        });

        return view;
    }





}