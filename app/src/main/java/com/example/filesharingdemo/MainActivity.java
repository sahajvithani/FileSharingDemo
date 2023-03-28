package com.example.filesharingdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void send(View view) {
        startActivity(new Intent(MainActivity.this,SenderActivity.class));
    }

    public void receive(View view) {
        startActivity(new Intent(MainActivity.this,ReceiverActivity.class));
    }
}