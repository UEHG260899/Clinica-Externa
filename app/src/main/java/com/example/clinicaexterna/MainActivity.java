package com.example.clinicaexterna;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clinicaexterna.mdbf.Recepcionista;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnLogin;
    EditText etEmail, etPass;
    TextView tvRegistro;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicializarComponentes();
        iniciarFirebase();

    }


    private void iniciarFirebase(){
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Recepcionistas");
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
                String email = etEmail.getText().toString();
                String pass = etPass.getText().toString();
                if(email.isEmpty() || pass.isEmpty()){
                    etEmail.setError("Este campo es obligatorio");
                    etPass.setError("Este campo es obligatorio");
                }else{
                    firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                databaseReference.orderByChild("correo").equalTo(email).addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                        Recepcionista recep = snapshot.getValue(Recepcionista.class);
                                        Intent intent = new Intent(MainActivity.this, DrawerClinica.class);
                                        intent.putExtra("nombre", recep.getNombre());
                                        intent.putExtra("correo", recep.getCorreo());
                                        startActivity(intent);
                                        finish();
                                    }

                                    @Override
                                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                    }

                                    @Override
                                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                                    }

                                    @Override
                                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }else{
                                try {
                                    throw task.getException();
                                }catch(FirebaseAuthInvalidUserException ex){
                                    etEmail.requestFocus();
                                    etEmail.setError("El usuario no esta registrado");
                                }catch(FirebaseAuthInvalidCredentialsException ex){
                                    etPass.requestFocus();
                                    etPass.setError("La contraseña no coincide con el registro");
                                }catch(Exception ex){
                                    Toast.makeText(MainActivity.this, "Ocurrio un error desconocido", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
                break;
            }
            case R.id.tvRegistro: {
                startActivity(new Intent(MainActivity.this, RegistroActivity.class));
                break;
            }

        }
    }
}