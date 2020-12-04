package com.example.clinicaexterna.ui.listar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.clinicaexterna.R;
import com.example.clinicaexterna.mdbf.Paciente;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListarFragment extends Fragment {

    private ListarViewModel listarViewModel;
    ListView lvPacientes;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseUser user;

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

        return root;
    }


    private void iniciarFirebase(){
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Pacientes");
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