package com.bloodconnection.bluetoothconnection;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bloodconnection.bluetoothconnection.bracelet.helpers.CustomBluetoothProfile;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import add.bloodconnection.common.CommandTransfer;
import add.bloodconnection.common.MessageType;
import add.bloodconnection.common.Response;
import add.bloodconnection.common.TransferMessage;
import add.bloodconnection.common.configuration.BloodParts;
import add.bloodconnection.common.configuration.DeviceMac;
import add.bloodconnection.common.configuration.ErythrocytesData;
import add.bloodconnection.common.configuration.GlucozeData;
import add.bloodconnection.common.configuration.HemoglobineData;
import add.bloodconnection.common.configuration.LeucocytesData;
import add.bloodconnection.common.configuration.MemoryData;
import add.bloodconnection.common.lifecycle.*;


public class MainActivity extends AppCompatActivity {

    private TextView consoleMain;
    private TextView consoleSecondary;
    private Dialog dialog;
    private ArrayAdapter <String> chatAdapter;
    private ArrayList <TransferMessage> callbacksFromDevice;
    private ArrayList <String> callbacksFromDeviceAdapter;
    private ArrayList <String> commandsToDevice;
    private BluetoothAdapter bluetoothAdapter;
    private List <BloodParts> data;

    private Body body;
    private GlucoseResultHandler glucoseResultHandler;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_OBJECT = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int ALLOW_CONNECTION = 6;
    public static final int IS_LISTETING_HEART_RATE = 7;
    public static final int IS_SENDING_BATTERY_STATUS = 8;
    public static final String DEVICE_OBJECT = "device_name";
    private static boolean isAllowedToConnect = false;

    private boolean iveStartedNotification = false;

    private DeviceMac thisMac;
    private DeviceMac braceletMac;
    private DeviceMac androidMac;
    private ErythrocytesData ery;
    private LeucocytesData lei;
    private HemoglobineData hem;
    private GlucozeData glu;

    private AtomicInteger braceletStatus;

    Boolean isListeningHeartRate = false;

    BluetoothAdapter braceletAdapter;
    BluetoothGatt braceletGatt;
    BluetoothDevice braceletDevice;

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private ChatController chatController;
    private BluetoothDevice connectingDevice;
    private ArrayAdapter <String> discoveredDevicesAdapter;
    private boolean connected;
    private DataHandler dataHandler;

    private TransferMessage readBracelet;
    private TransferMessage writeBracelet;

