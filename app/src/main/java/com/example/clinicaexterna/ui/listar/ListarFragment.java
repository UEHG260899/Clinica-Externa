package com.example.clinicaexterna.ui.listar;

import android.app.AlertDialog;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.clinicaexterna.R;
import com.example.clinicaexterna.mdbf.Paciente;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ListarFragment extends Fragment {

    private ListarViewModel listarViewModel;
    ListView lvPacientes;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseUser user;
    StorageReference storageReference;
    Paciente pacienteSelected;

    private List<Paciente> listaPaciente = new ArrayList<>();
    ArrayAdapter<Paciente> arrayAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        listarViewModel =
                new ViewModelProvider(this).get(ListarViewModel.class);
        View root = inflater.inflate(R.layout.fragment_listar, container, false);
        final TextView textView = root.findViewById(R.id.text_slideshow);
        listarViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        iniciarFirebase();
        iniciarComponentes(root);
        listarDatos();

        lvPacientes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pacienteSelected = (Paciente) parent.getItemAtPosition(position);
                View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_paciente, null);
                ((TextView) dialogView.findViewById(R.id.tvInfoPaciente)).setText("ID: " + pacienteSelected.getId() +"\n" +
                        "Nombre: " + pacienteSelected.getNombre() + "\n" +
                        "Fecha: " + pacienteSelected.getFecha() + "\n" +
                        "Edad: " + pacienteSelected.getEdad() + "\n" +
                        "Sexo: " + pacienteSelected.getSexo() + "\n" +
                        "Peso: " + pacienteSelected.getPeso() + "\n" +
                        "Estatura: " + pacienteSelected.getEstatura() + "\n" +
                        "Doctor: " + pacienteSelected.getDoctor() + "\n" +
                        "Área: " + pacienteSelected.getArea() + "\n");
                ImageView ivImagen = dialogView.findViewById(R.id.ivFotoDial);
                System.out.println(storageReference);
                storageReference.child(pacienteSelected.getImg()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(getContext()).load(uri).into(ivImagen);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getCause() + "", Toast.LENGTH_LONG).show();
                    }
                });
                AlertDialog.Builder dialogo = new AlertDialog.Builder(getContext());
                dialogo.setTitle("Información del paciente");
                dialogo.setView(dialogView);
                dialogo.setPositiveButton("Aceptar", null);
                dialogo.show();
            }
        });

        return root;
    }


    private void iniciarFirebase(){
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Pacientes");
        storageReference = FirebaseStorage.getInstance().getReference("imagenes");
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void iniciarComponentes(View root){
        lvPacientes = root.findViewById(R.id.lvPacientes);
    }

    private void listarDatos(){
            databaseReference.orderByChild("recep").equalTo(user.getEmail()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    listaPaciente.clear();
                    for(DataSnapshot objSnapshot : snapshot.getChildren()){
                        Paciente p = objSnapshot.getValue(Paciente.class);
                        listaPaciente.add(p);
                        if(getContext() != null){
                            arrayAdapter = new ArrayAdapter<Paciente>(getContext(), android.R.layout.simple_list_item_1, listaPaciente);
                            lvPacientes.setAdapter(arrayAdapter);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }
}