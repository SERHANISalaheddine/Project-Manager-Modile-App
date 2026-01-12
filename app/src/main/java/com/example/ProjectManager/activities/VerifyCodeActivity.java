package com.example.ProjectManager.activities;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ProjectManager.R;
import com.example.ProjectManager.api.ApiService;
import com.example.ProjectManager.api.RetrofitClient;
import com.example.ProjectManager.models.dto.AuthResponseDto;
import com.example.ProjectManager.models.dto.LoginRequestDto;
import com.example.ProjectManager.utils.SharedPrefsManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyCodeActivity extends AppCompatActivity {

    EditText otp1, otp2, otp3, otp4, otp5, otp6;
    Button btnSubmit;
    TextView txtResend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);
        btnSubmit = findViewById(R.id.btnSubmit);
        txtResend = findViewById(R.id.txtResend);

        btnSubmit.setOnClickListener(v -> {
            String code = otp1.getText().toString() +
                    otp2.getText().toString() +
                    otp3.getText().toString() +
                    otp4.getText().toString() +
                    otp5.getText().toString() +
                    otp6.getText().toString();

            if (code.length() < 6) {
                Toast.makeText(this, "Please enter full verification code", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Code verified successfully", Toast.LENGTH_SHORT).show();
            // Aller vers NewPasswordActivity
            Intent intent = new Intent(VerifyCodeActivity.this, NewPasswordActivity.class);
            startActivity(intent);
        });

        txtResend.setOnClickListener(v -> {
            Toast.makeText(this, "Verification code resent", Toast.LENGTH_SHORT).show();
        });
    }
}
