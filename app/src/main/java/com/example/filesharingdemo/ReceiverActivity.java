package com.example.filesharingdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ReceiverActivity extends AppCompatActivity {

    private static final String TAG = "fileSharingAppDemo";
    EditText editTextAddress, etName;
    Button buttonConnect;
    TextView textPort;
    ProgressBar progressBar;

    static final int SocketServerPORT = 8080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);

        editTextAddress = (EditText) findViewById(R.id.et_address);
        etName = (EditText) findViewById(R.id.et_getPath);
        textPort = (TextView) findViewById(R.id.port);
        textPort.setText("port: " + SocketServerPORT);
        buttonConnect = (Button) findViewById(R.id.connect);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        File file = new File(Environment.getExternalStorageDirectory(),"FileSharing");

        if (!file.exists()){
            file.mkdir();
            Toast.makeText(ReceiverActivity.this,"Successful",Toast.LENGTH_SHORT).show();
        }else
        {
            Toast.makeText(ReceiverActivity.this,"Folder Already Exists",Toast.LENGTH_SHORT).show();
        }

        buttonConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ClientRxThread clientRxThread =
                        new ClientRxThread(
                                editTextAddress.getText().toString(),
                                SocketServerPORT);

                clientRxThread.start();
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private class ClientRxThread extends Thread {
        String dstAddress;
        int dstPort;

        ClientRxThread(String address, int port) {
            dstAddress = address;
            dstPort = port;
        }

        @Override
        public void run() {
            Socket socket = null;

            try {
                socket = new Socket(dstAddress, dstPort);


                File file = new File(Environment.getExternalStorageDirectory(), "/FileSharing/" + etName.getText().toString());

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                byte[] bytes;
                FileOutputStream fos = null;
                try {
                    bytes = (byte[]) ois.readObject();
                    fos = new FileOutputStream(file);
                    fos.write(bytes);
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        fos.close();
                    }

                }

                socket.close();

                ReceiverActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ReceiverActivity.this,
                                "Finished",
                                Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });

            } catch (IOException e) {

                e.printStackTrace();

                final String eMsg = "Something wrong: " + e.getMessage();
                ReceiverActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ReceiverActivity.this,
                                eMsg,
                                Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });

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
}