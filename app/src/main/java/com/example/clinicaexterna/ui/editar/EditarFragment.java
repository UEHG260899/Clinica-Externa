package com.example.clinicaexterna.ui.editar;

import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.clinicaexterna.R;
import com.example.clinicaexterna.mdbf.Paciente;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EditarFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, DatePickerDialog.OnDateSetListener {

    private EditarViewModel mViewModel;
    private ImageView ivFoto;
    private EditText etNombre, etEdad, etFecha, etID, etPeso, etEstatura;
    private Spinner spArea, spDoc, spSexo;
    private Button btnLimpiar, btnEditar, btnBuscar;
    private ImageButton btnFecha;
    private ArrayAdapter<CharSequence> areaAdapter, sexoAdapter, docAdapter;
    private String img = "", imgF = "", sex, doc, ar;
    private int anio, mes, dia;
    Calendar c;
    DatePickerDialog dpd;
    StorageReference storageReference;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Paciente pacienteSelected;
    public static final int REQUEST_TAKE_PHOTO = 1;
    Uri photoUri;
    boolean bandera;


    public static EditarFragment newInstance() {
        return new EditarFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_editar, container, false);

        iniciaFirebase();
        iniciaComponentes(root);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EditarViewModel.class);
        // TODO: Use the ViewModel
    }


    private void iniciaComponentes(View root){
        compBotones(root);
        compEdit(root);
        comSpinn(root);
    }

    private void iniciaFirebase(){
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Pacientes");
        storageReference = FirebaseStorage.getInstance().getReference("imagenes");
    }

    private void compBotones(View root){
        btnBuscar = root.findViewById(R.id.btnBuscaEd);
        btnLimpiar = root.findViewById(R.id.btnLimpiaEd);
        btnEditar = root.findViewById(R.id.btnEditar);
        btnFecha = root.findViewById(R.id.btnCalendarioEd);
        ivFoto = root.findViewById(R.id.ivFotoEd);

        btnLimpiar.setEnabled(false);
        btnEditar.setEnabled(false);

        btnLimpiar.setOnClickListener(this);
        btnEditar.setOnClickListener(this);
        btnBuscar.setOnClickListener(this);
        btnFecha.setOnClickListener(this);
        ivFoto.setOnClickListener(this);
    }

    private void compEdit(View root){
        etNombre = root.findViewById(R.id.etNomPacEd);
        etEdad = root.findViewById(R.id.etEdadEd);
        etFecha = root.findViewById(R.id.etFechaEd);
        etID = root.findViewById(R.id.etIDEd);
        etPeso = root.findViewById(R.id.etPesoEd);
        etEstatura = root.findViewById(R.id.etEstEd);
    }

    private void comSpinn(View root){
        areaAdapter = ArrayAdapter.createFromResource(getContext(), R.array.opciones, android.R.layout.simple_spinner_item);
        docAdapter = ArrayAdapter.createFromResource(getContext(), R.array.o0, android.R.layout.simple_spinner_item);
        sexoAdapter = ArrayAdapter.createFromResource(getContext(), R.array.genero, android.R.layout.simple_spinner_item);

        spArea = root.findViewById(R.id.spAreaEd);
        spArea.setAdapter(areaAdapter);
        spDoc = root.findViewById(R.id.spDocEd);
        spDoc.setAdapter(docAdapter);
        spSexo = root.findViewById(R.id.spSexoEd);
        spSexo.setAdapter(sexoAdapter);

        spSexo.setOnItemSelectedListener(this);
        spArea.setOnItemSelectedListener(this);
        spDoc.setOnItemSelectedListener(this);
    }

    private void limpiar(){
        areaAdapter = ArrayAdapter.createFromResource(getContext(), R.array.opciones, android.R.layout.simple_spinner_item);
        docAdapter = ArrayAdapter.createFromResource(getContext(), R.array.o0, android.R.layout.simple_spinner_item);
        sexoAdapter = ArrayAdapter.createFromResource(getContext(), R.array.genero, android.R.layout.simple_spinner_item);

        spSexo.setAdapter(sexoAdapter);
        spDoc.setAdapter(docAdapter);
        spArea.setAdapter(areaAdapter);

        etNombre.setText("");
        etEdad.setText("");
        etFecha.setText("");
        etID.setText("");
        etPeso.setText("");
        etEstatura.setText("");

        ivFoto.setImageResource(R.drawable.ic_menu_camera);

        btnLimpiar.setEnabled(false);
        btnEditar.setEnabled(false);
        btnBuscar.setEnabled(true);

        img = imgF = sex = doc = ar = "";
        pacienteSelected = null;
        bandera = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLimpiaEd:
                limpiar();
                break;
            case R.id.btnBuscaEd:
                if(etID.getText().toString().isEmpty()){
                    etID.requestFocus();
                    etID.setError("Por favor ingrese un criterio de busqueda");
                }else{
                    bandera = true;
                    Query query = databaseReference.orderByChild("id").equalTo(etID.getText().toString());
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists() && bandera){
                                for(DataSnapshot objSnapshot : snapshot.getChildren()){
                                    pacienteSelected = objSnapshot.getValue(Paciente.class);
                                    etNombre.setText(pacienteSelected.getNombre());
                                    etEdad.setText(pacienteSelected.getEdad());
                                    etFecha.setText(pacienteSelected.getFecha());
                                    etPeso.setText(pacienteSelected.getPeso());
                                    etEstatura.setText(pacienteSelected.getEstatura());
                                    sex = pacienteSelected.getSexo();
                                    doc = pacienteSelected.getDoctor();
                                    ar = pacienteSelected.getArea();
                                    imgF = pacienteSelected.getImg();
                                }
                                cargaImagen(ivFoto);
                                etID.setEnabled(false);
                                btnBuscar.setEnabled(false);
                                btnLimpiar.setEnabled(true);
                                btnEditar.setEnabled(true);
                                spArea.setSelection(areaAdapter.getPosition(ar));
                                spSexo.setSelection(sexoAdapter.getPosition(sex));
                            }else{
                                if(getContext() != null){
                                    Toast.makeText(getContext(), "No se han encontrado resultados", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                break;
            case R.id.btnCalendarioEd:
                c = Calendar.getInstance();
                anio = c.get(Calendar.YEAR);
                mes =  c.get(Calendar.MONTH);
                dia = c.get(Calendar.DAY_OF_MONTH);
                dpd = new DatePickerDialog(getContext(), this, anio, mes, dia);
                dpd.show();
                break;
            case R.id.ivFotoEd:
                Intent tomaFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if(tomaFoto.resolveActivity(getActivity().getPackageManager()) != null){
                    startActivityForResult(tomaFoto, REQUEST_TAKE_PHOTO);
                }
                break;
            case R.id.btnEditar:
                if(etNombre.getText().toString().isEmpty() || etEdad.getText().toString().isEmpty() || etFecha.getText().toString().isEmpty() || etPeso.getText().toString().isEmpty() || etEstatura.getText().toString().isEmpty()
                        || sex.isEmpty() || doc.isEmpty() || ar.isEmpty()){
                    Toast.makeText(getContext(), "Hay campos vacios", Toast.LENGTH_SHORT).show();
                }else{
                    if(img.isEmpty()){
                        Paciente p = new Paciente();
                        p.setId(pacienteSelected.getId());
                        p.setNombre(etNombre.getText().toString());
                        p.setEstatura(etEstatura.getText().toString());
                        p.setEdad(etEdad.getText().toString());
                        p.setFecha(etFecha.getText().toString());
                        p.setRecep(pacienteSelected.getRecep());
                        p.setPeso(etPeso.getText().toString());
                        p.setDoctor(doc);
                        p.setArea(ar);
                        p.setSexo(sex);
                        p.setImg(imgF);
                        databaseReference.child(p.getId()).setValue(p).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), "Actualización exitosa", Toast.LENGTH_SHORT).show();
                                limpiar();
                            }
                        });
                    }else{
                        Paciente p = new Paciente();
                        p.setId(pacienteSelected.getId());
                        p.setNombre(etNombre.getText().toString());
                        p.setEstatura(etEstatura.getText().toString());
                        p.setEdad(etEdad.getText().toString());
                        p.setFecha(etFecha.getText().toString());
                        p.setRecep(pacienteSelected.getRecep());
                        p.setPeso(etPeso.getText().toString());
                        p.setDoctor(doc);
                        p.setArea(ar);
                        p.setSexo(sex);
                        p.setImg(img);
                        storageReference.child(imgF).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                limpiar();
                                databaseReference.child(p.getId()).setValue(p).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getContext(), "Actualización exitosa", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });

                    }
                }
                break;
        }
    }

    private void cargaImagen(ImageView ivFoto){
        if(getActivity() != null){
            storageReference.child(pacienteSelected.getImg()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getContext()).load(uri).into(ivFoto);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getCause() + "", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.spAreaEd:
                ArrayAdapter<CharSequence> adapter;

                switch (position){
                    case 1:
                        adapter = ArrayAdapter.createFromResource(getContext(), R.array.o1, android.R.layout.simple_spinner_item);
                        break;
                    case 2:
                        adapter = ArrayAdapter.createFromResource(getContext(), R.array.o2, android.R.layout.simple_spinner_item);
                        break;
                    case 3:
                        adapter = ArrayAdapter.createFromResource(getContext(), R.array.o3, android.R.layout.simple_spinner_item);
                        break;
                    case 4:
                        adapter = ArrayAdapter.createFromResource(getContext(), R.array.o4, android.R.layout.simple_spinner_item);
                        break;
                    default:
                        adapter = ArrayAdapter.createFromResource(getContext(), R.array.o0, android.R.layout.simple_spinner_item);
                        break;
                }

                if(position != 0){
                    ar = parent.getItemAtPosition(position).toString();
                }else{
                    ar = "";
                }

                spDoc.setAdapter(adapter);
                spDoc.setSelection(adapter.getPosition(doc));

                break;
            case R.id.spDocEd:
                if(position != 0){
                    doc = parent.getItemAtPosition(position).toString();
                }else{
                    doc = "";
                }
                break;
            case R.id.spSexoEd:
                if(position != 0){
                    sex = parent.getItemAtPosition(position).toString();
                }else{
                    sex = "";
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        etFecha.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), imageBitmap, "Title", null);
            photoUri = Uri.parse(path);

            cargaArchivo();
            ivFoto.setImageURI(photoUri);
        }
    }

    private void cargaArchivo(){
        img = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "." + extension(photoUri);
        StorageReference ref = storageReference.child(img);

        ref.putFile(photoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getContext(), "Archivo cargado con exito", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Algo salio mal al cargar la foto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String extension(Uri uri){
        ContentResolver cr = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }
}