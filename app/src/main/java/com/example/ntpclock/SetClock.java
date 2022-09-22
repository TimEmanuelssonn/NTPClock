package com.example.ntpclock;

import android.os.Handler;
import android.os.StrictMode;
import android.widget.TextView;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;

public class SetClock {

    /**
     * Set strictMode to get internet access
     */
    StrictMode.ThreadPolicy policy =
            new StrictMode.ThreadPolicy.Builder().permitAll().build();

    /**
     * Setting up global variables
     */
    public static final String TIME_SERVER = "0.se.pool.ntp.org";
    public Boolean backOnline = false;
    private long diff = 0;
    private long systemTime;
    private long currentTime;

    /**
     *
     * @param clock textView
     * @param df SimpleDateFormat
     */
    public SetClock(TextView clock, SimpleDateFormat df) {
        StrictMode.setThreadPolicy(policy);

        /**
         * Create new main to get access to getters and setters.
         */
        MainActivity main = new MainActivity();

        /**
         * Create handler and Runnable
         * Runnable: Set textview Clock
         *
         */
        Handler threadHandler = new Handler();
        Runnable rThread = () -> {
            if(main.getOnOffSwitch() && main.isOnline()) {
                clock.setText(df.format(currentTime));
            } else {
                clock.setText(df.format(systemTime));
                main.setOnOffSwitch(false);
            }
        };

        /**
         * Thread that's update textview clock every minute
         * and calculate current time with diff.
         * If offline or switch turned off show system clock.
         * Backonline will be true if we coming back online
         * and then we will check time with ntp server.
         */
        Thread updateClock= new Thread() {
            public void run() {
                while(!Thread.interrupted()) {
                    if(main.getOnOffSwitch()) {
                        if(backOnline) {
                            diff = calculateOffset();
                        }
                        systemTime = getSystemTime();
                        currentTime = systemTime + diff;
                        threadHandler.post(rThread);
                        backOnline = false;
                    } else {
                        systemTime = getSystemTime();
                        threadHandler.post(rThread);
                        backOnline = true;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        /**
         * Thread that is updating NTP clock every 20 seconds if online.
         */
        Thread updateNTPClock = new Thread() {
            public void run() {
                while(!Thread.interrupted()) {
                    if(main.getOnOffSwitch()) {
                        diff = calculateOffset();
                    }
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        /**
         * Start both threads
         */
        updateNTPClock.start();
        updateClock.start();
    }

    /**
     *
     * @return long with diff between system time and ntp server
     */
    public long calculateOffset() {
        long ntpTime = getCurrentTimeFromNtpServer();
        systemTime = getSystemTime();
        diff = systemTime - ntpTime;
        return diff;
    }

    /**
     *
     * @return long from ntp server in millis
     */
    public long getCurrentTimeFromNtpServer() {
        final NTPUDPClient client = new NTPUDPClient();
        long timeInMillis;
        client.setDefaultTimeout(2000);
        try {
            final InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
            final TimeInfo timeInfo = client.getTime(inetAddress);
            timeInMillis = timeInfo.getMessage().getTransmitTimeStamp().getTime();
        } catch (IOException e) {
            client.close();
            return 0;
        }
        client.close();
        return timeInMillis;
    }

    /**
     *
     * @return system time in millis
     */
    public long getSystemTime() {
        return System.currentTimeMillis();
    }
}