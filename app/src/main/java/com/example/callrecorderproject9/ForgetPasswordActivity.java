package com.example.callrecorderproject9;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.callrecorderproject9.databinding.ActivityForgetPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends AppCompatActivity {
    private ActivityForgetPasswordBinding binding;
    private String email;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = binding.resetPasswordET.getText().toString().trim();

                if (email.isEmpty()){
                    Toast.makeText(ForgetPasswordActivity.this, "Field's can't be empty", Toast.LENGTH_SHORT).show();
                } else if (!email.contains("@") && !email.contains(".com")) {
                    Toast.makeText(ForgetPasswordActivity.this, "Invalid Email", Toast.LENGTH_SHORT).show();
                }else {
                    resetPassword(email);
                }

            }
        });
    }
    private void resetPassword(String email) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(ForgetPasswordActivity.this, "Open your Email", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ForgetPasswordActivity.this, "Reset : "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}