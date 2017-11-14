package com.bloodconnection.bluetoothconnection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Badgeable;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Date;

//custom packages
import add.bloodconnection.common.CommandTransfer;
import add.bloodconnection.common.Response;
import add.bloodconnection.common.TransferMessage;
import add.bloodconnection.common.configuration.BloodParts;
import add.bloodconnection.common.configuration.DeviceMac;
import add.bloodconnection.common.configuration.ErythrocytesData;
import add.bloodconnection.common.configuration.GlucozeData;
import add.bloodconnection.common.configuration.HemoglobineData;
import add.bloodconnection.common.configuration.LeucocytesData;

public class MainActivity extends AppCompatActivity {

    private TextView status;
    private Button btnConnect;
    private Dialog dialog;
    private ArrayAdapter<String> chatAdapter;
    private ArrayList<TransferMessage> callbacksFromDevice;
    private ArrayList<String> callbacksFromDeviceAdapter;
    private ArrayList<String> commandsToDevice;
    private BluetoothAdapter bluetoothAdapter;
    private Drawer.Result drawerResult = null;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_OBJECT = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_OBJECT = "device_name";

    private String deviceValue = new String();

    private static boolean isAnswerPositive = false;

    private DeviceMac trackerMac;
    private ErythrocytesData ery;
    private LeucocytesData lei;
    private HemoglobineData hem;
    //private ColorData clr;
    private GlucozeData glu;
    private List<BloodParts> data;
    private AlertDialog.Builder builder;

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private ChatController chatController;
    private BluetoothDevice connectingDevice;
    private ArrayAdapter<String> discoveredDevicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        findViewsByIds();

