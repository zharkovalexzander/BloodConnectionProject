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
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.CircleProgress;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Badgeable;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.victor.loading.rotate.RotateLoading;

import org.apache.commons.lang3.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import add.bloodconnection.common.misc.GraphicsProcessing;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {

    private TextView status;
    private Button btnConnect;
    private Button btnBracelet;
    private Button btnHr;
    private Button btnBat;
    private AlertDialog.Builder dialog;
    private Dialog dialogBd;
    private ImageView heart;
    //private ImageView rotatingGlu;
    private List<GifImageView> rotators;
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
    private PrimaryDrawerItem pdI;

    private static boolean isAnswerPositive = false;

    private DeviceMac trackerMac;
    private ErythrocytesData ery;
    private LeucocytesData lei;
    private HemoglobineData hem;
    private GlucozeData glu;
    private List<BloodParts> data;

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

        pdI = new PrimaryDrawerItem().withName(R.string.drawer_item_custom).withIcon(FontAwesome.Icon.faw_wifi).setEnabled(true);

        drawerResult = new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withHeader(R.layout.drawer_header)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_home),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_connections).withIcon(FontAwesome.Icon.faw_bolt),
                        pdI.setEnabled(false),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_contact).withIcon(FontAwesome.Icon.faw_close)
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
                            RelativeLayout rlH = (RelativeLayout) findViewById(R.id.home);
                            RelativeLayout rlC = (RelativeLayout) findViewById(R.id.connections);
                            RelativeLayout rlB = (RelativeLayout) findViewById(R.id.bracelet);
                            switch (MainActivity.this.getString(((Nameable) drawerItem).getNameRes())) {
                                case "Exit":
                                    finish();
                                    break;
                                case "Home":
                                    rlC.setVisibility(View.GONE);
                                    rlB.setVisibility(View.GONE);
                                    rlH.setVisibility(View.VISIBLE);
                                    break;
                                case "Connection":
                                    rlH.setVisibility(View.GONE);
                                    rlB.setVisibility(View.GONE);
                                    rlC.setVisibility(View.VISIBLE);
                                    break;
                                case "Bracelet":
                                    rlH.setVisibility(View.GONE);
                                    rlC.setVisibility(View.GONE);
                                    rlB.setVisibility(View.VISIBLE);
                                    break;
                            }
                        }
                        if (drawerItem instanceof Badgeable) {
                            Badgeable badgeable = (Badgeable) drawerItem;
                            if (badgeable.getBadge() != null) {
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

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPrinterPickDialog();
            }
        });


        for(BloodParts entry : data) {
            readBloodDataFromMemory(entry);
            if(!entry.isEmpty()) {
                try {
                    displayData(entry, entry.read(entry.getDataLen() - 1), true, R.drawable.giphy);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        try {
            tryGetDataFromMemory(Response.AFC);
        } catch (Exception e) {
            e.printStackTrace();
        }

       // setAnimations();

        changeButton(btnConnect, "#009885", true, "Connect");
    }


    private void setAnimations() {
        GraphicsProcessing.CycleFadeAnimation(heart, 1000, 1000);
    }

    private void initializeAlertBuilder() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View alertView = layoutInflater.inflate(R.layout.asker, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final EditText userInput1 = (EditText) alertView.findViewById(R.id.ed1);
        final EditText userInput2 = (EditText) alertView.findViewById(R.id.ed2);
        final EditText userInput3 = (EditText) alertView.findViewById(R.id.ed3);
        final EditText userInput4 = (EditText) alertView.findViewById(R.id.ed4);
        final EditText userInput5 = (EditText) alertView.findViewById(R.id.ed5);
        final EditText userInput6 = (EditText) alertView.findViewById(R.id.ed6);
        Button okButton = (Button) alertView.findViewById(R.id.okButton);
        Button cancelButton = (Button) alertView.findViewById(R.id.cancelButton);


        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder bd = new StringBuilder();
                bd.append(userInput1.getText()).append(":")
                        .append(userInput2.getText()).append(":")
                        .append(userInput3.getText()).append(":")
                        .append(userInput4.getText()).append(":")
                        .append(userInput5.getText()).append(":")
                        .append(userInput6.getText());
                String text = bd.toString().toString();
                if(text.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")) {
                    sendMessage(new CommandTransfer(text, new Date(), Response.AFC));
                    //dialog.dismiss();
                } else {
                    userInput1.setText(new String());
                    userInput2.setText(new String());
                    userInput3.setText(new String());
                    userInput4.setText(new String());
                    userInput5.setText(new String());
                    userInput6.setText(new String());
                    Toast.makeText(getApplicationContext(), "Tracker code has bad format", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(new CommandTransfer("", new Date(), Response.AFC));
                //dialog.dismiss();
            }
        });

        alertDialogBuilder.setView(alertView);
        dialog = alertDialogBuilder;
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
                            heart.clearAnimation();
                            heart.setAlpha(1.0f);

                            changeButton(btnConnect, "#1D8508", false, "Connected");
                            changeButton(btnBracelet, "#009885", true, "Connect");

                            ((TextView) findViewById(R.id.t2)).setText("is connected");

                            TextView twC = (TextView) findViewById(R.id.trackertext);
                            twC.setText("Blood cells scanner: " + connectingDevice.getName() + System.getProperty("line.separator") +
                                    "MAC-Address: " + connectingDevice.getAddress() + System.getProperty("line.separator") +
                                    "Bond state: " + (connectingDevice.getBondState() == BluetoothDevice.BOND_BONDED) + System.getProperty("line.separator") +
                                    "BloodConnection® Health Monitoring System"
                            );

                            sendMessage(new CommandTransfer("Is your bracelet connected?", new Date(), Response.BRACELET_CONNECTED));

                            sendMyCursors();

                            break;
                        case ChatController.STATE_CONNECTING:
                            /*btnConnect.setBackgroundColor(Color.parseColor("#1D8508"));

                            btnConnect.setText("Connected");*/

                            btnConnect.setEnabled(false);
                            break;
                        case ChatController.STATE_LISTEN:
                        case ChatController.STATE_NONE:
                            RelativeLayout rlH = (RelativeLayout) findViewById(R.id.home);
                            RelativeLayout rlC = (RelativeLayout) findViewById(R.id.connections);
                            RelativeLayout rlB = (RelativeLayout) findViewById(R.id.bracelet);

                            setStatus("Not connected");
                            changeButton(btnConnect, "#009885", true, "Connect");

                            ((TextView) findViewById(R.id.t2)).setText("is not connected");
                            ((TextView) findViewById(R.id.t3)).setText("is not connected");

                            changeButton(btnBracelet, "#ff4081", false, "N/A");
                            changeButton(((Button) findViewById(R.id.angry_btn1)), "#ff4081", false, "N/A");

                            pdI.withName(R.string.drawer_item_custom).withIcon(FontAwesome.Icon.faw_wifi).setEnabled(false);

                            rlC.setVisibility(View.GONE);
                            rlB.setVisibility(View.GONE);
                            rlH.setVisibility(View.VISIBLE);

                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    chatAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_READ:
                    synchronized (msg) {
                        try {
                            TransferMessage tMsg = SerializationUtils.deserialize((byte[]) msg.obj);
                            callRotator(tMsg.getReponseStatus());
                            callbacksFromDevice.add(tMsg);
                            callbacksFromDeviceAdapter.add(tMsg.toString());
                            manipulateRead(tMsg);
                            chatAdapter.notifyDataSetChanged();
                        } catch (Exception exe) {
                            Log.w("ENTRY", exe.getMessage());
                        }
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

    private void callRotator(Response reponseStatus) {
        switch (reponseStatus) {
            case GLU:
                rotators.get(0).setImageResource(R.drawable.loader);
                //GraphicsProcessing.RotateAnimation(rotators.get(0), 1500);
                break;
            case ERY:
                rotators.get(1).setImageResource(R.drawable.loader);
                //GraphicsProcessing.RotateAnimation(rotators.get(1), 1500);
                break;
            case LEU:
                rotators.get(2).setImageResource(R.drawable.loader);
                //GraphicsProcessing.RotateAnimation(rotators.get(2), 1500);
                break;
            case HEM:
                rotators.get(3).setImageResource(R.drawable.loader);
                //GraphicsProcessing.RotateAnimation(rotators.get(3), 1500);
                break;
        }
    }

    private void changeButton(Button btn, String color, boolean enabled, String text) {
        btn.setBackgroundColor(Color.parseColor(color));
        btn.setText(text);
        btn.setEnabled(enabled);
    }

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

        Button btn = (Button) findViewById(R.id.angry_btn1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(new CommandTransfer("Searching for you", new Date(), Response.BRACELET_VIBRATION));
            }
        });
    }

    private synchronized void manipulateRead(TransferMessage tMsg) throws IOException {
        DateFormat df1 = new SimpleDateFormat("MM/dd/yyyy");
        DateFormat df2 = new SimpleDateFormat("HH:mm:ss");
        Log.w("ENTRY", tMsg.toString());
        switch(tMsg.getReponseStatus()) {
            case ERY:
                writeMyData(ery, tMsg.getMessage());

                TextView eryDate = (TextView) findViewById(R.id.eryDate);
                eryDate.setText(df1.format(tMsg.getDateTime()));
                TextView eryTime = (TextView) findViewById(R.id.eryTime);
                eryTime.setText(df2.format(tMsg.getDateTime()));

                break;
            case LEU:
                writeMyData(lei, tMsg.getMessage());

                TextView leuDate = (TextView) findViewById(R.id.leuDate);
                leuDate.setText(df1.format(tMsg.getDateTime()));
                TextView leuTime = (TextView) findViewById(R.id.leuTime);
                leuTime.setText(df2.format(tMsg.getDateTime()));

                break;
            case HEM:
                writeMyData(hem, tMsg.getMessage());

                TextView hemDate = (TextView) findViewById(R.id.hemDate);
                hemDate.setText(df1.format(tMsg.getDateTime()));
                TextView hemTime = (TextView) findViewById(R.id.hemTime);
                hemTime.setText(df2.format(tMsg.getDateTime()));

                break;
            case GLU:
                Log.w("ww", tMsg.getMessage());
                writeMyData(glu, tMsg.getMessage());

                TextView gluDate = (TextView) findViewById(R.id.gluDate);
                gluDate.setText(df1.format(tMsg.getDateTime()));
                TextView gluTime = (TextView) findViewById(R.id.gluTime);
                gluTime.setText(df2.format(tMsg.getDateTime()));

                break;
            case AFC:
                dialog.show();
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
            case CURSORS:
                sendMyCursors();
                break;
            case BRACELET_CONNECTED:
                changeButton(btnBracelet, "#1D8508", false, "Connected");
                changeButton(((Button) findViewById(R.id.angry_btn1)), "#1D8508", true, "Vibrate");

                pdI.withName(R.string.drawer_item_custom).withIcon(FontAwesome.Icon.faw_wifi).setEnabled(true);

                TextView twC = (TextView) findViewById(R.id.bracelettext);
                twC.setText(tMsg.getMessage());
                ((TextView) findViewById(R.id.t3)).setText("is connected");
                break;
            case BRACELET_LISTENHEARTRATE:
                RotateLoading rl = (RotateLoading) findViewById(R.id.rotateloading);
                rl.stop();
                Log.w("BLHR", tMsg.getMessage());

                TextView tx = (TextView) findViewById(R.id.trackertextb);

                if(tMsg.getMessage().equals("0")) {
                    tx.setText('X');
                } else {
                    tx.setText(tMsg.getMessage());
                }

                FrameLayout fl = (FrameLayout) findViewById(R.id.hrcover);
                fl.setVisibility(View.VISIBLE);
                break;
            case BRACELET_BATTERY:
                RotateLoading rl2 = (RotateLoading) findViewById(R.id.rotateloadingb);
                rl2.stop();
                Log.w("BBHR", tMsg.getMessage());

                CircleProgress cp = (CircleProgress) findViewById(R.id.circle_progress);

                cp.setProgress(Integer.valueOf(tMsg.getMessage(), 10));

                CircleProgress fl2 = (CircleProgress) findViewById(R.id.circle_progress);
                fl2.setVisibility(View.VISIBLE);
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
            displayData(bp, value, true, R.drawable.tick);
            bp.write(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayData(BloodParts bp, Long value, boolean changeImage, int drawable) throws IOException {
        if(bp.getClass().equals(ery.getClass())) {
            TextView eryText = (TextView) findViewById(R.id.eryText);
            eryText.setText(String.valueOf(value) + " per liter");
            if(changeImage) {
                GifImageView r1 = rotators.get(1);
                r1.setImageResource(drawable);
            }
        } else if(bp.getClass().equals(lei.getClass())) {
                TextView leuText = (TextView) findViewById(R.id.leuText);
                leuText.setText(String.valueOf(value) + " per liter");
                if(changeImage) {
                    GifImageView r1 = rotators.get(2);
                    r1.setImageResource(drawable);
                }
            } else if(bp.getClass().equals(hem.getClass())) {
                    TextView hemText = (TextView) findViewById(R.id.hemText);
                    hemText.setText(String.valueOf(value) + " per liter");
                    if(changeImage) {
                        GifImageView r1 = rotators.get(3);
                        r1.setImageResource(drawable);
                    }
                } else if(bp.getClass().equals(glu.getClass())) {
                        TextView gluText = (TextView) findViewById(R.id.gluText);
                        gluText.setText(new DecimalFormat("#0.00").format(((value / 1000) / 113.12)) + " mmoles/l");
                        if(changeImage) {
                            GifImageView r1 = rotators.get(0);
                            r1.setImageResource(drawable);
                        }
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
        dialogBd = new Dialog(this);
        dialogBd.setContentView(R.layout.layout_bluetooth);
        dialogBd.setTitle("Bluetooth Devices");

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(this, R.layout.custom_txtview);
        discoveredDevicesAdapter = new ArrayAdapter<>(this, R.layout.custom_txtview);

        ListView listView = (ListView) dialogBd.findViewById(R.id.pairedDeviceList);
        ListView listView2 = (ListView) dialogBd.findViewById(R.id.discoveredDeviceList);
        listView.setAdapter(pairedDevicesAdapter);
        listView2.setAdapter(discoveredDevicesAdapter);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryFinishReceiver, filter);

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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);
                connectToDevice(address);
                dialogBd.dismiss();
            }

        });

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);
                connectToDevice(address);
                dialogBd.dismiss();
            }
        });

        dialogBd.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogBd.dismiss();
            }
        });
        dialogBd.setCancelable(false);
        dialogBd.show();
    }

    private void setStatus(String s) {
       //.setText(s);
    }

    private void connectToDevice(String deviceAddress) {
        bluetoothAdapter.cancelDiscovery();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        chatController.connect(device);
    }

    private void findViewsByIds() {
        this.rotators = Arrays.asList((GifImageView) findViewById(R.id.gluRotating),
                (GifImageView) findViewById(R.id.eryRotating),
                (GifImageView) findViewById(R.id.leuRotating),
                (GifImageView) findViewById(R.id.hemRotating));

        this.heart = (ImageView) findViewById(R.id.imageView7);
        btnConnect = (Button) findViewById(R.id.button);
        btnBracelet = (Button) findViewById(R.id.angry_btn);
        btnHr = (Button) findViewById(R.id.buttonb);
        btnBat = (Button) findViewById(R.id.angry_btnb);
		//Button scan = (Button) findViewById(R.id.btn_send_msg_to_scan);

        btnBracelet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(new CommandTransfer("D3:44:ED:E1:1F:9A", new Date(), Response.BRACELET_MAC));
            }
        });

        btnHr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(new CommandTransfer("HS", new Date(), Response.BRACELET_STARTHEARTRATE));
                FrameLayout fl = (FrameLayout) findViewById(R.id.hrcover);
                fl.setVisibility(View.GONE);
                RotateLoading rl = (RotateLoading) findViewById(R.id.rotateloading);
                rl.start();
            }
        });

        btnBat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(new CommandTransfer("BAT", new Date(), Response.BRACELET_BATTERY));
                CircleProgress fl = (CircleProgress) findViewById(R.id.circle_progress);
                fl.setVisibility(View.GONE);
                RotateLoading rl = (RotateLoading) findViewById(R.id.rotateloadingb);
                rl.start();
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
            return;
        }

        if (message != null) {
            byte[] data = SerializationUtils.serialize(message);
            chatController.write(data);
        }
    }

    void sendMyCursors(){
        runOnUiThread(new Runnable() {
            public void run() {
                sendMessage(new CommandTransfer(
                        String.valueOf(ery.getDataLen()) + ":" +
                        String.valueOf(glu.getDataLen()) + ":" +
                                String.valueOf(hem.getDataLen()) + ":" +
                                String.valueOf(lei.getDataLen()),
                        new Date(), Response.CURSORS));
            }
        });
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