    private static final int MAIN_TERMINAL = 0;
    private static final int SCANNING_TERMINAL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        findViewsByIds();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_SHORT).show();
            finish();
        }
            callbacksFromDeviceAdapter = new ArrayList<>();
            callbacksFromDevice = new ArrayList<>();
            commandsToDevice = new ArrayList<>();
            chatAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, callbacksFromDeviceAdapter);

            initializeObjects();

            body.eh.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            body.gh.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            body.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            glucoseResultHandler.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            //dataHandler.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static boolean isAllowedToConnect() {
        return isAllowedToConnect;
    }

    void initializeObjects() {
        braceletAdapter = BluetoothAdapter.getDefaultAdapter();
        thisMac = new DeviceMac();
        thisMac.setNewMac(new long[] {
                0x20,
                0x07,
                0x30,
                0x10,
                0x20,
                0x17
        });
        readBracelet = null;
        braceletMac = new DeviceMac();
        androidMac = new DeviceMac();
        try {
            tryGetDataFromMemory(Response.AFC);
        } catch (Exception exe) {
            writeToConsole(new CommandTransfer(exe.getMessage(), new Date(), Response.ERROR).toString(),
                    MessageType.Error, MAIN_TERMINAL);
        }

        try {
            tryGetDataFromMemory(Response.BRACELET_MAC);
            if (!braceletMac.isEmpty()) {
                connectBracelet();
            } else {
                writeToConsole(new CommandTransfer("Unable to connect bracelet", new Date(), Response.ERROR).toString(),
                        MessageType.Error, MAIN_TERMINAL);
            }
        } catch (Exception e) {
            writeToConsole(new CommandTransfer(e.getMessage(), new Date(), Response.ERROR).toString(),
                    MessageType.Error, MAIN_TERMINAL);
        }

        braceletStatus = new AtomicInteger(0);

        ery = new ErythrocytesData();
        lei = new LeucocytesData();
        hem = new HemoglobineData();
        glu = new GlucozeData();

        body = new Body();

        data = new LinkedList<>();

        glucoseResultHandler = new GlucoseResultHandler();

        data.add(lei);
        data.add(hem);
        data.add(glu);
        data.add(ery);

        for (BloodParts entry: data) {
            readBloodDataToMemory(entry);
        }
    }

    private void connectBracelet() throws Exception {
        if (braceletMac.isEmpty()) {
            throw new Exception("Bracelet was not found");
        }
        String address = braceletMac.getMac();
        braceletDevice = braceletAdapter.getRemoteDevice(address);

        Log.v("test", "Connecting to " + address);
        Log.v("test", "Device name " + braceletDevice.getName());

        braceletGatt = braceletDevice.connectGatt(this, true, bluetoothGattCallback);
    }

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatController.STATE_CONNECTED:
                            writeToConsole("Connected to: " + connectingDevice.getName(), MessageType.Connection, MAIN_TERMINAL);
                            if(!braceletMac.isEmpty()) {
                                sendMessage(new CommandTransfer("Bracelet Connected", new Date(),
                                        Response.BRACELET_CONNECTED));
                            }
                            if(!androidMac.isEmpty() && connectingDevice.getAddress().equalsIgnoreCase(androidMac.getMac())) {
                                askForCursors();
                            }
                            break;
                        case ChatController.STATE_CONNECTING:
                            writeToConsole("Connecting...", MessageType.Connection, MAIN_TERMINAL);
                            break;
                        case ChatController.STATE_LISTEN:
                        case ChatController.STATE_NONE:
                            writeToConsole("Not connected", MessageType.Connection, MAIN_TERMINAL);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    chatAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_READ:
                    synchronized(msg) {
                        TransferMessage tMsg = SerializationUtils.deserialize((byte[]) msg.obj);
                        try {
                            callbacksFromDevice.add(tMsg);
                            callbacksFromDeviceAdapter.add(tMsg.toString());
                            manipulateRead(tMsg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
                case ALLOW_CONNECTION:
                    Log.w("DEVICE", msg.getData().getString("device"));
                    Log.w("DEVICE", androidMac.getMac());
                    if (!msg.getData().getString("device").equals(androidMac.getMac())
                            && !msg.getData().getString("device").equals(braceletMac.getMac())) {
                        sendMessage(new CommandTransfer(thisMac.getMac(), new Date(), Response.AFC));
                    }
                    break;
            }
            return false;
        }
    });

    private void askForCursors() {
        sendMessage(new CommandTransfer("CURSORS", new Date(), Response.CURSORS));
    }

    private void showPrinterPickDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_bluetooth);
        dialog.setTitle("Bluetooth Devices");

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

        ArrayAdapter < String > pairedDevicesAdapter = new ArrayAdapter < > (this, R.layout.custom_txtview);
        discoveredDevicesAdapter = new ArrayAdapter < > (this, R.layout.custom_txtview);

        ListView listView = (ListView) dialog.findViewById(R.id.pairedDeviceList);
        ListView listView2 = (ListView) dialog.findViewById(R.id.discoveredDeviceList);
        listView.setAdapter(pairedDevicesAdapter);
        listView2.setAdapter(discoveredDevicesAdapter);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryFinishReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryFinishReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set <BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device: pairedDevices) {
                pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            pairedDevicesAdapter.add(getString(R.string.none_paired));
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView < ? > parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                connectToDevice(address);
                dialog.dismiss();
            }

        });

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView < ? > adapterView, View view, int i, long l) {
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

    private synchronized void manipulateRead(TransferMessage msg) throws InterruptedException {
        switch (msg.getReponseStatus()) {
            case BRACELET_MAC:
                if (braceletMac.isEmpty()) {
                    writeToConsole(msg.toString(), MessageType.Response, MAIN_TERMINAL);
                    try {
                        braceletMac.setNewMac(msg.getMessage());
                        connectBracelet();
                        tryWriteDataToMemory(Response.BRACELET_MAC, msg.getMessage().getBytes());
                    } catch (Exception exe) {
                        writeToConsole(new CommandTransfer(exe.getMessage(), new Date(), Response.ERROR).toString(),
                                MessageType.Error, MAIN_TERMINAL);
                    }
                }
                break;
            case BRACELET_VIBRATION:
                try {
                    startVibrate((byte)3);
                    writeToConsole(msg.toString(), MessageType.Response, MAIN_TERMINAL);
                } catch (Exception exe) {
                    writeToConsole(new CommandTransfer(exe.getMessage(), new Date(), Response.ERROR).toString(),
                            MessageType.Error, MAIN_TERMINAL);
                }
                break;
            case AFC:
                String str = msg.getMessage();
                if (androidMac.isEmpty() && thisMac.getMac().equals(str)) {
                    androidMac.setNewMac(connectingDevice.getAddress());
                    sendMessage(new CommandTransfer(thisMac.getMac(), new Date(), Response.BRACELET_MAC));
                    try {
                        tryWriteDataToMemory(Response.AFC, androidMac.getMac().getBytes());
                        askForCursors();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    chatController.stop();
                    chatController.start();
                }
                break;
            case BRACELET_STARTHEARTRATE:
                new Handler().post(new Runnable(){
                    @Override
                    public void run() {
                        while(true) {
                            if(braceletStatus.get() == 0) {
                                braceletStatus.set(IS_LISTETING_HEART_RATE);
                                startScanHeartRate();
                                break;
                            }
                        }
                    }
                });
                break;
            case BRACELET_CONNECTED:
                if(!braceletMac.isEmpty()) {
                    sendMessage(new CommandTransfer("Bracelet Connected", new Date(), Response.BRACELET_CONNECTED));
                }
                break;
            case BRACELET_BATTERY:
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        while(true) {
                            if(braceletStatus.get() == 0) {
                                braceletStatus.set(IS_SENDING_BATTERY_STATUS);
                                getBatteryStatus();
                                break;
                            }
                        }
                    }
                });
                break;
            case CURSORS:
                String[] strings = msg.getMessage().split(":");
                if(dataHandler == null && strings.length == 4) {
                    int i1 = Integer.valueOf(strings[0]);
                    int i2 = Integer.valueOf(strings[1]);
                    int i3 = Integer.valueOf(strings[2]);
                    int i4 = Integer.valueOf(strings[3]);
                    dataHandler = new DataHandler(i1, i2, i3, i4);
                    dataHandler.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    writeToConsole("Data was synced with phone", MessageType.Reason, MAIN_TERMINAL);
                }
                break;
            default:
                writeToConsole(msg.toString(), MessageType.Query, MAIN_TERMINAL);
                break;
        }
    }

    private void writeToConsole(String s, MessageType type, final int activityXML) {
        final MessageType t = type;
        final String sTemp = new String(s);
        runOnUiThread(new Runnable() {
            public void run() {
                String str = new String(sTemp);
                switch (t) {
                    case Connection:
                        str = ("<font color='yellow'>> " + str + "</font><br>");
                        break;
                    case Error:
                        str = ("<font color='red'>> " + str + "</font><br>");
                        break;
                    case Query:
                        str = ("<font color='#FF4081'>> " + str + "</font><br>");
                        break;
                    case Response:
                        str = ("<font color='green'>> " + str + "</font><br>");
                        break;
                    case Bracelet:
                        str = ("<font color='cyan'>> " + str + "</font><br>");
                        break;
                    case Ery:
                        str = ("<font color='yellow'>------------<br>> " + str + "<br>------------</font><br>");
                        break;
                    case Hem:
                        str = ("<font color='#ffe6ff'>------------<br>> " + str + "<br>------------</font><br>");
                        break;
                    case Col:
                        str = ("<font color='#ff3399'>------------<br>> " + str + "<br>------------</font><br>");
                        break;
                    case Leu:
                        str = ("<font color='green'>------------<br>> " + str + "<br>------------</font><br>");
                        break;
                    case Glu:
                        str = ("<font color='#00b3b3'>------------<br>> " + str + "<br>------------</font><br>");
                        break;
                    case Reason:
                        str = ("<font color='#ffcc00'>------------<br>> " + str + "<br>------------</font><br>");
                        break;
                }
                if (activityXML == MAIN_TERMINAL) {
                    consoleMain.append(Html.fromHtml(str));
                } else {
                    if (activityXML == SCANNING_TERMINAL) {
                        consoleSecondary.append(Html.fromHtml(str));
                    }
                }
            }
        });
    }

    private void connectToDevice(String deviceAddress) {
        bluetoothAdapter.cancelDiscovery();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        chatController.connect(device);
    }

    public void findViewsByIds() {
        consoleMain = (TextView) findViewById(R.id.innerTerminal);
        consoleMain.setMovementMethod(new ScrollingMovementMethod());
        consoleSecondary = (TextView) findViewById(R.id.innerTerminalScanner);
        consoleSecondary.setMovementMethod(new ScrollingMovementMethod());
        final Switch btnMain = (Switch) findViewById(R.id.fl1).findViewById(R.id.switch1);
        final Switch btnSec = (Switch) findViewById(R.id.fl2).findViewById(R.id.switch2);

        btnMain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                findViewById(R.id.fl1).setVisibility(View.GONE);
                findViewById(R.id.fl2).setVisibility(View.VISIBLE);
                btnMain.setChecked(false);
            }
        });

        btnSec.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                findViewById(R.id.fl2).setVisibility(View.GONE);
                findViewById(R.id.fl1).setVisibility(View.VISIBLE);
                btnSec.setChecked(true);
            }
        });
    }

    public void onClickStop(View v) {
        if (braceletAdapter != null) {
            braceletAdapter.disable();
        }
        if (bluetoothAdapter != null) {
            bluetoothAdapter.disable();
        }
        finish();
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

    private synchronized void sendMessage(TransferMessage message) {
        if (chatController.getState() != ChatController.STATE_CONNECTED) {
            //Toast.makeText(this, "Connection was lost!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (message != null) {
            byte[] data = SerializationUtils.serialize(message);
            chatController.write(data);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        } else {
            chatController = new ChatController(this, handler);
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
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatController != null) {
            chatController.stop();
        }
        body.eh.cancel(true);
        body.gh.cancel(true);
        body.cancel(true);
        glucoseResultHandler.cancel(true);

        for (BloodParts entry: data) {
            writeBloodDataToMemory(entry);
        }
    }

    private synchronized void writeBloodDataToMemory(BloodParts dataArray) {
        try {
            FileOutputStream fileout = openFileOutput(dataArray.getFileMemoryName(), Context.MODE_PRIVATE);
            for (int i = 0; i < dataArray.getDataLen(); ++i) {
                fileout.write((dataArray.read(i) + "\n").getBytes());
            }
            fileout.close();
        } catch (IOException exe) {
            exe.printStackTrace();
        }
    }

    private synchronized void readBloodDataToMemory(BloodParts dataArray) {
        try {
            File file = new File(getFilesDir(), dataArray.getFileMemoryName());
            if (file.exists()) {
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
                for (int i = 0; i < splitted.length; ++i) {
                    dataArray.write(Long.parseLong(splitted[i], 10));
                }
                fileIn.close();
            }
        } catch (Exception exe) {
            exe.printStackTrace();
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

    private void tryGetDataFromMemory(Response rep) throws Exception {
        if(rep == Response.BRACELET_MAC) {
            SharedPreferences prefs = getSharedPreferences("strings", 0);
            String mac = prefs.getString(getString(R.string.bracelet_mac), "NIL");
            if (mac == "NIL") {
                throw new Exception("Bracelet mac address was not found in memory");
            } else {
                braceletMac.setNewMac(mac);
            }
        } else {
            if(rep == Response.AFC) {
                SharedPreferences prefs = getSharedPreferences("phonemac", 0);
                String macAndroid = prefs.getString(getString(R.string.phone_mac), "NIL");
                if (macAndroid == "NIL") {
                    throw new Exception("Phone mac address was not found in memory");
                } else {
                    androidMac.setNewMac(macAndroid);
                }
            }
        }
    }

    private void tryWriteDataToMemory(Response rep, byte[] bytes) throws Exception {
        if(rep == Response.BRACELET_MAC) {
            SharedPreferences prefs = getSharedPreferences("strings", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.bracelet_mac), new String(bytes));
            editor.commit();
        } else {
            if(rep == Response.AFC) {
                SharedPreferences prefs = getSharedPreferences("phonemac", 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(getString(R.string.phone_mac), new String(bytes));
                editor.commit();
            }
      }
    }

    private void stateConnected() {
        braceletGatt.discoverServices();
        connected = true;
        writeToConsole(new CommandTransfer("Connected to bracelet", new Date(), Response.OK).toString(),
                MessageType.Bracelet, MAIN_TERMINAL);
        if(!braceletMac.isEmpty()) {
            sendMessage(new CommandTransfer("Bracelet Connected", new Date(), Response.BRACELET_CONNECTED));
        }
        readBracelet = null;
    }

    private void stateDisconnected() {
        braceletGatt.disconnect();
        connected = false;
        writeToConsole(new CommandTransfer("Bracelet disconnected", new Date(), Response.OK).toString(),
                MessageType.Bracelet, MAIN_TERMINAL);
    }

    private void startScanHeartRate() {
        BluetoothGattCharacteristic bchar = braceletGatt.getService(CustomBluetoothProfile.HeartRate.service)
                .getCharacteristic(CustomBluetoothProfile.HeartRate.controlCharacteristic);
        bchar.setValue(new byte[] {
                21,
                2,
                1
        });
        braceletGatt.writeCharacteristic(bchar);
        writeToConsole(new CommandTransfer("Scanning heart rate", new Date(), Response.BRACELET_STARTHEARTRATE).toString(),
                MessageType.Bracelet, MAIN_TERMINAL);
    }

    private void getConnectionChar() {
        BluetoothGattCharacteristic bchar = braceletGatt.getService(CustomBluetoothProfile.BraceletChars.service)
                .getCharacteristic(CustomBluetoothProfile.BraceletChars.connectionCharacteristic);
        if (!braceletGatt.readCharacteristic(bchar)) {
            writeToConsole(new CommandTransfer("Failed to get scan info", new Date(), Response.ERROR).toString(),
                    MessageType.Bracelet, SCANNING_TERMINAL);
            Toast.makeText(this, "Failed to get scan info", Toast.LENGTH_SHORT).show();
        }
    }

    private void listenHeartRate() {
        BluetoothGattCharacteristic bchar = braceletGatt.getService(CustomBluetoothProfile.HeartRate.service)
                .getCharacteristic(CustomBluetoothProfile.HeartRate.measurementCharacteristic);
        braceletGatt.setCharacteristicNotification(bchar, true);
        BluetoothGattDescriptor descriptor = bchar.getDescriptor(CustomBluetoothProfile.HeartRate.descriptor);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        braceletGatt.writeDescriptor(descriptor);
        isListeningHeartRate = true;
    }

    private void readScanHeartRate() {
        BluetoothGattCharacteristic bchar = braceletGatt.getService(CustomBluetoothProfile.HeartRate.service)
                .getCharacteristic(CustomBluetoothProfile.HeartRate.controlCharacteristic);
        if (!braceletGatt.readCharacteristic(bchar)) {
            writeToConsole(new CommandTransfer("Failed to get scan info", new Date(), Response.ERROR).toString(),
                    MessageType.Bracelet, SCANNING_TERMINAL);
            Toast.makeText(this, "Failed to get scan info", Toast.LENGTH_SHORT).show();
        }
        writeToConsole(new CommandTransfer("Getting heart rate info", new Date(), Response.BRACELET_LISTENHEARTRATE).toString(),
                MessageType.Bracelet, SCANNING_TERMINAL);
    }

    private void getBatteryStatus() {
        BluetoothGattCharacteristic bchar = braceletGatt.getService(CustomBluetoothProfile.Basic.service)
                .getCharacteristic(CustomBluetoothProfile.Basic.batteryCharacteristic);
        if (!braceletGatt.readCharacteristic(bchar)) {
            writeToConsole(new CommandTransfer("Failed to get battery info", new Date(), Response.ERROR).toString(),
                    MessageType.Bracelet, MAIN_TERMINAL);
            Toast.makeText(this, "Failed get battery info", Toast.LENGTH_SHORT).show();
        }
        writeToConsole(new CommandTransfer("Getting battery braceletStatus", new Date(), Response.BRACELET_BATTERY).toString(),
                MessageType.Bracelet, MAIN_TERMINAL);
    }

    private void startVibrate(byte type) {
        BluetoothGattCharacteristic bchar = braceletGatt.getService(CustomBluetoothProfile.AlertNotification.service)
                .getCharacteristic(CustomBluetoothProfile.AlertNotification.alertCharacteristic);
        bchar.setValue(new byte[] {
                type
        });
        if (!braceletGatt.writeCharacteristic(bchar)) {
            writeToConsole(new CommandTransfer("Failed start vibration", new Date(), Response.ERROR).toString(),
                    MessageType.Bracelet, MAIN_TERMINAL);
            Toast.makeText(this, "Failed start vibrate", Toast.LENGTH_SHORT).show();
        }

    }

    private void stopVibrate() {
        writeToConsole(new CommandTransfer("Vibration stopped", new Date(), Response.OK).toString(),
                MessageType.Bracelet, MAIN_TERMINAL);
        BluetoothGattCharacteristic bchar = braceletGatt.getService(CustomBluetoothProfile.AlertNotification.service)
                .getCharacteristic(CustomBluetoothProfile.AlertNotification.alertCharacteristic);
        bchar.setValue(new byte[] {
                0
        });
        if (!braceletGatt.writeCharacteristic(bchar)) {
            writeToConsole(new CommandTransfer("Failed stop vibration", new Date(), Response.OK).toString(),
                    MessageType.Bracelet, MAIN_TERMINAL);
            Toast.makeText(this, "Failed stop vibrate", Toast.LENGTH_SHORT).show();
        }

    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.v("test", "onConnectionStateChange");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                stateConnected();
                isAllowedToConnect = true;
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                stateDisconnected();
                isAllowedToConnect = false;
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.v("test", "onServicesDiscovered");
            listenHeartRate();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.v("test", "onCharacteristicRead");
            byte[] data = characteristic.getValue();
            writeToConsole(String.valueOf(data[1]),
                    MessageType.Query, MAIN_TERMINAL);
            switch (braceletStatus.get()) {
                case IS_LISTETING_HEART_RATE:
                    sendMessage(new CommandTransfer(String.valueOf(data[1]), new Date(), Response.BRACELET_LISTENHEARTRATE));
                    break;
                case IS_SENDING_BATTERY_STATUS:
                    sendMessage(new CommandTransfer(String.valueOf(data[1]), new Date(), Response.BRACELET_BATTERY));
                    break;
            }
            braceletStatus.set(0);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.v("test", "onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.v("test", "onCharacteristicChanged");
            byte[] data = characteristic.getValue();
            writeToConsole(String.valueOf(data[1]),
                    MessageType.Query, MAIN_TERMINAL);
            switch (braceletStatus.get()) {
                case IS_LISTETING_HEART_RATE:
                    sendMessage(new CommandTransfer(String.valueOf(data[1]), new Date(), Response.BRACELET_LISTENHEARTRATE));
                    break;
                case IS_SENDING_BATTERY_STATUS:
                    sendMessage(new CommandTransfer(String.valueOf(data[1]), new Date(), Response.BRACELET_BATTERY));
                    break;
            }
            braceletStatus.set(0);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.v("test", "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.v("test", "onDescriptorWrite");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.v("test", "onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.v("test", "onReadRemoteRssi");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.v("test", "onMtuChanged");
        }

    };

    class Body extends AsyncTask < Void, Void, Void > {
        private LifePoint leukocytesCount,
                eritrocytesCount,
                hemoglobineCount,
                glucoseCount;
        private List < ReasonTimeCount > reasonTimeCountsList;
        BloodPartsHandler eh;
        GlucozeHandler gh;

        private Body() {
            this.reasonTimeCountsList = new LinkedList<>();
            this.leukocytesCount = new LeukocytesCount(this.reasonTimeCountsList);
            this.eritrocytesCount = new ErythrocytesCount(this.reasonTimeCountsList);
            this.hemoglobineCount = new HemoglobineCount(this.reasonTimeCountsList);
            this.glucoseCount = new GlucozeCount(this.reasonTimeCountsList);
            this.eh = new BloodPartsHandler(this.eritrocytesCount, ery, this.leukocytesCount, lei, this.hemoglobineCount, hem);
            this.gh = new GlucozeHandler(this.glucoseCount, glu);
        }

        @Override
        protected Void doInBackground(Void...voids) {
            String result = new String();
            Random random = new Random();
            double chance = 0;
            RandomDataGenerator rdg = new RandomDataGenerator();
            writeToConsole("Body was started", MessageType.Ery, SCANNING_TERMINAL);
            while (true) {
                if (!result.isEmpty()) {
                    result = new String();
                }
                chance = random.nextDouble();
                Log.w("", "" + chance);
                if (chance < 0.00000000002) {
                    if (!findByReason(Reason.PREGNANCY)) {
                        reasonTimeCountsList.add(new ReasonTimeCount(Reason.PREGNANCY,
                                rdg.nextLong(23932800000L, 31881600000L)));
                        result = "User is pregnant!";
                        writeToConsole(result, MessageType.Reason, SCANNING_TERMINAL);
                    }
                } else {
                    if (chance < 0.00005) {
                        if (findByReason(Reason.FOOD_DRINK)) {
                            reasonTimeCountsList.add(new ReasonTimeCount(Reason.FOOD_DRINK,
                                    rdg.nextLong(1800, 3600)));
                            result = "User ate again. Not long time passed since last time he was eating...";
                            writeToConsole(result, MessageType.Reason, SCANNING_TERMINAL);
                        }
                    } else {
                        if (chance < 0.0002) {
                            if (!findByReason(Reason.SMOKING)) {
                                reasonTimeCountsList.add(new ReasonTimeCount(Reason.SMOKING,
                                        rdg.nextLong(157680000000L, 473040000000L)));
                                result = "User started smoking";
                                writeToConsole(result, MessageType.Reason, SCANNING_TERMINAL);
                            }
                        } else {
                            if (chance < 0.10) {
                                if (!findByReason(Reason.FOOD_DRINK)) {
                                    reasonTimeCountsList.add(new ReasonTimeCount(Reason.FOOD_DRINK,
                                            rdg.nextLong(36000L, 72000L)));
                                    result = "User ate";
                                    writeToConsole(result, MessageType.Reason, SCANNING_TERMINAL);
                                    if (findByReason(Reason.THIRST)) {
                                        removeByReason(Reason.THIRST);
                                    }
                                }
                            } else {
                                if (chance < 0.15) {
                                    if (!findByReason(Reason.THIRST) && !findByReason(Reason.FOOD_DRINK)) {
                                        reasonTimeCountsList.add(new ReasonTimeCount(Reason.THIRST,
                                                rdg.nextLong(600000L, 3600000L)));
                                        result = "User is thirsting";
                                        writeToConsole(result, MessageType.Reason, SCANNING_TERMINAL);
                                    }
                                }
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean findByReason(Reason rs) {
            for (ReasonTimeCount res: reasonTimeCountsList) {
                if (rs == res.getReason()) {
                    return true;
                }
            }
            return false;
        }

        private void removeByReason(Reason rs) {
            for (ReasonTimeCount res: reasonTimeCountsList) {
                if (rs == res.getReason()) {
                    reasonTimeCountsList.remove(rs);
                    return;
                }
            }
        }
    }

    class BloodPartsHandler extends AsyncTask < Void, Void, Void > {

        private LeukocytesCount leukocytesCount;
        private LeucocytesData leucocytesData;
        private ErythrocytesCount erythrocytesCount;
        private ErythrocytesData erythrocytesData;
        private HemoglobineData hemoglobineData;
        private HemoglobineCount hemoglobineCount;
        private int cursor;
        private long resultHem;
        private long resultLeu;
        private long resultEri;

        public BloodPartsHandler(LifePoint erythrocytesCount, MemoryData erythrocytesData,
                                 LifePoint leukocytesCount, MemoryData leucocytesData,
                                 LifePoint hemoglobineCount, MemoryData hemoglobineData) {
            this.erythrocytesCount = (ErythrocytesCount) erythrocytesCount;
            this.erythrocytesData = (ErythrocytesData) erythrocytesData;
            this.leukocytesCount = (LeukocytesCount) leukocytesCount;
            this.leucocytesData = (LeucocytesData) leucocytesData;
            this.hemoglobineCount = (HemoglobineCount) hemoglobineCount;
            this.hemoglobineData = (HemoglobineData) hemoglobineData;
            this.cursor = 0;
            this.resultLeu = 0;
        }

        @Override
        protected Void doInBackground(Void...voids) {
            long tmp = (long) Math.pow(10, 3);
            while (!this.erythrocytesData.isFilled() || !this.leucocytesData.isFilled() || !this.hemoglobineData.isFilled()) {
                long startTime = System.nanoTime();
                for (int i = 0; i < tmp; ++i) {
                    try {
                        this.erythrocytesCount.produce();
                        this.leukocytesCount.produce();
                        this.hemoglobineCount.produce();
                        resultHem += this.hemoglobineCount.getBloodCharacts();
                        resultEri += this.erythrocytesCount.getBloodCharacts();
                        resultLeu += this.leukocytesCount.getBloodCharacts();
                        Thread.sleep(4);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                long endTime = System.nanoTime();
                if (!this.erythrocytesData.isFilled() || !this.leucocytesData.isFilled() || !this.hemoglobineData.isFilled()) {
                    try {
                        this.erythrocytesData.write(resultEri);
                        this.leucocytesData.write(resultLeu);
                        this.hemoglobineData.write(resultHem);
                        long time = (endTime - startTime) / 1000000;
                        Log.w("This is the output", "Cycle was ended: " + time);
                        writeToConsole("{ Scanned erythrocytes: " + resultEri + " (qua per liter) }<br>Scanning was: " + time + " ms",
                                MessageType.Ery, SCANNING_TERMINAL);
                        writeToConsole("{ Scanned leucocytes: " + resultLeu + " (qua per liter) }<br>Scanning was: " + time + " ms",
                                MessageType.Leu, SCANNING_TERMINAL);
                        writeToConsole("{ Scanned hemoglobine: " + resultHem + " (g per liter) }<br>Scanning was: " + time + " ms",
                                MessageType.Hem, SCANNING_TERMINAL);
                        cursor++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }
                this.resultLeu = 0;
                this.resultEri = 0;
                this.resultHem = 0;
            }
            return null;
        }
    }

    class GlucozeHandler extends AsyncTask < Void, Void, Void > {

        private GlucozeData glucozeData;
        private GlucozeCount glucozeCount;
        private long result;

        public GlucozeHandler(LifePoint glucozeCount, MemoryData glucozeData) {
            this.glucozeCount = (GlucozeCount) glucozeCount;
            this.glucozeData = (GlucozeData) glucozeData;
            this.result = 0;
        }

        @Override
        protected Void doInBackground(Void...voids) {
            while (!this.glucozeData.isFilled()) {
                try {
                    if (!this.glucozeCount.isDiabetics()) {
                        this.glucozeCount.tryToMakeDiabetic();
                        if (this.glucozeCount.isDiabetics()) {
                            glucoseResultHandler.setDiabetics();
                            writeToConsole("User is suffering from diabetes now", MessageType.Reason, SCANNING_TERMINAL);
                        }
                    }
                    this.glucozeCount.produce();
                    long query = this.glucozeCount.getBloodCharacts();
                    result += query;
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!this.glucozeData.isFilled()) {
                    try {
                        this.glucozeData.write(result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }
                this.result = 0;
            }
            return null;
        }
    }

    class GlucoseResultHandler extends AsyncTask <Void, Void, Void> {

        private int gluCusror,
                gluCounter;
        private boolean isDiabetics;

        public GlucoseResultHandler() {
            this.gluCusror = 1;
            this.isDiabetics = false;
            this.gluCounter = 0;
        }

        public void setDiabetics() {
            this.isDiabetics = true;
        }

        @Override
        protected Void doInBackground(Void...voids) {
            while (true) {
                while ((gluCusror % 10) != 0) {
                    long glucoze = glu.read(this.gluCusror - 1);
                    if (glucoze != 0) {
                        if (isDangerous(glucoze)) {
                            Log.w("Diabetes", "" + (glucoze / 10000));
                            this.gluCounter++;
                        } else {
                            this.gluCounter--;
                        }
                        this.gluCusror++;
                    }
                }
                if (this.gluCounter >= 5) {
                    Log.w("Diabetes", "dangerous");
                    if (braceletConnected()) {
                        try {
                            startVibrate((byte)2);
                            Thread.sleep(3000);
                            stopVibrate();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    this.gluCounter = 0;
                }
                this.gluCusror++;
            }
        }

        private boolean isDangerous(long glucoze) {
            return (((glucoze > (110 * 11312) || glucoze < (39 * 11312)) && this.isDiabetics) || ((glucoze > (70 * 11312) || glucoze < (30 * 11312)) && !this.isDiabetics));
        }

        private boolean braceletConnected() {
            if (braceletAdapter == null || braceletGatt == null) {
                return false;
            } else {
                if (!braceletAdapter.isEnabled()) {
                    return false;
                } else {
                    return braceletAdapter.getBondedDevices().size() > 0;
                }
            }
        }
    }

    class DataHandler extends AsyncTask <Void, CommandTransfer, Void> {

        private int hemCursor,
                leuCursor,
                eriCursor,
                gluCursor;

        public DataHandler(int eriCursor, int gluCursor, int hemCursor, int leuCursor) {
            this.eriCursor = eriCursor;
            this.gluCursor = gluCursor;
            this.hemCursor = hemCursor;
            this.leuCursor = leuCursor;
        }

        @Override
        protected Void doInBackground(Void...voids) {
            while (true) {
                    long readEry = ery.read(this.eriCursor);
                    long readGlu = glu.read(this.gluCursor);
                    long readHem = hem.read(this.hemCursor);
                    long readLeu = lei.read(this.leuCursor);
                    if (readEry != 0 && (this.eriCursor != ery.getDataSize())) {
                        this.publishProgress(new CommandTransfer("ERY:" + readEry, new Date(), Response.ERY));
                        this.eriCursor++;
                    } else {
                        if (readLeu != 0 && (this.leuCursor != lei.getDataSize())) {
                            this.publishProgress(new CommandTransfer("LEU:" + readLeu, new Date(), Response.LEU));
                            this.leuCursor++;
                        } else {
                            if (readHem != 0 && (this.hemCursor != hem.getDataSize())) {
                                this.publishProgress(new CommandTransfer("HEM:" + readHem, new Date(), Response.HEM));
                                this.hemCursor++;
                            } else {
                                if (readGlu != 0 && (this.gluCursor != glu.getDataSize())) {
                                    this.publishProgress(new CommandTransfer("GLU:" + readGlu, new Date(), Response.GLU));
                                    this.gluCursor++;
                                }
                            }
                        }
                    }

            }
        }

        @Override
        protected void onProgressUpdate(CommandTransfer...values) {
            super.onProgressUpdate(values);
            for (CommandTransfer value: values) {
                sendMessage(value);
            }
        }
    }
}