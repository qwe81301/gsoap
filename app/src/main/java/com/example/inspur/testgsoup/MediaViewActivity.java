package com.example.inspur.testgsoup;

import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TabHost;

import com.example.inspur.testgsoup.dummy.DummyContent;

public class MediaViewActivity extends AppCompatActivity implements EPGFragment.OnListFragmentInteractionListener ,ChannelListFragment.OnListFragmentInteractionListener {


    private FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_view);

        mTabHost =(FragmentTabHost) findViewById(R.id.tabHost);
        mTabHost.setup(this,getSupportFragmentManager(),android.R.id.tabcontent);

        TabHost.TabSpec tab1= mTabHost.newTabSpec("tab1");
        TabHost.TabSpec tab2= mTabHost.newTabSpec("tab2");

        tab1.setIndicator("節目列表",null);//tab 文字 或是可以放小圖示
        tab2.setIndicator("tab2 Text",null);

        mTabHost.addTab(tab1, ChannelListBlankFragment.class, null);
        mTabHost.addTab(tab2, EPGFragment.class, null);


    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }
}
