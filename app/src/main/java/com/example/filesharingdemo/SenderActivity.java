package com.example.filesharingdemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class SenderActivity extends AppCompatActivity {

    private static final String TAG = "shareFile";
    TextView tv_infoIp, tv_infoPort;
    EditText etPath;
    Button btnNext;

    static final int SocketServerPORT = 8080;
    ServerSocket serverSocket;

    ServerSocketThread serverSocketThread;


    private WifiManager wifiManager;
    WifiConfiguration currentConfig;
    WifiManager.LocalOnlyHotspotReservation hotspotReservation;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);

        tv_infoIp = (TextView) findViewById(R.id.tv_infoIp);
        tv_infoPort = (TextView) findViewById(R.id.tv_infoPort);
        etPath = (EditText) findViewById(R.id.et_setPath);
        btnNext = (Button) findViewById(R.id.btn_next);

        tv_infoIp.setText(getIpAddress());

        serverSocketThread = new ServerSocketThread();
        serverSocketThread.start();

        btnNext.setOnClickListener(view -> {
            startActivity(new Intent(SenderActivity.this, ReceiverActivity.class));
        });
//        turnOnHotspot();
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void turnOnHotspot() {

        wifiManager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                hotspotReservation = reservation;
                currentConfig = hotspotReservation.getWifiConfiguration();

                Log.v("DANG", "THE PASSWORD IS: "
                        + currentConfig.preSharedKey
                        + " \n SSID is : "
                        + currentConfig.SSID);

                hotspotDetailsDialog();

            }

            @Override
            public void onStopped() {
                super.onStopped();
                Log.v("DANG", "Local Hotspot Stopped");
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                Log.v("DANG", "Local Hotspot failed to start");
            }
        }, new Handler());

    }

    private void hotspotDetailsDialog() {

        Log.v(TAG, SenderActivity.this.getString(R.string.hotspot_details_message) + "\n" + SenderActivity.this.getString(
                R.string.hotspot_ssid_label) + " " + currentConfig.SSID + "\n" + SenderActivity.this.getString(
                R.string.hotspot_pass_label) + " " + currentConfig.preSharedKey);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }
                }
            }
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

public class ServerSocketThread extends Thread {

    @Override
    public void run() {
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(SocketServerPORT);
            SenderActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    tv_infoPort.setText("I'm waiting here: "
                            + serverSocket.getLocalPort());
                }
            });

            while (true) {
                socket = serverSocket.accept();
                FileTxThread fileTxThread = new FileTxThread(socket);
                fileTxThread.start();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}

public class FileTxThread extends Thread {
    Socket socket;

    FileTxThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        File file = new File(
                Environment.getExternalStorageDirectory(), "/" + etPath.getText().toString());

        byte[] bytes = new byte[(int) file.length()];
        BufferedInputStream bis;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            bis.read(bytes, 0, bytes.length);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(bytes);
            oos.flush();

            socket.close();

            final String sentMsg = "File sent to: " + socket.getInetAddress();
            SenderActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(SenderActivity.this,
                            sentMsg,
                            Toast.LENGTH_LONG).show();
                }
            });
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
}