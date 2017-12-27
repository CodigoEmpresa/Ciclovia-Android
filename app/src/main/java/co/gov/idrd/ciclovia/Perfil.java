package co.gov.idrd.ciclovia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.gov.idrd.ciclovia.util.Preferencias;
import co.gov.idrd.ciclovia.util.RequestCaller;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class Perfil extends Fragment {

    public static final int REQUEST_USER_DATA = 1000;
    private View view;
    private Context context;
    private FloatingActionButton boton_registro;
    private FloatingActionButton boton_datos;
    private TextView registrado;
    private TextView no_registrado;
    private String username;
    private Principal principal;
    private LinearLayout rutas_layout;

    public Perfil() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view = inflater.inflate(R.layout.fragment_perfil, container, false);

        context = getContext();
        this.boton_registro = (FloatingActionButton) view.findViewById(R.id.registro);
        this.boton_datos = (FloatingActionButton) view.findViewById(R.id.datos);
        this.registrado = (TextView) view.findViewById(R.id.registrado);
        this.no_registrado = (TextView) view.findViewById(R.id.noregistrado);
        this.username = Preferencias.getUsername(this.getContext());
        this.rutas_layout =(LinearLayout) view.findViewById(R.id.rutas_layout);
        this.principal = (Principal) getActivity();


        if(username != ""){
            boton_registro.setVisibility(View.INVISIBLE);
            boton_datos.setVisibility(View.VISIBLE);
            registrado.setText("Bienvenido usuario "+username);
            registrado.setVisibility(View.VISIBLE);
            no_registrado.setVisibility(View.INVISIBLE);
            rutas_layout.setVisibility(View.VISIBLE);


        }else{
            boton_registro.setVisibility(View.VISIBLE);
            boton_datos.setVisibility(View.INVISIBLE);
            registrado.setVisibility(View.INVISIBLE);
            no_registrado.setVisibility(View.VISIBLE);
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

        this.registrado.setText(username != "" ? username : "");
        this.boton_registro.setVisibility(visible);
        this.boton_datos.setVisibility(visible);
        this.registrado.setVisibility(visible);
        this.no_registrado.setVisibility(visible);

        principal.updateMenuLabels();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(RequestCaller.TAG, "Resultado en perfil: "+requestCode+" "+Perfil.REQUEST_USER_DATA+" "+resultCode);
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
