package com.example.inspur.testgsoup;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IMainActivity {

    private static final String TAG = "Gsoap";

    //    IMainPresenter mMainPresenter = new MainPresenter();
    private IMainPresenter mMainPresenter;


    private TextView mShowInfoTextView;
    private Button mSearchSTBButton;
//    private Button mTestButton;
    private Spinner mShowIpSpinner;
    private Spinner mShowFeatureSpinner;
    private ToggleButton mConnectSTBToggleButton;
    private ToggleButton mFeatureStartStopToggleButton;

    private String mStbToken = "stbTokenTest";
    private String mUserID = "userIDTest";
    private String mSTBDeviceIP;


    //    因應大陸那邊的機頂盒需要改channelFreq, channelTsid, channelServiceId
//    大陸那邊的機頂盒 需改參數 "freq":546000,"tsid":1,"serviceid": 101
//    原測試參數channelFreq = 729000 , channelTsid = 1 , ChannelServiceId = 2
    private int mChannelFreq = 729000;
    private int mChannelTsid = 1;
    private int mChannelServiceId = 2;

    private List<String> mSearchIpList = new ArrayList<String>();
    private List<String> mFeatureList = new ArrayList<String>();
    private ArrayAdapter<String> mIpList = null;
    private ArrayAdapter<String> mShowFeatureList = null;


    private String mSaveVideoPath = null;
    private String mToggleButtonFlag = null;
    private String mShowInfoText = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainPresenter = new MainPresenter(this, this);

        //執行設置儲存.ts檔內部資料夾的絕對路徑
        setSaveVideoPath();

        mShowInfoTextView = (TextView) findViewById(R.id.showInfoTextView);
        mSearchSTBButton = (Button) findViewById(R.id.searchSTBButton);
//        mTestButton = (Button) findViewById(R.id.testButton);
        mShowIpSpinner = (Spinner) findViewById(R.id.showIPSpinner);
        mShowFeatureSpinner = (Spinner) findViewById(R.id.showFeatureSpinner);
        mConnectSTBToggleButton = (ToggleButton) findViewById(R.id.connectSTBToggleButton);
        mFeatureStartStopToggleButton = (ToggleButton) findViewById(R.id.featureStartStopToggleButton);

        // 顯示IP在Spinner上   Spinner
        mIpList = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mSearchIpList);
        mIpList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mShowIpSpinner.setAdapter(mIpList);

        mShowIpSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                mSTBDeviceIP = adapterView.getItemAtPosition(position).toString();
                Toast.makeText(getBaseContext(), mSTBDeviceIP, Toast.LENGTH_SHORT).show();
                Log.v("searchIPChoice", mSTBDeviceIP);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //可點選其他功能（ex:錄製影片...）在Spinner上   Spinner
        mFeatureList.add("錄製功能");
        mFeatureList.add("查看機頂盒資訊功能(STB Info)");
        mFeatureList.add("查看機頂盒capacity資訊(STB capacity)");
        mFeatureList.add("查看機頂盒的頻道清單(STB Channel List)");
        mFeatureList.add("查看機頂盒的頻道Classification(STB Classification)");
        //mFeatureList.add("選取功能");
        mShowFeatureList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mFeatureList);
        mShowFeatureList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mShowFeatureSpinner.setAdapter(mShowFeatureList);
        mShowFeatureSpinner.setSelection(0, false);
        mShowFeatureSpinner.setEnabled(false);

        mShowFeatureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Log.v("mFeatureList position", String.valueOf(position));
                Toast.makeText(getBaseContext(), adapterView.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
                switch (position) {
                    case 0:
                        mShowInfoTextView.setText("已選擇錄製功能，請點擊右側開始按鈕");
                        mToggleButtonFlag = "startShareVideoOnSTBDeviceFlag";
                        break;
                    case 1:
                        mShowInfoTextView.setText("已選擇查看機頂盒資訊功能，請點擊右側開始按鈕");
                        mToggleButtonFlag = "getSTBDeviceInfoFlag";
                        break;
                    case 2:
                        mShowInfoTextView.setText("已選擇查看機頂盒capacity資訊，請點擊右側開始按鈕");
                        mToggleButtonFlag = "getCapacityOnSTBDeviceInfoFlag";
                        break;
                    case 3:
                        mShowInfoTextView.setText("已選擇查看機頂盒的頻道清單，請點擊右側開始按鈕");
                        mToggleButtonFlag = "getChannelInfoOnSTBDeviceInfoFlag";
                        break;
                    case 4:
                        mShowInfoTextView.setText("已選擇查看機頂盒的頻道Classification，請點擊右側開始按鈕");
                        mToggleButtonFlag = "getChannelClassificationInfoFlag";
                        break;
                    default:
                        Log.v("mFeatureList default", String.valueOf(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.v("onNothingSelected", String.valueOf(adapterView));
            }
        });

        //收尋STB Button
        mSearchSTBButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMainPresenter.searchSTBDevices();
                mConnectSTBToggleButton.setEnabled(true);
            }
        });

        //暫時測試功能用的 Button
