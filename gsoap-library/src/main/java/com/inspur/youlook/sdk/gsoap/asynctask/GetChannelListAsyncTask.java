package com.inspur.youlook.sdk.gsoap.asynctask;

import com.inspur.youlook.sdk.gsoap.GsoapProxySingleton;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallback;
import com.inspur.youlook.sdk.gsoap.jni.JniResponse;
import com.inspur.youlook.sdk.gsoap.utils.GsoapUtils;
import com.inspur.youlook.sdk.gsoap.utils.Log;

/**
 * Created by andyliu on 2017/5/18.
 */
public class GetChannelListAsyncTask extends GsoapAsyncTask {

    private static final String TAG = GetChannelListAsyncTask.class.getSimpleName();

    public GetChannelListAsyncTask(GsoapCallback callback) {
        super(callback);
    }

    @Override
    JniResponse requestAPI(String... params) {
        String userID = verifyUserID(params[0]);
        String stbToken = verifyStbToken(params[1]);
        String clientInfo = GsoapUtils.getClientInfo(userID, stbToken);
        Log.getInstance().writeLog(TAG, "requestAPI", "clientInfo=" + clientInfo);
        return GsoapProxySingleton.getInstance().hpstb_GetChannelList(clientInfo);
    }

    @Override
    protected void onPostExecute(CallbackStatus status) {
        Log.getInstance().writeLog(TAG, "onPostExecute", "statusCode=" + status.getCode() + ", statusMessage=" + status.getMessage());
        String channelList = processChannelListSpecialCharWithData(status.getData());
        Log.getInstance().writeLog(TAG, "onPostExecute", "return value channelList=" + channelList);
        if (getCallback() != null) {
            getCallback().invoke(status.getCode(), status.getMessage(), channelList);
        } else {
            Log.getInstance().writeLog(TAG, "onPostExecute", "call without callback", Log.LogLevel.ERROR);
        }
    }

    private String processChannelListSpecialCharWithData(String data) {
        //FIXME: 该函数调用导致盒子返回的信息中缺失字符，该部分只能修复临芩地区的问题
        char aa[] = {'x', 'x', 'x', 'x', 'x', 'x'};
        StringBuilder md = new StringBuilder(data);
        int loc = 0;
        Log.getInstance().writeLog(TAG, "processChannelListSpecialCharWithData", "(before) newStr:" + data);
        while (loc < md.length()) {
            char buffer = md.charAt(loc);
//            [md getBytes:&buffer range:NSMakeRange(loc, 1)];
            //printf("%d", buffer&0x80);
            if ((buffer <= 0x1F && buffer >= 0x00) || buffer == 0x7F) {
                //[md replaceBytesInRange:NSMakeRange(loc, 1) withBytes:aa length:1];
                md.setCharAt(loc, aa[0]);
                loc++;
                continue;
            }

            if ((buffer & 0x80) == 0) {
                loc++;
                continue;
            } else if ((buffer & 0xE0) == 0xC0) {
                loc++;
                //[md getBytes:&buffer range:NSMakeRange(loc, 1)];
                buffer = md.charAt(loc);
                if ((buffer & 0xC0) == 0x80) {
                    loc++;
                    continue;
                }
                loc--;
                //非法字符，将这1个字符替换为AA
                //[md replaceBytesInRange:NSMakeRange(loc, 1) withBytes:aa length:1];
//                md.setCharAt(loc, aa[0]);
                loc++;
                continue;

            } else if ((buffer & 0xF0) == 0xE0) {
                loc++;
                buffer = md.charAt(loc);
                //[md getBytes:&buffer range:NSMakeRange(loc, 1)];
                if ((buffer & 0xC0) == 0x80) {
                    loc++;
                    buffer = md.charAt(loc);
                    //[md getBytes:&buffer range:NSMakeRange(loc, 1)];
                    if ((buffer & 0xC0) == 0x80) {
                        loc++;
                        continue;
                    }
                    loc--;
                }
                loc--;
                //非法字符，将这个字符替换为A
                //[md replaceBytesInRange:NSMakeRange(loc, 1) withBytes:aa length:1];
//                md.setCharAt(loc, aa[0]);
                loc++;
                continue;

            } else {
//                md.setCharAt(loc, aa[0]);
                //[md replaceBytesInRange:NSMakeRange(loc, 1) withBytes:aa length:1];
                loc++;
                continue;
            }
        }
        String newStr = md.toString().replace("x", "");
        Log.getInstance().writeLog(TAG, "processChannelListSpecialCharWithData", "(after) newStr:" + newStr);
        return newStr;
    }

}
