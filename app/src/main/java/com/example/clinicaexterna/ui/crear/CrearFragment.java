package com.example.clinicaexterna.ui.crear;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.clinicaexterna.R;
import com.example.clinicaexterna.mdbf.Paciente;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CrearFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, DatePickerDialog.OnDateSetListener {

    private CrearViewModel crearViewModel;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    private ImageView ivFoto;
    private EditText etID, etNombre, etPeso, etEstatura, etFecha, etEdad;
    private Spinner spArea, spGenero, spDoctor;
    private ImageButton btnFecha;
    private Button btnLimpiar, btnCrear;
    private Uri photoUri;
    private static int dia, mes, anio;

    public static final int REQUEST_TAKE_PHOTO = 1;
    String sexo, img = "", area, doc;

    DatePickerDialog dpd;
    Calendar c;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        crearViewModel =
                new ViewModelProvider(this).get(CrearViewModel.class);
        View root = inflater.inflate(R.layout.fragment_crear, container, false);
        final TextView textView = root.findViewById(R.id.text_gallery);
        crearViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        iniciarFirebase();
        iniciarComponentes(root);

        return root;
    }


    private void iniciarFirebase(){
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        storageReference = FirebaseStorage.getInstance().getReference("imagenes");
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void iniciarComponentes(View root){
        componentesBotones(root);
        componentesSpinners(root);
        componentesTexto(root);
    }


    private void componentesBotones(View root){
        btnCrear = root.findViewById(R.id.btnCrear);
        btnFecha = root.findViewById(R.id.btnCalendarioC);
        btnLimpiar = root.findViewById(R.id.btnLimpiaC);
        ivFoto = root.findViewById(R.id.ivFotoC);

        btnCrear.setOnClickListener(this);
        btnLimpiar.setOnClickListener(this);
        ivFoto.setOnClickListener(this);
        btnFecha.setOnClickListener(this);
    }

    private void componentesSpinners(View root){

        ArrayAdapter<CharSequence> areaAdapter, drAdapter, generoAdapter;
        areaAdapter = ArrayAdapter.createFromResource(getContext(), R.array.opciones, android.R.layout.simple_spinner_item);
        drAdapter = ArrayAdapter.createFromResource(getContext(), R.array.o0, android.R.layout.simple_spinner_item);
        generoAdapter = ArrayAdapter.createFromResource(getContext(), R.array.genero, android.R.layout.simple_spinner_item);


        spArea = root.findViewById(R.id.spAreaC);
        spArea.setAdapter(areaAdapter);
        spDoctor = root.findViewById(R.id.spDocC);
        spDoctor.setAdapter(drAdapter);
        spGenero = root.findViewById(R.id.spSexoC);
        spGenero.setAdapter(generoAdapter);

        spGenero.setOnItemSelectedListener(this);
        spDoctor.setOnItemSelectedListener(this);
        spArea.setOnItemSelectedListener(this);
    }

    private void componentesTexto(View root){
        etID = root.findViewById(R.id.etIDPacC);
        etNombre = root.findViewById(R.id.etNomPacC);
        etFecha = root.findViewById(R.id.etFechaC);
        etPeso = root.findViewById(R.id.etPesoC);
        etEstatura = root.findViewById(R.id.etEstC);
        etEdad = root.findViewById(R.id.etEdadC);
    }

    private void limpiar(){
        etEdad.setText("");
        etID.setText("");
        etNombre.setText("");
        etFecha.setText("");
        etPeso.setText("");
        etEstatura.setText("");

        ArrayAdapter<CharSequence> areaAdapter, drAdapter, generoAdapter;
        areaAdapter = ArrayAdapter.createFromResource(getContext(), R.array.opciones, android.R.layout.simple_spinner_item);
        drAdapter = ArrayAdapter.createFromResource(getContext(), R.array.o0, android.R.layout.simple_spinner_item);
        generoAdapter = ArrayAdapter.createFromResource(getContext(), R.array.genero, android.R.layout.simple_spinner_item);

        img = "";
        sexo = "";
        doc = "";
        area = "";

        spArea.setAdapter(areaAdapter);
        spDoctor.setAdapter(drAdapter);
        spGenero.setAdapter(generoAdapter);
        ivFoto.setImageResource(R.drawable.ic_menu_camera);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivFotoC:
                Intent tomaFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if(tomaFoto.resolveActivity(getActivity().getPackageManager()) != null){
                    startActivityForResult(tomaFoto, REQUEST_TAKE_PHOTO);
                }
                break;
            case R.id.btnLimpiaC:
                limpiar();
                break;
            case R.id.btnCalendarioC:
                c = Calendar.getInstance();
                anio = c.get(Calendar.YEAR);
                mes = c.get(Calendar.MONTH);
                dia = c.get(Calendar.DAY_OF_MONTH);

                dpd = new DatePickerDialog(getContext(), this, anio, mes, dia);
                dpd.show();
                break;
            case R.id.btnCrear:
                if(etID.getText().toString().isEmpty() || etNombre.getText().toString().isEmpty() || etFecha.getText().toString().isEmpty() || etPeso.getText().toString().isEmpty()
                       || etEstatura.getText().toString().isEmpty() || img.isEmpty() || sexo.isEmpty() || doc.isEmpty() || area.isEmpty()){
                    Toast.makeText(getContext(), "Hay campos vacios", Toast.LENGTH_SHORT).show();
                }else {
                    Paciente p = new Paciente();
                    p.setId(etID.getText().toString());
                    p.setNombre(etNombre.getText().toString());
                    p.setEdad(etEdad.getText().toString());
                    p.setEstatura(etEstatura.getText().toString());
                    p.setPeso(etPeso.getText().toString());
                    p.setFecha(etFecha.getText().toString());
                    p.setImg(img);
                    p.setSexo(sexo);
                    p.setArea(area);
                    p.setDoctor(doc);
                    p.setRecep(user.getEmail());
                    databaseReference.child("Pacientes").child(p.getId()).setValue(p);
                    Toast.makeText(getContext(), "Paciente agregado con exito", Toast.LENGTH_LONG).show();
                    limpiar();
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.spAreaC:
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
                    area = parent.getItemAtPosition(position).toString();
                }else{
                    area = "";
                }
                spDoctor.setAdapter(adapter);
                break;

            case R.id.spDocC:
                if(position != 0){
                    doc = parent.getItemAtPosition(position).toString();
                }else{
                    doc = "";
                }
                break;
            case R.id.spSexoC:
                if(position != 0){
                    sexo = parent.getItemAtPosition(position).toString();
                }else{
                    sexo = "";
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        etFecha.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
    }
}