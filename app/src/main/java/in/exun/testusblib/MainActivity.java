package in.exun.testusblib;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends Activity
{

    private EditText txtBox;
    private Button btnRefresh;
    public static final String ACTION_USB_DEVICE_ATTACHED = "com.example.ACTION_USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ACTION_USB_DEVICE_ATTACHED:
                    refreshDevices(true);
                    break;
                case ACTION_USB_DEVICE_DETACHED:
                    refreshDevices(false);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtBox = (EditText) findViewById(R.id.editText1);
        btnRefresh = (Button) findViewById(R.id.button1);

        btnRefresh.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0)
            {
                refreshDevices(false);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(ACTION_USB_DEVICE_DETACHED);
        filter.addAction("android.hardware.usb.action.USB_STATE");
        registerReceiver(usbReceiver,filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbReceiver);
    }

    private void refreshDevices(boolean attachedState)
    {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        // Get the list of attached devices
        HashMap<String, UsbDevice> devices = manager.getDeviceList();

        txtBox.setText("");
        txtBox.setText("Number of devices: " + devices.size() + "  " + attachedState + "\n");

        // Iterate over all devices
        Iterator<String> it = devices.keySet().iterator();
        while (it.hasNext())
        {
            String deviceName = it.next();
            UsbDevice device = devices.get(deviceName);

            String VID = Integer.toHexString(device.getVendorId()).toUpperCase();
            String PID = Integer.toHexString(device.getProductId()).toUpperCase();
            txtBox.append(deviceName + " " +  VID + ":" + PID + " " + manager.hasPermission(device) + "\n");
        }
    }

}
