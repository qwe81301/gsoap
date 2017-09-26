package com.example.inspur.testgsoup;

import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TabHost;

import com.example.inspur.testgsoup.dummy.DummyContent;

public class MediaViewActivity extends AppCompatActivity implements EPGFragment.OnListFragmentInteractionListener, ChannelListFragment.OnListFragmentInteractionListener {


    private FragmentTabHost mTabHost;
//    private TabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_view);

        mTabHost = (FragmentTabHost) findViewById(R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        TabHost.TabSpec channelListTab = mTabHost.newTabSpec("tab1");
        TabHost.TabSpec EPGListTab = mTabHost.newTabSpec("tab2");

        channelListTab.setIndicator("節目列表", null);//tab 文字 或是可以放小圖示
        EPGListTab.setIndicator("EPG", null);//電子節目指南（英語：Electronic program guide，縮寫：EPG）

        mTabHost.addTab(channelListTab, ChannelListBlankFragment.class, null);// new ChannelListBlankFragment
        mTabHost.addTab(EPGListTab, EPGBlankFragment.class, null);



    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }
}
