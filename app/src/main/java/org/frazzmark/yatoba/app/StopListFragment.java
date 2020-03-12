/*
 * Copyright (c) 2015. Fabio Mazza
 */

package org.frazzmark.yatoba.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by fabio on 01/10/15.
 */
public class StopListFragment extends Fragment{
    RecyclerView recView;
    StopAdapter stopAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View linearlayout = inflater.inflate(R.layout.stop_list_fragment,container,false);
        recView = (RecyclerView) linearlayout.findViewById(R.id.cardList);
        stopAdapter = new StopAdapter(new ArrayList<BusStop>());
        recView.setAdapter(stopAdapter);
        return linearlayout;
    }

}
