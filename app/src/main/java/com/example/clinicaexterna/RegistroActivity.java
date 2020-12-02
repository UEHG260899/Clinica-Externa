package com.example.clinicaexterna;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.clinicaexterna.mdbf.Recepcionista;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class RegistroActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnRegistro;
    EditText etEmail, etPass, etNombre;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        inicializarComponentes();
        inicializarFirebase();
    }


    private void inicializarComponentes(){
        btnRegistro = findViewById(R.id.btnRegistrarse);
        etEmail = findViewById(R.id.etEmailR);
        etPass = findViewById(R.id.etPassR);
        etNombre = findViewById(R.id.etNomR);

        btnRegistro.setOnClickListener(this);
    }

    private void inicializarFirebase(){
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnRegistrarse: {
                if(etNombre.getText().toString().equals("") || etEmail.getText().toString().equals("") || etPass.getText().toString().equals("")){
                    validacion();
                }else{
                    firebaseAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPass.getText().toString()).addOnCompleteListener(RegistroActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Recepcionista recepcionista = new Recepcionista();
                                recepcionista.setNombre(etNombre.getText().toString());
                                recepcionista.setCorreo(etEmail.getText().toString());
                                recepcionista.setPassword(etPass.getText().toString());
                                recepcionista.setUuid(UUID.randomUUID().toString());
                                databaseReference.child("Recepcionistas").child(recepcionista.getUuid()).setValue(recepcionista);
                                Intent intent = new Intent(RegistroActivity.this, DrawerClinica.class);
                                intent.putExtra("nombre", recepcionista.getNombre());
                                intent.putExtra("correo", recepcionista.getCorreo());
                                startActivity(intent);
                            }else{
                                try {
                                    throw task.getException();
                                }catch (FirebaseAuthWeakPasswordException ex){
                                    etPass.requestFocus();
                                    etPass.setError("La contraseña es demasiado corta, debe contener minimo 6 caracteres");
                                }catch(FirebaseAuthUserCollisionException ex){
                                    etEmail.requestFocus();
                                    etEmail.setError("Este usuario ya esta registrado");
                                }catch (Exception ex){
                                    Toast.makeText(RegistroActivity.this, "Ha ocurrido un error desconocido", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
                break;
            }
        }
    }


    private void validacion(){

        String nombre = etNombre.getText().toString();
        String correo = etEmail.getText().toString();
        String pass = etPass.getText().toString();

        if(nombre.isEmpty()){
            etNombre.setError("Campo obligatorio");
        }else if(correo.isEmpty()){
            etEmail.setError("Campo obligatorio");
        }else if(pass.isEmpty()){
            etPass.setError("Campo obligatorio");
        }
    }
}