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
public class ChannelListBlankFragment extends Fragment {

    private IMainPresenter mMainPresenter;
    private String mChnoChannelListItem;
    private String mNameChannelListItem;
    private ListView mChannelListListView;
    private List<String> mChannelList = new ArrayList<String>();
    private ArrayAdapter<String> mChannelListArrayAdapter = null;

    public ChannelListBlankFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainPresenter = new MainPresenter(this, getActivity());
        runChannelList();

    }

    //getChannelInfoOnSTBDevice 印出節目列表
    private void runChannelList(){
        mMainPresenter.connectToSTBDevice("172.16.129.98", "UserID");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        mMainPresenter.getChannelClassification("UserID", "StbToken");
        mMainPresenter.getChannelInfoOnSTBDevice("UserID", "StbToken");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_channel_list_blank, container, false);
        mChannelListListView = (ListView)view.findViewById(R.id.channelListListView);
        return view;
    }


//    {
//          "chno":2,
//          "name":"bb kuaibao",
//          "tsid":1,
//          "serviceid":2,
//          "freq":729000,
//          "tunerid":0,
//          "isHide":"false",
//          "isFavor":"false",
//          "isLock":"false",
//          "isHD":"true",
//          "type":"tv",
//          "bat":""
//    },
    //[{"bat":1,"name":"央视频道"}, ... ,{"bat":2,"name":"北京频道"}]
    public void dismantleJsonChannelList(String channelList){
//        mChannelList.clear();
        try {
            JSONArray channelListJsonArray = new JSONArray(channelList);
            for (int i = 0; i < channelListJsonArray.length(); i++) {
                JSONObject channelListJsonObject = channelListJsonArray.getJSONObject(i);
                Log.v("channelListJsonObject", String.valueOf(channelListJsonObject));
                mChnoChannelListItem = channelListJsonObject.getString("chno");
                mNameChannelListItem = channelListJsonObject.getString("name");
                String channelListText = "("+ mChnoChannelListItem +")  "+mNameChannelListItem;
                Log.v("mBatNameChannelListItem", channelListText);
                mChannelList.add(channelListText);
            }
            Log.v("ChannelListBat", String.valueOf(mChannelList));
            showChannelList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //showChannelListToFragment
    private void showChannelList(){
            mChannelListArrayAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1, mChannelList);
            mChannelListListView.setAdapter(mChannelListArrayAdapter);
//            mChannelListArrayAdapter.notifyDataSetChanged();
    }

}
