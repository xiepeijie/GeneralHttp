package com.example.payge.network;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class NetworkFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "xxx";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_network, container, false);
        Button button = view.findViewById(R.id.detach);
        button.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {

    }
}