        initializeAlertBuilder();
        initializeObjects();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerResult = new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withHeader(R.layout.drawer_header)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_home).withBadge("99").withIdentifier(1),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_free_play).withIcon(FontAwesome.Icon.faw_gamepad),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_custom).withIcon(FontAwesome.Icon.faw_eye).withBadge("6").withIdentifier(2),
                        new SectionDrawerItem().withName(R.string.drawer_item_settings),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_help).withIcon(FontAwesome.Icon.faw_cog),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_open_source).withIcon(FontAwesome.Icon.faw_question).setEnabled(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_contact).withIcon(FontAwesome.Icon.faw_close).withIdentifier(1)
                ).withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), 0);
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) { }
                }).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        if (drawerItem instanceof Nameable) {
                            switch (MainActivity.this.getString(((Nameable) drawerItem).getNameRes())) {
                                case "Exit":
                                    finish();
                                    break;
                            }
                        }
                        if (drawerItem instanceof Badgeable) {
                            Badgeable badgeable = (Badgeable) drawerItem;
                            if (badgeable.getBadge() != null) {
                                // учтите, не делайте так, если ваш бейдж содержит символ "+"
                                try {
                                    int badge = Integer.valueOf(badgeable.getBadge());
                                    if (badge > 0) {
                                        drawerResult.updateBadge(String.valueOf(badge - 1), position);
                                    }
                                } catch (Exception e) {
                                    Log.d("test", "Не нажимайте на бейдж, содержащий плюс! :)");
                                }
                            }
                        }
                    }
                }).build();

        //check device support bluetooth or not
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_SHORT).show();
            finish();
        }

        //show bluetooth devices dialog when click connect button
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPrinterPickDialog();
            }
        });

        //set chat adapter

        for(BloodParts entry : data) {
            readBloodDataFromMemory(entry);
            Log.w("ENTRY", "" + entry.getDataLen());
        }

        try {
            tryGetDataFromMemory(Response.AFC);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void initializeAlertBuilder() {
        builder = new AlertDialog.Builder(this);
        builder.setTitle(Html.fromHtml("<font color='black'>Enter the tracker device code</font>"));

        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setTextColor(Color.rgb(0,0,0));
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();
                if(text.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")) {
                    sendMessage(new CommandTransfer(bluetoothAdapter.getAddress(), new Date(), Response.AFC));
                    dialog.cancel();
                } else {
                    input.setText(new String());
                    Toast.makeText(getApplicationContext(), "Tracker code has bad format", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendMessage(new CommandTransfer("", new Date(), Response.AFC));
                dialog.cancel();
            }
        });
    }

    public static boolean isIsAnswerPositive() {
        return isAnswerPositive;
    }

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatController.STATE_CONNECTED:
                            if(trackerMac.isEmpty()) {
                                trackerMac.setNewMac(connectingDevice.getAddress());
                                try {
                                    tryWriteDataToMemory(Response.AFC, trackerMac.getMac());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            sendMyCursors();
                            setStatus("Connected to tracker");
                            btnConnect.setEnabled(false);
                            break;
                        case ChatController.STATE_CONNECTING:
                            setStatus("Connecting...");
                            btnConnect.setEnabled(false);
                            break;
                        case ChatController.STATE_LISTEN:
                        case ChatController.STATE_NONE:
                            setStatus("Not connected");
                            btnConnect.setEnabled(true);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    TransferMessage yourObject = SerializationUtils.deserialize((byte[]) msg.obj);
                    commandsToDevice.add(yourObject.toString());
                    chatAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_READ:
                    synchronized (msg) {
                        TransferMessage tMsg = SerializationUtils.deserialize((byte[]) msg.obj);
                        Log.w("ENTRY", tMsg.toString());
                        callbacksFromDevice.add(tMsg);
                        callbacksFromDeviceAdapter.add(tMsg.toString());
                        manipulateRead(tMsg);
                        chatAdapter.notifyDataSetChanged();
                    }
                    break;
                case MESSAGE_DEVICE_OBJECT:
                    connectingDevice = msg.getData().getParcelable(DEVICE_OBJECT);
                    Toast.makeText(getApplicationContext(), "Connected to " + connectingDevice.getName(),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("toast"),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    void initializeObjects() {
        data = new ArrayList<>();
        callbacksFromDeviceAdapter = new ArrayList<>();
        callbacksFromDevice = new ArrayList<>();
        commandsToDevice = new ArrayList<>();
        chatAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, callbacksFromDeviceAdapter);

        trackerMac = new DeviceMac();
        ery = new ErythrocytesData();
        lei = new LeucocytesData();
        hem = new HemoglobineData();
        //clr = new ColorData();
        glu = new GlucozeData();

        data.add(lei);
        data.add(hem);
        data.add(glu);
        data.add(ery);
    }

    private void rewriteAddress() {
        String mac = trackerMac.getMac();
        String[] macs = mac.split(":");
        String result = "";
        for(int i = 0; i < macs.length; ++i) {
            if(macs[i].length() != 2) {
                result += "0" + macs[i];
            } else {
                result += macs[i];
            }
            if(i < macs.length - 1) {
                result += ":";
            }
        }
        trackerMac.free();
        trackerMac.setNewMac(result);
    }

    private synchronized void manipulateRead(TransferMessage tMsg) {
        Log.w("ENTRY", tMsg.toString());
        switch(tMsg.getReponseStatus()) {
            case ERY:
                writeMyData(ery, tMsg.getMessage());
                break;
            case LEU:
                writeMyData(lei, tMsg.getMessage());
                break;
            case HEM:
                writeMyData(hem, tMsg.getMessage());
                break;
            case GLU:
                writeMyData(glu, tMsg.getMessage());
                break;
            case AFC:
                builder.show();
                break;
            case BRACELET_MAC:
                String mesg = tMsg.getMessage();
                if(trackerMac.isEmpty()) {
                    trackerMac.setNewMac(mesg);
                    try {
                        tryWriteDataToMemory(Response.AFC, trackerMac.getMac());
                    } catch (Exception e) {
                        Log.w("ERROR", e.getMessage());
                    }
                }
                setStatus("Connected to tracker");
                break;
        }
    }

    private synchronized void writeBloodDataToMemory(BloodParts dataArray) {
        try {
            File file = new File(getFilesDir(), dataArray.getFileMemoryName());
            FileOutputStream fileout = openFileOutput(dataArray.getFileMemoryName(), Context.MODE_PRIVATE);
            for (int i = 0; i < dataArray.getDataLen(); ++i) {
                fileout.write((dataArray.read(i) + "\n").getBytes());
            }
            fileout.close();
        } catch (IOException exe) {
            exe.printStackTrace();
        }
    }

    private synchronized void readBloodDataFromMemory(BloodParts dataArray) {
        try {
            File file = new File(getFilesDir(), dataArray.getFileMemoryName());
            if(file.exists()) {
                int length = (int) file.length();
                byte[] bytes = new byte[length];
                FileInputStream fileIn = openFileInput(dataArray.getFileMemoryName());
                try {
                    fileIn.read(bytes);
                } finally {
                    fileIn.close();
                }
                String s = new String(bytes);
                String[] splitted = s.split("\n");
                for(int i = 0; i < splitted.length; ++i) {
                    dataArray.write(Long.parseLong(splitted[i], 10));
                }
                fileIn.close();
            }
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

    private void writeMyData(BloodParts bp, String msg) {
        try {
            String[] vals = msg.split(":");
            if(vals.length != 2) return;
            Long value = Long.parseLong(vals[1]);
            bp.write(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryGetDataFromMemory(Response rep) throws Exception {
        switch (rep) {
            case AFC:
                SharedPreferences sharedPref = this.getSharedPreferences(
                        getString(R.string.tracker_mac), Context.MODE_PRIVATE);
                String mac = sharedPref.getString(getString(R.string.tracker_mac), null);
                if (mac == null) {
                    throw new Exception("Tracker mac address was not found in memory");
                } else {
                    trackerMac.setNewMac(mac);
                }
                break;
        }
    }

    private void tryWriteDataToMemory(Response rep, String str) throws Exception {
        switch (rep) {
            case AFC:
                SharedPreferences sharedPref = this.getSharedPreferences(
                        getString(R.string.tracker_mac), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.tracker_mac), str);
                editor.commit();
                break;
        }
    }

    private void showPrinterPickDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_bluetooth);
        dialog.setTitle("Bluetooth Devices");

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

        //Initializing bluetooth adapters
        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(this, R.layout.custom_txtview);
        discoveredDevicesAdapter = new ArrayAdapter<>(this, R.layout.custom_txtview);

        //locate listviews and attatch the adapters
        ListView listView = (ListView) dialog.findViewById(R.id.pairedDeviceList);
        ListView listView2 = (ListView) dialog.findViewById(R.id.discoveredDeviceList);
        listView.setAdapter(pairedDevicesAdapter);
        listView2.setAdapter(discoveredDevicesAdapter);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryFinishReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryFinishReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            pairedDevicesAdapter.add(getString(R.string.none_paired));
        }

        //Handling listview item click event
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);
                connectToDevice(address);
                dialog.dismiss();
            }

        });

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);
                connectToDevice(address);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void setStatus(String s) {
        status.setText(s);
    }

    private void connectToDevice(String deviceAddress) {
        bluetoothAdapter.cancelDiscovery();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        chatController.connect(device);
    }

    private void findViewsByIds() {
        status = (TextView) findViewById(R.id.status);
        btnConnect = (Button) findViewById(R.id.btn_connect);
        Button btn_cnct_brac = (Button) findViewById(R.id.btn_on_bracelet);
		Button scan = (Button) findViewById(R.id.btn_send_msg_to_scan);

        btn_cnct_brac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(new CommandTransfer("D3:44:ED:E1:1F:9A", new Date(), Response.BRACELET_MAC));
            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(new CommandTransfer("Vibrate", new Date(), Response.BRACELET_VIBRATION));
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    chatController = new ChatController(this, handler);
                } else {
                    Toast.makeText(this, "Bluetooth still disabled, turn off application!", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void sendMessage(TransferMessage message) {
        if (chatController.getState() != ChatController.STATE_CONNECTED) {
            //Toast.makeText(this, "Connection was lost!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (message != null) {
            byte[] data = SerializationUtils.serialize(message);
            chatController.write(data);
        }
    }

    void sendMyCursors(){
        Log.w("HEREIS", "");
        sendMessage(new CommandTransfer(Response.ERY.getTextReponse() + ":" + String.valueOf(ery.getDataLen()),
                new Date(), Response.ERY));
        sendMessage(new CommandTransfer(Response.GLU.getTextReponse() + ":" + String.valueOf(glu.getDataLen()),
                new Date(), Response.GLU));
        sendMessage(new CommandTransfer(Response.HEM.getTextReponse() + ":" + String.valueOf(hem.getDataLen()),
                new Date(), Response.HEM));
        sendMessage(new CommandTransfer(Response.LEU.getTextReponse() + ":" + String.valueOf(lei.getDataLen()),
                new Date(), Response.LEU));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        } else {
            chatController = new ChatController(this, handler);
            if(!trackerMac.isEmpty()) {
                connectToDevice(trackerMac.getMac());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (chatController != null) {
            if (chatController.getState() == ChatController.STATE_NONE) {
                chatController.start();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Закрываем Navigation Drawer по нажатию системной кнопки "Назад" если он открыт
        if (drawerResult.isDrawerOpen()) {
            drawerResult.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatController != null) {
            chatController.stop();
        }
        for (BloodParts entry : data) {
            writeBloodDataToMemory(entry);
        }
    }

    private final BroadcastReceiver discoveryFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    discoveredDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (discoveredDevicesAdapter.getCount() == 0) {
                    discoveredDevicesAdapter.add(getString(R.string.none_found));
                }
            }
        }
    };
}