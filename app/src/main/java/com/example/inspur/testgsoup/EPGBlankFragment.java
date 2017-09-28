package com.example.inspur.testgsoup;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class EPGBlankFragment extends Fragment {

    private IMainPresenter mMainPresenter;
    private String mEPGItem1;
    private String mEPGItem2;
    private ListView mEPGListView;
    private List<String> mEPGList = new ArrayList<String>();
    private ArrayAdapter<String> mEPGArrayAdapter = null;


    public EPGBlankFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainPresenter = new MainPresenter(this, getActivity());
        runEPG();

    }

    private void runEPG() {
        mMainPresenter.connectToSTBDevice("172.16.129.44", "UserID_EPG_test1");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mMainPresenter.requestEPG("UserID_EPG_test2", "StbToken", 729000, 1, 2);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_epg_blank, container, false);
        mEPGListView = (ListView) view.findViewById(R.id.epgListView);
        return view;
    }

    public void dismantleJsonEPG(String epg) {
        mEPGList.clear();

        try {
            JSONArray epgJsonArray = new JSONArray(epg);
            for (int i = 0; i < epgJsonArray.length(); i++) {
                JSONObject epgJsonObject = epgJsonArray.getJSONObject(i);
                Log.v("channelListJsonObject", String.valueOf(epgJsonObject));
                mEPGItem1 = epgJsonObject.getString("EPGItem1");
                mEPGItem2 = epgJsonObject.getString("EPGItem1");
                String epgText = "(" + mEPGItem1 + ")  " + mEPGItem2;
                Log.v("mBatNameChannelListItem", epgText);
                mEPGList.add(epgText);
            }
            Log.v("epgTextAll", String.valueOf(mEPGList));
            showEPG();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showEPG() {
        mEPGArrayAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, mEPGList);
        mEPGListView.setAdapter(mEPGArrayAdapter);
    }
}
