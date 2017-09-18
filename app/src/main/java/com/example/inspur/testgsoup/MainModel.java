package com.example.inspur.testgsoup;

import android.util.Log;
import java.util.List;

/**
 * Created by inspur on 2017/9/5.
 */


public class MainModel implements IMainModel {

    private List<String> mIPList = null;
    @Override
    public void setIpArrayList(List<String> ipList) {
        Log.v("mSearchIpListModel", String.valueOf(ipList));
        mIPList = ipList;
    }

    @Override
    public List<String> getIpArrayList(){
        return mIPList;
    }

}
