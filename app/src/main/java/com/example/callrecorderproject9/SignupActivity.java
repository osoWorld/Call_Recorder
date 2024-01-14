package com.example.callrecorderproject9;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.callrecorderproject9.databinding.ActivitySignupBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    private String name, email, password;
    private FirebaseAuth auth;
    private FirebaseFirestore fireStore;
    private String userid;
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(SignupActivity.this, MainActivity.class));
            finish();
        }

        binding.LoginAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            }
        });

        binding.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name = binding.SignupName.getText().toString().trim();
                email = binding.SignupEmail.getText().toString().trim();
                password = binding.SignupPassword.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Field's can't be empty", Toast.LENGTH_SHORT).show();
                } else if (!email.contains("@") && !email.contains("com")) {
                    Toast.makeText(SignupActivity.this, "Invalid Email", Toast.LENGTH_SHORT).show();
                } else {
                    signUpWithFirebase(email, password,name);
                }
            }
        });
    }

    private void signUpWithFirebase(String email, String password,String name) {
        binding.progressBarSignup.setVisibility(View.VISIBLE);
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    user = auth.getCurrentUser();
                    if (user != null){
                        userid = user.getUid();
                        saveUserDataToFirebase(name,email,"",userid);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignupActivity.this, "Auth Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                binding.progressBarSignup.setVisibility(View.GONE);
            }
        });
    }

    private void saveUserDataToFirebase(String username,String useremail,String phone ,String userid) {
        DocumentReference userDocument = fireStore.collection("Users").document(userid);

        RegistrationModel register = new RegistrationModel(
                username,
                useremail,
                phone,
                userid

        );

        userDocument.set(register).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(SignupActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                binding.progressBarSignup.setVisibility(View.GONE);
                startActivity(new Intent(SignupActivity.this, MainActivity.class).putExtra("UserId", userid));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignupActivity.this, "FireStore : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                binding.progressBarSignup.setVisibility(View.GONE);
            }
        });
    }
}