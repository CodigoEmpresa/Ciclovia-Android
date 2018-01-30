package co.gov.idrd.ciclovia;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Calendar;

import co.gov.idrd.ciclovia.util.DatabaseManager;
import co.gov.idrd.ciclovia.util.Preferencias;

public class Datos extends AppCompatActivity {
    static EditText fecha;
    static EditText peso;
    static EditText altura;
    static EditText nombre;
    static Spinner sexo;
    static String email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final DatabaseManager db = new DatabaseManager(Datos.this);
        setContentView(R.layout.activity_datos);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        nombre = (EditText) findViewById(R.id.nombre);
        fecha = (EditText) findViewById(R.id.fecha);
        peso = (EditText) findViewById(R.id.peso);
        altura = (EditText) findViewById(R.id.altura);
        sexo = (Spinner) findViewById(R.id.sexo);
        email =  Preferencias.getUsername(Datos.this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sexo_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        sexo.setAdapter(adapter);
        //setSupportActionBar(toolbar);

        try{
            final Cursor c = db.obtenerdatos_persona(email);
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    nombre.setText(c.getString(c.getColumnIndexOrThrow("nombre")));
                    fecha.setText(c.getString(c.getColumnIndexOrThrow("fecha")));
                    altura.setText(c.getString(c.getColumnIndexOrThrow("altura")));
                    peso.setText(c.getString(c.getColumnIndexOrThrow("peso")));
                    sexo.setId(c.getInt(c.getColumnIndexOrThrow("sexo")));
                    c.moveToNext();
                }
            }
        }catch (Exception e){}



        /*
        "nombre"
        "fecha"
        "altura"
        "peso"
        "email"
        "sexo"
        "sincronizado"
         */
        Button guardar = (Button) findViewById(R.id.guardar);
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.agregar_datos(nombre.getText().toString(),fecha.getText().toString(),Integer.parseInt(altura.getText().toString()), Integer.parseInt(peso.getText().toString()),sexo.getSelectedItemId(),email);
                AlertDialog dialogo = createSimpleDialog("Alerta","Datos registrados correctamente");
                dialogo.show();
            }
        });
    }

    public AlertDialog createSimpleDialog(String titulo, String texto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Datos.this);

        builder.setTitle(titulo)
                .setMessage(texto)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                    finish();
                            }
                        });

        return builder.create();
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(this.getFragmentManager(), "datepicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
          fecha.setText(year+"-"+month+"-"+day);
        }
    }

}