//        mTestButton.setOnClickListener(new Button.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mMainPresenter.getSTBDeviceInfo(mUserID, mStbToken);
//            }
//        });


        //連結STB . 斷開STB ToggleButton
        mConnectSTBToggleButton.setEnabled(false);
        mConnectSTBToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mConnectSTBToggleButton.isChecked()) {
                    mMainPresenter.connectToSTBDevice(mSTBDeviceIP, mUserID);
                    mShowInfoTextView.setText("連線上" + mSTBDeviceIP + "\n點選第二個按鈕來觸發開始(錄製)功能");
                    mFeatureStartStopToggleButton.setEnabled(true);
                    mShowIpSpinner.setEnabled(false);
                    mShowFeatureSpinner.setEnabled(true);
                } else {
                    Log.v("mStbToken", String.valueOf(mStbToken));
                    mMainPresenter.disconnectToSTBDevice(mUserID, mStbToken);
                    mShowInfoTextView.setText("已斷開與機頂盒連結");
                    mFeatureStartStopToggleButton.setEnabled(false);
                    mShowIpSpinner.setEnabled(true);
                }
            }
        });

        //Spinner其他功能要用的開始.停止的ToggleButton（ex:錄製影片...）   ToggleButton (以後新增功能要寫成if判斷是哪個功能進來在決定要做什麼)
        mFeatureStartStopToggleButton.setEnabled(false);
        mFeatureStartStopToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFeatureStartStopToggleButton.isChecked()) {
                    //開始Spinner已選定功能
                    switch (mToggleButtonFlag) {
                        case "startShareVideoOnSTBDeviceFlag":
                            mMainPresenter.startShareVideoOnSTBDevice(mUserID, mStbToken, mChannelFreq, mChannelTsid, mChannelServiceId, "http_socket", "default", "default");
                            mShowInfoTextView.setText("開始錄製影片，影片路徑為:" + getObbDir().getAbsolutePath() + "\n\n再次點擊按鈕即可停止錄製\n" +
                                    "\n頻道的Frequency:" + mChannelFreq + "\n頻道的TSID:" + mChannelTsid + "\n頻道的ServiceID:" + mChannelServiceId);
                            break;
                        case "getSTBDeviceInfoFlag":
                            mMainPresenter.getSTBDeviceInfo(mUserID, mStbToken);
                            break;
                        case "getCapacityOnSTBDeviceInfoFlag":
                            mMainPresenter.getCapacityOnSTBDevice(mUserID, mStbToken);
                            break;
                        case "getChannelInfoOnSTBDeviceInfoFlag":
                            mMainPresenter.getChannelInfoOnSTBDevice(mUserID, mStbToken);
                            break;
                        case "getChannelClassificationInfoFlag":
                            mMainPresenter.getChannelClassification(mUserID, mStbToken);
                            break;
                        default:
                            Log.v("TogButton isCheck", String.valueOf(mToggleButtonFlag));
                    }
//                    mMainPresenter.startShareVideoOnSTBDevice(mUserID, mStbToken, mChannelFreq, mChannelTsid, mChannelServiceId, "http_socket", "default", "default");
//                    mShowInfoTextView.setText("開始錄製影片，影片路徑為:" + getObbDir().getAbsolutePath() + "\n\n再次點擊按鈕即可停止錄製\n" +
//                            "\n頻道的Frequency:" + mChannelFreq + "\n頻道的TSID:" + mChannelTsid + "\n頻道的ServiceID:" + mChannelServiceId);
                    mConnectSTBToggleButton.setEnabled(false);
                    mShowFeatureSpinner.setEnabled(false);
                } else {
                    //停止Spinner已選定功能
                    switch (mToggleButtonFlag) {
                        case "startShareVideoOnSTBDeviceFlag":
                            mMainPresenter.stopShareVideoOnSTBDevice(mUserID, mStbToken, mChannelFreq, mChannelTsid, mChannelServiceId);
                            mShowInfoTextView.setText("停止錄製影片");
                            break;
                        case "getSTBDeviceInfoFlag":
                            mShowInfoTextView.setText("停止(查看機頂盒資訊功能)");
                            break;
                        case "getCapacityOnSTBDeviceInfoFlag":
                            mShowInfoTextView.setText("停止(查看機頂盒capacity資訊)");
                            break;
                        case "getChannelInfoOnSTBDeviceInfoFlag":
                            mShowInfoTextView.setText("停止(查看機頂盒取得機頂盒的頻道清單)");
                            break;
                        case "getChannelClassificationInfoFlag":
                            mShowInfoTextView.setText("停止(查看機頂盒的頻道Classification)");
                            break;
                        default:
                            Log.v("ToggleButton default", String.valueOf(mToggleButtonFlag));
                    }
                    mConnectSTBToggleButton.setEnabled(true);
                    mShowFeatureSpinner.setEnabled(true);
                }
            }
        });
    }

    // 收到Presenter的IpList更新Spinner
    @Override
    public void updateIpList(List<String> ipList) {
        if (mSearchIpList != null) {
            Log.v("ipList", String.valueOf(ipList));
            mIpList = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ipList);
            mIpList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mShowIpSpinner.setAdapter(mIpList);
            mIpList.notifyDataSetChanged();
        }
    }

    //設置儲存.ts檔內部資料夾的絕對路徑getObbDir().getAbsolutePath() 或是要改成外部記憶卡路徑也可改
    public void setSaveVideoPath() {
        mSaveVideoPath = getObbDir().getAbsolutePath();
    }
    @Override
    public String getSaveVideoPath() {
        return mSaveVideoPath;
    }

    //取得回傳的GsoapCallbackObject args[2] 再印出來
    @Override
    public void getGsoapCallbackObject(String print){
        mShowInfoText = print;
        //這裡的判斷用的Flag 跟ToggleButton一樣
        switch (mToggleButtonFlag) {
            case "startShareVideoOnSTBDeviceFlag":
                Log.v("getGsoapCallbackObject", "startShareVideoOnSTBDeviceFlag");
                break;
            case "getSTBDeviceInfoFlag":
                mShowInfoTextView.setText("查看機頂盒資訊\n" + mShowInfoText);
                break;
            case "getCapacityOnSTBDeviceInfoFlag":
                mShowInfoTextView.setText("查看機頂盒capacity資訊\n"+ mShowInfoText);
                break;
            case "getChannelInfoOnSTBDeviceInfoFlag":
                mShowInfoTextView.setText("查看機頂盒取得機頂盒的頻道清單\n"+ mShowInfoText);
                break;
            case "getChannelClassificationInfoFlag":
                mShowInfoTextView.setText("查看機頂盒的頻道Classification\n"+ mShowInfoText);
                break;
            default:
                Log.v("getGsoapCallbackObject", String.valueOf(mToggleButtonFlag));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
