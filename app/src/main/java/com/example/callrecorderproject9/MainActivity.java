package com.example.callrecorderproject9;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.callrecorderproject9.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> permissions;
    private ActivityMainBinding binding;
    private String phoneNumber;
    private FirebaseFirestore fireStore;
    private String userId;
    private FirebaseAuth auth;
    private String name, email;
    private RegistrationModel user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fireStore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        userId = auth.getCurrentUser().getUid();
        if (userId != null) {
            retrieveUserDataFromFireStore(userId);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int accessInternet = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.INTERNET);
            int accessStorage = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE );
            int accessContact = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS);
            int accessCall = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE );
            int accessAudio = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO );

            permissions = new ArrayList();

            if (accessInternet == PackageManager.PERMISSION_DENIED) {
                permissions.add(android.Manifest.permission.INTERNET);
            }
            if (accessStorage == PackageManager.PERMISSION_DENIED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (accessContact == PackageManager.PERMISSION_DENIED) {
                permissions.add(Manifest.permission.READ_CONTACTS);
            }
            if (accessCall == PackageManager.PERMISSION_DENIED) {
                permissions.add(Manifest.permission.CALL_PHONE);
            }
            if (accessAudio == PackageManager.PERMISSION_DENIED) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }

            if (permissions.size() > 0) {
                showPermissionExplanationDialog();
            } else {
                startCallRecorderService();
            }
        }

        binding.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                startActivity(new Intent(MainActivity.this, SignupActivity.class));
            }
        });
        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = binding.phoneNumberET.getText().toString().trim();
                if (phoneNumber.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Field's can't be empty", Toast.LENGTH_SHORT).show();
                } else {
                    saveNumberToFireStore(phoneNumber);
                }
            }
        });

        binding.monitoringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Start Monitoring", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPermissionExplanationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Required");
        builder.setMessage("This app needs some permission to keep you protected against scammers. Please allow all the permission that are necessary to monitor the calls. We assure you, your data is 100% private and securely stored with us. None of it will ever be used at any other purpose other than protecting you against scammers. By clicking on \"Agree\" below, you acknowledge that you read and understood everything in this message, and are sharing your data willingly.");
        builder.setPositiveButton("Agree", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this, permissions.toArray(new String[permissions.size()]), 1);
    }

    private void retrieveUserDataFromFireStore(String userid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(userid);

        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            user = documentSnapshot.toObject(RegistrationModel.class);

                            if (user != null) {
                                name = user.getName();
                                email = user.getEmail();
                                phoneNumber = user.getPhoneNumber();

                                if (phoneNumber != null) {
                                    binding.phoneNumberET.setHint(phoneNumber);
                                } else {
                                    binding.phoneNumberET.setHint("Phone Number");
                                }
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Data does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "FireStore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveNumberToFireStore(String phoneNumber) {
        if (userId != null && user != null) {

            String name = user.getName();
            String email = user.getEmail();

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("email", email);
            updates.put("phoneNumber", phoneNumber);
            updates.put("userID", userId);

            fireStore.collection("Users").document(userId).set(updates);
            Toast.makeText(this, "Number Saved", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCallRecorderService() {
        Intent serviceIntent = new Intent(MainActivity.this, CallRecorder.class);
        startService(serviceIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            boolean allPermissionsGranted = true;

            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                startCallRecorderService();
            } else {

            }
        }
    }
}