package com.example.clinicaexterna;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnLogin;
    EditText etEmail, etPass;
    TextView tvRegistro;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicializarComponentes();

    }

    private void inicializarComponentes(){
        btnLogin = findViewById(R.id.btnLogin);
        etEmail = findViewById(R.id.etCorreoL);
        etPass = findViewById(R.id.etEmailR);
        tvRegistro = findViewById(R.id.tvRegistro);

        btnLogin.setOnClickListener(this);
        tvRegistro.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.btnLogin: {

                break;
            }
            case R.id.tvRegistro: {
                startActivity(new Intent(MainActivity.this, RegistroActivity.class));
                break;
            }

        }
    }
}