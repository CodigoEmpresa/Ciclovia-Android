package co.gov.idrd.ciclovia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import co.gov.idrd.ciclovia.util.DatabaseManager;
import co.gov.idrd.ciclovia.util.Datamodel_rutas;
import co.gov.idrd.ciclovia.util.Preferencias;
import co.gov.idrd.ciclovia.util.RequestCaller;
import co.gov.idrd.ciclovia.util.Rutas_Adapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class Perfil extends Fragment {

    public static final int REQUEST_USER_DATA = 1000;
    private final String TAG = Perfil.class.getName();
    private View view;
    private Context context;
    private FloatingActionButton boton_registro;
    private FloatingActionButton boton_datos;
    private TextView no_registrado;
    private String username;
    private Principal principal;
    private ListView rutas_layout;
    ArrayList<Datamodel_rutas> dataModels = new ArrayList<Datamodel_rutas>();

    public Perfil() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view = inflater.inflate(R.layout.fragment_perfil, container, false);
        final DatabaseManager db = new DatabaseManager(getActivity());
        context = getContext();
        this.boton_registro = (FloatingActionButton) view.findViewById(R.id.registro);
        this.boton_datos = (FloatingActionButton) view.findViewById(R.id.datos);
        this.no_registrado = (TextView) view.findViewById(R.id.noregistrado);
        this.username = Preferencias.getUsername(this.getContext());
        this.rutas_layout = (ListView) view.findViewById(R.id.lista_rutas);
        this.principal = (Principal) getActivity();

        if (username != "") {
            boton_registro.setVisibility(View.INVISIBLE);
            boton_datos.setVisibility(View.VISIBLE);
            no_registrado.setText("Bienvenido usuario " + username);
            rutas_layout.setVisibility(View.VISIBLE);
            String tiempo = "";
            try {
                final Cursor c = db.getTabla("rutas").obtenerdatos();
                if (c.moveToFirst()) {
                    while (!c.isAfterLast()) {

                        try {
                            Cursor p = db.getTabla("puntos").rawQuery(" SELECT MAX(tiempo) FROM puntos WHERE id_ruta = " + c.getString(c.getColumnIndexOrThrow("id")) + " ;");

                            if (p.moveToFirst()) {
                                while (!p.isFirst()) {
                                    tiempo = p.getString(p.getColumnIndexOrThrow("tiempo"));
                                }
                            }
                        } catch (Exception e) {
                        }


                        dataModels.add(new Datamodel_rutas(c.getString(c.getColumnIndexOrThrow("creacion")), c.getString(c.getColumnIndexOrThrow("medio")), tiempo, ""));
                        c.moveToNext();
                    }
                }
            } catch (Exception e) {
            }

            Rutas_Adapter adapter = new Rutas_Adapter(dataModels, getActivity());

            rutas_layout.setAdapter(adapter);
            rutas_layout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Datamodel_rutas dataModel = Perfil.this.dataModels.get(position);
                    Snackbar.make(view, dataModel.getfecha() + "\n" + dataModel.getmedio(), Snackbar.LENGTH_LONG)
                            .setAction("Sin acción", null).show();
                }
            });


        } else {
            boton_registro.setVisibility(View.VISIBLE);
            boton_datos.setVisibility(View.INVISIBLE);
        }

        boton_registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registroIntent = new Intent(context, Registro.class);
                startActivityForResult(registroIntent, REQUEST_USER_DATA);
            }
        });

        boton_datos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registroIntent = new Intent(context, Datos.class);
                startActivity(registroIntent);
            }
        });

        return view;
    }

    public void updateUI() {
        String username = Preferencias.getUsername(this.getContext());
        int visible = username != "" ? View.VISIBLE : View.INVISIBLE;

        this.no_registrado.setText(username != "" ? username : "");
        this.boton_registro.setVisibility(visible);
        this.boton_datos.setVisibility(visible);

        principal.updateMenuLabels();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Resultado en perfil: " + requestCode + " " + Perfil.REQUEST_USER_DATA + " " + resultCode);
        switch (requestCode) {
            case Perfil.REQUEST_USER_DATA:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        principal.updateMenuLabels();
                        updateUI();
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
        }
    }
}
