package com.inspur.youlook.sdk.gsoap;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.test.InstrumentationTestCase;

import com.inspur.youlook.sdk.gsoap.jni.GsoapJNI;
import com.inspur.youlook.sdk.gsoap.jni.JniResponse;

import org.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GsoapInstrumentationTestCase extends InstrumentationTestCase {

    private static boolean enableSearch;
    private GsoapJNI mGsoapJNI;
    private JniResponse jniResponse;

    public GsoapInstrumentationTestCase() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mGsoapJNI = GsoapProxySingleton.getInstance().getGoapJNI();

        WifiManager wifi = (WifiManager) getInstrumentation().getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        enableSearch = wifi.isWifiEnabled();
    }

    private String getClientInfo() {
        return "{\"userid\":\"" + TestConstants.USER_ID + "\",\"token\":\"" + TestConstants.mStbToken + "\"}";
    }

    /**
     * 測試案例：Init
     * 預計結果：成功返回代號0, 無返回值
     *
     * @throws Exception
     */
    @Test
    public void test01_Init() throws Exception {
        String localInfo = "{\"port\": " + 10001 + "}";
        jniResponse = mGsoapJNI.hpstb_Init(localInfo);
        assertEquals(0, jniResponse.getReturnCode());
        assertEquals("No return value", jniResponse.getReturnValue());
    }

    /**
     * 測試案例：Set Notify Enable
     * 預計結果：成功返回代號0, 無返回值
     *
     * @throws Exception
     */
    @Test
    public void test02_SetNotifyEnable() throws Exception {
        jniResponse = mGsoapJNI.hpstb_SetNotifyEnable(1);
        assertEquals(0, jniResponse.getReturnCode());
        assertEquals("No return value", jniResponse.getReturnValue());
    }

    /**
     * 測試案例：Set Event Notify
     * 預計結果：成功返回代號0, 無返回值
     *
     * @throws Exception
     */
    @Test
    public void test03_SetEventNotify() throws Exception {
        jniResponse = mGsoapJNI.hpstb_SetEventNotify();
        assertEquals(0, jniResponse.getReturnCode());
        assertEquals("No return value", jniResponse.getReturnValue());
    }

    /**
     * 測試案例：Connect To STB
     * 預計結果：成功返回代號0
     *
     * @throws Exception
     */
    @Test
    public void test04_ConnectToSTB() throws Exception {
        if (enableSearch) {
            final String connectServerInfo = "{\"server\":\"" + TestConstants.STB_IP + ":" + 10000 + "\"}";
            final String connectClientInfo = "{\"userid\":\"" + TestConstants.USER_ID + "\",\"uuid\":\"" + TestConstants.USER_ID + "\"}";

            final CountDownLatch latch = new CountDownLatch(1);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    jniResponse = mGsoapJNI.hpstb_ConnectToSTB(connectClientInfo, connectServerInfo);
                    latch.countDown();
                }
            }).start();

            latch.await(10, TimeUnit.SECONDS);

            assertEquals(0, jniResponse.getReturnCode());

            String jsonString = jniResponse.getReturnValue();
            assertNotNull(jsonString);

            TestConstants.mStbToken = new JSONObject(jsonString).getString("token");

        } else {
            fail("Wifi is NOT enabled.");
        }
    }

    @Test
    public void test05_Disconnect() throws Exception {
        if (enableSearch) {
            jniResponse = mGsoapJNI.hpstb_Disconnect(getClientInfo());
            assertEquals(0, jniResponse.getReturnCode());
            assertEquals("No return value", jniResponse.getReturnValue());
        } else {
            fail("Wifi is NOT enabled.");
        }
    }

    // TODO only get STBJID not really binding.
    @Test
    public void test06_RequestBind() throws Exception {
        if (enableSearch) {
            jniResponse = mGsoapJNI.hpstb_RequestBind("{\"userid\":\"" + TestConstants.USER_ID + "\",\"token\":\"" + TestConstants.mStbToken + "\"}");
            assertEquals(0, jniResponse.getReturnCode());
            assertNotNull(jniResponse.getReturnValue());
            TestConstants.bIsBind = false;
        } else {
            fail("Wifi is NOT enabled.");
        }
    }

    @Test
    public void test07_GetMasterClient() throws Exception {
        if (enableSearch) {
            jniResponse = mGsoapJNI.hpstb_GetMasterClient(getClientInfo());
            int statusCode = jniResponse.getReturnCode();
            if (statusCode == 0) {
                assertEquals(0, statusCode);
                assertNotNull(jniResponse.getReturnValue());
            } else {
                assertEquals(10, statusCode);  // <10> HPSTB_MASTER_NOT_EXIST
                assertNull(jniResponse.getReturnValue());
            }
        } else {
            fail("Wifi is NOT enabled.");
        }
    }

    @Test
    public void test08_SetClientRole() throws Exception {
        if (enableSearch) {
            jniResponse = mGsoapJNI.hpstb_SetClientRole(getClientInfo(), "{\"role\":\"" + "master" + "\"}");
            int statusCode = jniResponse.getReturnCode();
            if (statusCode == 0) {
                TestConstants.bIsBind = true;
            }
            if (TestConstants.bIsBind) {
                assertEquals(0, statusCode);
            } else {
                if (statusCode == 11) {
                    assertEquals(11, statusCode);
                } else {
                    assertEquals(12, statusCode);
                }
            }
            assertEquals("No return value", jniResponse.getReturnValue());
        } else {
            fail("Wifi is NOT enabled.");
        }
    }

    @Test
    public void test09_GetClientRole() throws Exception {
        if (enableSearch) {
            jniResponse = mGsoapJNI.hpstb_GetClientRole(getClientInfo());
            if (jniResponse.getReturnValue().equals("{\"role\":\"master\"}")) {
                TestConstants.bHasPermission = true;
            } else {
                TestConstants.bHasPermission = false;
            }
        } else {
            fail("Wifi is NOT enabled.");
        }
    }

    /**
     * 測試案例：發出HandoverMasterRole請求
     * 預計結果：沒有權限, 返回代碼:<3> HPSTB_NO_PERMISSION
     *
     * @throws Exception
     */
    @Test
    public void test10_HandoverMasterRole() throws Exception {
        if (enableSearch) {
            final String anotherClient = "{\"userid\":\"" + "USER2" + "\",\"ip\":\"" + "10.0.0.5" + "\"}";
            jniResponse = mGsoapJNI.hpstb_HandoverMasterRole(getClientInfo(), anotherClient);
            int responseCode = jniResponse.getReturnCode();
            if (TestConstants.bHasPermission) {
                if (responseCode == 0) {
                    assertEquals(0, responseCode);  // HPSTB_OK
                } else {
                    assertEquals(1, responseCode);  // HPSTB_FAIL
                }
            } else {
                assertEquals(3, responseCode); // HPSTB_NO_PERMISSION = 3
            }
            assertEquals("No return value", jniResponse.getReturnValue());
        } else {
            fail("Wifi is NOT enabled.");
        }
    }

    /**
     * @throws Exception
     */
    public void test11_GetSTBInfo() throws Exception {
        if (enableSearch) {
            jniResponse = mGsoapJNI.hpstb_GetSTBInfo(getClientInfo());
            assertEquals(0, jniResponse.getReturnCode());
            assertNotNull(jniResponse.getReturnValue());
        } else {
            fail("Wifi is NOT enabled.");
        }
    }

    /**
     * @throws Exception
     */
    public void test12_GetSTBCapacity() throws Exception {
        if (enableSearch) {
            jniResponse = mGsoapJNI.hpstb_GetSTBCapacity(getClientInfo());
            assertEquals(0, jniResponse.getReturnCode());
            assertNotNull(jniResponse.getReturnValue());
        } else {
            fail("Wifi is NOT enabled.");
        }
    }

    // hpstb_SetBulletScreenEnable // TODO no implement
    // hpstb_GetBulletScreenEnable // TODO no implement
    // hpstb_SendText              // TODO no implement
    // hpstb_SendVoice             // TODO no implement

    /**
     * @throws Exception
     */
    public void test13_SendKey() throws Exception {
        if (enableSearch) {
            jniResponse = mGsoapJNI.hpstb_SendKey(getClientInfo(), TestConstants.KEY_INFO);
            if (TestConstants.bHasPermission) {
                assertEquals(0, jniResponse.getReturnCode());
            } else {
                assertEquals(3, jniResponse.getReturnCode()); // HPSTB_NO_PERMISSION = 3
            }
            assertEquals("No return value", jniResponse.getReturnValue());
        } else {
            fail("Wifi is NOT enabled.");
        }
    }

    // hpstb_GetSceenshot       // TODO no implement

    /**
     * @throws Exception
     */
    public void test14_GetChannelList() throws Exception {
        if (enableSearch) {
            jniResponse = mGsoapJNI.hpstb_GetChannelList(getClientInfo());
            assertEquals(0, jniResponse.getReturnCode());
            assertNotNull(jniResponse.getReturnValue());
        } else {
            fail("Wifi is NOT enabled.");
        }
    }

    /**
     * @throws Exception
     */
    public void test15_GetChannelClassification() throws Exception {
        if (enableSearch) {
            jniResponse = mGsoapJNI.hpstb_GetChannelClassification(getClientInfo());
            assertEquals(0, jniResponse.getReturnCode());
            assertNotNull(jniResponse.getReturnValue());
        } else {
            fail("Wifi is NOT enabled.");
        }
    }

    // hpstb_GetBookingList     // TODO no implement
    // hpstb_SetBookingList     // TODO no implement

    /**
     * @throws Exception
     */
    public void test16_RequestPF() throws Exception {
        if (enableSearch) {
            jniResponse = mGsoapJNI.hpstb_RequestPF(getClientInfo(), TestConstants.CHANNEL_INFO);
            // HPSTB_WAITING_FOR_NOTIFY = 4 //async notify for epg, pf, etc.
            assertEquals(4, jniResponse.getReturnCode());
            assertNull(jniResponse.getReturnValue());
        } else {
            fail("Wifi is NOT enabled.");
        }
    }

    // hpstb_RequestEPG         // TODO no implement
    // hpstb_PlayChannelOnTV    // TODO no implement
    // hpstb_GetPlayingOnTV     // TODO no implement
    // hpstb_GetSharingChannels // TODO no implement
    // hpstb_GetShareable       // TODO no implement

    /**
     * @throws Exception
     */
    public void test17_RequestShareChannel() throws Exception {
        if (enableSearch) {
            String protocolType = "http_socket";
            String channelInfo = TestConstants.CHANNEL_INFO;
            String requestInfo = "{\"channel\":" + channelInfo + "," +
                    "\"codec\":{\"type\":\"default\",\"res\":\"default\"}" + "," + // option
                    "\"protocol\":{\"type\":\"" + protocolType + "\",\"port\":\"default\"},\"encrypt\":\"false\"}";
            jniResponse = mGsoapJNI.hpstb_RequestShareChannel(getClientInfo(), requestInfo);
            assertEquals(0, jniResponse.getReturnCode());
            assertNotNull(jniResponse.getReturnValue());
        } else {
            fail("Wifi is NOT enabled.");
        }
    }

    /**
     * @throws Exception
     */
    public void test18_StopSharingChannel() throws Exception {
        if (enableSearch) {
            jniResponse = mGsoapJNI.hpstb_StopSharingChannel(getClientInfo(), TestConstants.CHANNEL_INFO);
            assertEquals(0, jniResponse.getReturnCode());
            assertEquals("No return value", jniResponse.getReturnValue());
        } else {
            fail("Wifi is NOT enabled.");
        }
    }
}
