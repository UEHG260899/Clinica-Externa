package com.example.clinicaexterna.ui.eliminar;

import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.PersistableBundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.clinicaexterna.R;
import com.example.clinicaexterna.mdbf.Paciente;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EliminarFragment extends Fragment implements View.OnClickListener {

    private Button btnElimina, btnLimpia, btnBusca;
    private TextView tvNombre, tvArea, tvDoctor, tvGenero, tvfecha, tvEdad, tvEstatura, tvPeso;
    private ImageView ivFoto;
    private EditText etID;
    Paciente pacienteSelected;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;

    private EliminarViewModel mViewModel;

    public static EliminarFragment newInstance() {
        return new EliminarFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_eliminar, container, false);

        inicializaComponentes(root);
        inicializaFirebase();
        return root;
    }

    private void inicializaComponentes(View root) {
        compText(root);
        compBotones(root);
    }

    private void compText(View root) {
        tvArea = root.findViewById(R.id.tvArea);
        tvDoctor = root.findViewById(R.id.tvDoctor);
        tvEdad = root.findViewById(R.id.tvEdad);
        tvEstatura = root.findViewById(R.id.tvEst);
        tvPeso = root.findViewById(R.id.tvPeso);
        tvNombre = root.findViewById(R.id.tvNombre);
        tvGenero = root.findViewById(R.id.tvGenero);
        tvfecha = root.findViewById(R.id.tvFecha);
        etID = root.findViewById(R.id.etIDEl);
    }

    private void compBotones(View root) {
        btnBusca = root.findViewById(R.id.btnBuscaEl);
        btnElimina = root.findViewById(R.id.btnEliminar);
        btnLimpia = root.findViewById(R.id.btnLimparEl);
        ivFoto = root.findViewById(R.id.ivFotoEl);

        btnLimpia.setEnabled(false);
        btnElimina.setEnabled(false);

        btnBusca.setOnClickListener(this);
        btnElimina.setOnClickListener(this);
        btnLimpia.setOnClickListener(this);
    }

    private void inicializaFirebase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Pacientes");
        storageReference = FirebaseStorage.getInstance().getReference("imagenes");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EliminarViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBuscaEl: {

                if (etID.getText().toString().isEmpty()) {
                    etID.requestFocus();
                    etID.setError("Por favor, ingrese un criterio de busqueda");
                } else {
                    Query query = databaseReference.orderByChild("id").equalTo(etID.getText().toString());
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                btnElimina.setEnabled(true);
                                btnLimpia.setEnabled(true);
                                for (DataSnapshot objSnapshot : snapshot.getChildren()) {
                                    pacienteSelected = objSnapshot.getValue(Paciente.class);
                                    tvNombre.setText("Nombre del paciente: " + pacienteSelected.getNombre());
                                    tvfecha.setText("Fecha de ingreso: " + pacienteSelected.getFecha());
                                    tvEdad.setText("Edad: " + pacienteSelected.getEdad());
                                    tvEstatura.setText("Estatura: " + pacienteSelected.getEstatura());
                                    tvPeso.setText("Peso: " + pacienteSelected.getPeso());
                                    tvGenero.setText("Género: " + pacienteSelected.getSexo());
                                    tvDoctor.setText("Doctor: " + pacienteSelected.getDoctor());
                                    tvArea.setText("Área: " + pacienteSelected.getArea());
                                    cargaImagen(ivFoto);
                                }
                            } else {
                                tvNombre.setText("Nombre del paciente: ");
                                tvfecha.setText("Fecha de ingreso: ");
                                tvEdad.setText("Edad: ");
                                tvEstatura.setText("Estatura: ");
                                tvPeso.setText("Peso: ");
                                tvGenero.setText("Género: ");
                                tvDoctor.setText("Doctor: ");
                                tvArea.setText("Área: ");
                                ivFoto.setImageResource(R.drawable.ic_menu_camera);
                                btnLimpia.setEnabled(false);
                                btnElimina.setEnabled(false);
                                etID.setError("No hay resultados");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                break;
            }
            case R.id.btnLimparEl: {
                limpiar();
                break;
            }
            case R.id.btnEliminar: {
                View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_paciente, null);
                ((TextView) dialogView.findViewById(R.id.tvInfoPaciente)).setText("¿Desea eliminar el registro?\n" +
                        "ID: " + pacienteSelected.getId() + "\n" +
                        "Nombre: " + pacienteSelected.getNombre() + "\n" +
                        "Fecha: " + pacienteSelected.getFecha() + "\n" +
                        "Edad: " + pacienteSelected.getEdad() + "\n" +
                        "Sexo: " + pacienteSelected.getSexo() + "\n" +
                        "Peso: " + pacienteSelected.getPeso() + "\n" +
                        "Estatura: " + pacienteSelected.getEstatura() + "\n" +
                        "Doctor: " + pacienteSelected.getDoctor() + "\n" +
                        "Área: " + pacienteSelected.getArea() + "\n");
                ImageView imageView = dialogView.findViewById(R.id.ivFotoDial);
                cargaImagen(imageView);
                AlertDialog.Builder dialogo = new AlertDialog.Builder(getContext());
                dialogo.setTitle("Importante");
                dialogo.setView(dialogView);
                dialogo.setCancelable(false);
                dialogo.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        aceptar(pacienteSelected.getImg());
                        limpiar();
                    }
                });
                dialogo.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getContext(), "Registro aún activo", Toast.LENGTH_SHORT).show();
                    }
                });
                dialogo.show();
                break;
            }
        }
    }


    private void limpiar() {
        tvNombre.setText("Nombre del paciente: ");
        tvfecha.setText("Fecha de ingreso: ");
        tvEdad.setText("Edad: ");
        tvEstatura.setText("Estatura: ");
        tvPeso.setText("Peso: ");
        tvGenero.setText("Género: ");
        tvDoctor.setText("Doctor: ");
        tvArea.setText("Área: ");
        ivFoto.setImageResource(R.drawable.ic_menu_camera);
        etID.setText("");
        btnLimpia.setEnabled(false);
        btnElimina.setEnabled(false);
    }

    private void aceptar(String imagen) {
        databaseReference.child(etID.getText().toString()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                storageReference.child(imagen).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Registro eliminado de forma satisfactoria", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void cargaImagen(ImageView imageView) {
        storageReference.child(pacienteSelected.getImg()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getContext()).load(uri).into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.getCause() + "", Toast.LENGTH_LONG).show();
            }
        });
    }
}