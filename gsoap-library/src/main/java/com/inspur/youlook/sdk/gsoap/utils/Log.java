package com.inspur.youlook.sdk.gsoap.utils;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jerome on 2016/5/30.
 */
public class Log {

    private static Log mLogInstance;

    private String mFileName = "log";
    private String mFolderPath = "JeromeLibrary";

    private SimpleDateFormat mSimpleDateFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private LogLevel mLogLevel = LogLevel.DEBUG;

    private boolean mEnableShowLogcat = true;
    private boolean mEnableRecord = false;

    private Log() {
    }

    public static Log getInstance() {
        if (mLogInstance == null) {
            synchronized (Log.class) {
                if (mLogInstance == null) {
                    mLogInstance = new Log();
                }
            }
        }
        return mLogInstance;
    }

    public void enableRecord(boolean enable) {
        this.mEnableRecord = enable;
    }

    public boolean isEnableRecord() {
        return mEnableRecord;
    }

    public void enableShowLogCat(boolean enable) {
        this.mEnableShowLogcat = enable;
    }

    public boolean isShowLogcat() {
        return mEnableShowLogcat;
    }

    public void setFolderName(String folderName) {
        this.mFolderPath = folderName;
    }

    public void setFileName(String fileName) {
        this.mFileName = fileName;
    }

    public void setLogLevel(LogLevel level) {
        this.mLogLevel = level;
    }

    public boolean writeLog(String className, String methodName, String msg) {
        return write(mEnableRecord, mEnableShowLogcat, className, methodName, msg + "\n", mLogLevel);
    }

    public boolean writeLog(String className, String methodName, byte[] msg, LogLevel logLevel) {
        return write(mEnableRecord, mEnableShowLogcat, className, methodName, msg, logLevel);
    }

    public boolean writeLog(String className, String methodName, String msg, LogLevel logLevel) {
        return write(mEnableRecord, mEnableShowLogcat, className, methodName, msg, logLevel);
    }

    public boolean writeLog(String className, String methodName, String msg, LogLevel logLevel, boolean enforceRecord) {
        return write(enforceRecord, mEnableShowLogcat, className, methodName, msg, logLevel);
    }

    private boolean write(boolean bRecord, boolean isShowLogcat, String className, String methodName, byte[] msg, LogLevel logLevel) {
        StringBuilder strMsg = new StringBuilder();
        for (int i = 0; i < msg.length - 1; i++) {
            strMsg.append(String.format("%d,", msg[i]));
        }
        return write(bRecord, isShowLogcat, className, methodName, strMsg.toString(), logLevel);
    }

    private boolean write(boolean enableRecord, boolean enableShowLogcat, String className, String methodName, String msg, LogLevel logLevel) {
        if (enableShowLogcat)
            android.util.Log.i("LogUtility", className + "->" + methodName + "]: " + msg + "\n");

        if (enableRecord) {
            File root = Environment.getExternalStorageDirectory();
            File outDir = new File(root.getAbsolutePath() + File.separator + mFolderPath);
            if (!outDir.isDirectory()) {
                outDir.mkdir();
            }
            String fileName = this.mFileName;
            if (logLevel == LogLevel.ERROR)
                fileName += ".error";
            else if (logLevel == LogLevel.DEBUG)
                fileName += ".debug";
            else if (logLevel == LogLevel.INFO)
                fileName += ".info";
            File logFile = new File(outDir, fileName);

            try {
                Writer writer = new BufferedWriter(new FileWriter(logFile, true));
                String retStrFormatNowDate = mSimpleDateFormatter.format(new Date());
                writer.write("[" + retStrFormatNowDate + "-> " + className + "->" + methodName + "]: " + msg + "\n");
                writer.flush();
                writer.close();
                return true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return false;
    }

    public enum LogLevel {
        ERROR(0),
        DEBUG(1),
        INFO(2);

        public int type;

        LogLevel(int p) {
            type = p;
        }
    }
}