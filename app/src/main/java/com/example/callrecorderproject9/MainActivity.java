package com.example.callrecorderproject9;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> permissions;
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    int accessStorage = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE );
                    int accessContact = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_CONTACTS);
                    int accessCall = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CALL_PHONE );
                    int accessAudio = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.RECORD_AUDIO );

                    permissions = new ArrayList();

                    if (accessStorage == PackageManager.PERMISSION_DENIED) {
                        permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                    if (accessContact == PackageManager.PERMISSION_DENIED) {
                        permissions.add(android.Manifest.permission.READ_CONTACTS);
                    }
                    if (accessCall == PackageManager.PERMISSION_DENIED) {
                        permissions.add(android.Manifest.permission.CALL_PHONE);
                    }
                    if (accessAudio == PackageManager.PERMISSION_DENIED) {
                        permissions.add(Manifest.permission.RECORD_AUDIO);
                    }

                    if(permissions.size() > 0) {
                        ActivityCompat.requestPermissions(MainActivity.this, permissions.toArray(new String[permissions.size()]), 1);
                    }else{

                        Toast.makeText(MainActivity.this,"Call Recorder Started",Toast.LENGTH_LONG).show();
                    }
                }

                Intent serviceIntent = new Intent(MainActivity.this, CallRecorder.class);
                startService(serviceIntent);
            }
        });
    }
}