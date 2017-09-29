package com.example.inspur.testgsoup;


import java.util.List;

/**
 * Created by inspur on 2017/9/7.
 */

public interface IMainActivity {
    //Presenter 叫 Activity 做的事情
    void updateIpList(List<String> ipList);
    String getSaveVideoPath();
    void printGsoapCallbackObject(String print);
    void showVerifyEditDialog();

}
