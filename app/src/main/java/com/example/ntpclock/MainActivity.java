package com.example.ntpclock;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    private static Switch onOffSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView clock = findViewById(R.id.clock);
        onOffSwitch = findViewById(R.id.switch1);

        /**
         * Create new simpledateformat and use it to convert date format
         * Set timezone to stockholm
         */
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));

        /**
         * Create new setClock class
         * @parm clock is my textview
         * @parm df is my SimpleDateFormat
         */
        new SetClock(clock, df);

        /**
         * Set listener on switch button.
         */
        onOffSwitch = findViewById(R.id.switch1);
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             *
             * @param compoundButton Check if pressed
             * @param isChecked Check if switch is on/off
             * getIsOnline return true if online
             */
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked && isOnline()) {
                    onOffSwitch.setChecked(true);
                    Toast.makeText(getApplicationContext(),
                            "NTP server switched ON", Toast.LENGTH_SHORT).show();
                } else {
                    onOffSwitch.setChecked(false);
                    Toast.makeText(getApplicationContext(),
                            "NTP server switched OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     *
     * @return true if we got internet access
     */
    public boolean isOnline() {
        try {
            int timeoutMs = 1500;
            Socket sock = new Socket();
            SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);
            sock.connect(sockaddr, timeoutMs);
            sock.close();

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     *
     * @return either true or false
     */
    public static boolean getOnOffSwitch() {
        return onOffSwitch.isChecked();
    }

    /**
     *
     * @param value is either true or false to set onOffSwitch.
     */
    public static void setOnOffSwitch(boolean value) {
        onOffSwitch.setChecked(value);
    }
}