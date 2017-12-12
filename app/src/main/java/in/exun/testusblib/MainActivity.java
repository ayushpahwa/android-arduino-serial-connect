package in.exun.testusblib;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.ReadLisener;

public class MainActivity extends Activity {

    private TextView info, receivedData;
    private EditText txtBox;
    private Button btnRefresh, reconn;
    public static final String ACTION_USB_DEVICE_ATTACHED = "com.example.ACTION_USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_USB_DEVICE_ATTACHED:
                    refreshDevices(true);
                    break;
                case ACTION_USB_DEVICE_DETACHED:
                    refreshDevices(false);
                    break;
            }
        }
    };
    private Physicaloid mPhysicaloid;
    private String TAG = "Main";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        info = findViewById(R.id.textInfo);
        receivedData = findViewById(R.id.textReceived);
        txtBox = (EditText) findViewById(R.id.editText1);
        btnRefresh = (Button) findViewById(R.id.button1);
        reconn = (Button) findViewById(R.id.button2);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPhysicaloid.isOpened()) {
                    String str = txtBox.getText().toString();    //get text from EditText
                    if (str.length() > 0) {
                        byte[] buf = str.getBytes();    //convert string to byte array
                        mPhysicaloid.write(buf, buf.length);    //write data to arduino
                    }
                    txtBox.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "USB not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        reconn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildSerialConnection();
            }
        });
        reconn.setVisibility(View.GONE);
        mPhysicaloid = new Physicaloid(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbReceiver);
    }

    private void refreshDevices(boolean attachedState) {
        reconn.setVisibility(View.GONE);
        info.setText("USB Connected: " + "  " + attachedState);

        if (attachedState) {

            buildSerialConnection();

        } else {
            mPhysicaloid.close();
            txtBox.setText("");
            txtBox.setEnabled(false);
            mPhysicaloid.clearReadListener();
        }
    }

    private void buildSerialConnection() {
        Log.d(TAG, "buildSerialConnection: ");

        try {

            info.setText("USB Connected: true");

            if (mPhysicaloid != null) {
                mPhysicaloid.close();
                mPhysicaloid.clearReadListener();
                mPhysicaloid.setBaudrate(9600);

                if (mPhysicaloid.open()) {
                    txtBox.setEnabled(true);
                    txtBox.setMovementMethod(new ScrollingMovementMethod());

                    // read listener, When new data is received from Arduino add it to Text view
                    mPhysicaloid.addReadListener(new ReadLisener() {
                        @Override
                        public void onRead(int size) {
                            byte[] buf = new byte[size];
                            mPhysicaloid.read(buf, size);
                            tvAppend(receivedData, Html.fromHtml("<font color=blue>" + new String(buf) + "</font>"));
                        }
                    });

                } else {
                    txtBox.setEnabled(false);
                    info.setText("USB Connected: true\nDidn't connect tho");
                }
            } else {
                reconn.setVisibility(View.VISIBLE);
                info.setText("Trouble buidling connection. Restart");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            txtBox.setEnabled(false);
            info.setText("USB Connected: false\nDevice needs restart or invalid USB");

        }
    }

    Handler mHandler = new Handler();

    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);    // add text to Text view
            }
        });
    }

}
