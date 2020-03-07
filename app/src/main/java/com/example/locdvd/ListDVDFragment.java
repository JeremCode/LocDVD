package com.example.locdvd;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

public class ListDVDFragment extends Fragment {

    public interface OnDVDSelectedListener {
        public void onDVDSelected(long dvdId);
    }

    OnDVDSelectedListener onDVDSelectedListener;
    ListView list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup
            container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listdvd, null);

        list =(ListView)view.findViewById(R.id.main_List);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int
                    position, long id) {
                if(onDVDSelectedListener!=null ) {
                    DVD selectedDvd = (DVD)v.getTag();
                    onDVDSelectedListener.onDVDSelected(selectedDvd.id);
                }

            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDVDList();
    }

    @Override
    public void onAttach(Activity activity)  {
        super.onAttach(activity);
        onDVDSelectedListener = (OnDVDSelectedListener)activity;
    }
    public void updateDVDList() {
        ArrayList<DVD> dvdList = DVD.getDVDList(getActivity());
        DVDAdapter dvdAdapter = new DVDAdapter(getActivity(), dvdList);
        list.setAdapter(dvdAdapter);
    }




}
