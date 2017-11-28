package co.gov.idrd.ciclovia;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import co.gov.idrd.ciclovia.util.Preferencias;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Perfil.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class Perfil extends Fragment {


    public Perfil() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        FloatingActionButton boton_registro = (FloatingActionButton) view.findViewById(R.id.registro);
        FloatingActionButton boton_datos = (FloatingActionButton) view.findViewById(R.id.datos);
        TextView registrado = (TextView) view.findViewById(R.id.registrado);
        TextView no_registrado = (TextView) view.findViewById(R.id.noregistrado);
        String username = Preferencias.getUsername(this.getContext());

        if(username != ""){
            boton_registro.setVisibility(View.INVISIBLE);
            boton_datos.setVisibility(View.VISIBLE);
            registrado.setText("Bienvenido usuario "+username);
            registrado.setVisibility(View.VISIBLE);
            no_registrado.setVisibility(View.INVISIBLE);

        }else{
            boton_registro.setVisibility(View.VISIBLE);
            boton_datos.setVisibility(View.INVISIBLE);
            registrado.setVisibility(View.INVISIBLE);
            no_registrado.setVisibility(View.VISIBLE);

        }
        boton_registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registroIntent = new Intent(v.getContext(), Registro.class);
                startActivityForResult(registroIntent, 1);
            }
        });

        boton_datos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registroIntent = new Intent(v.getContext(), Datos.class);
                startActivityForResult(registroIntent, 1);
            }
        });

        return view;
    }
    @SuppressLint("MissingSuperCall")
    public void onResume(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        FloatingActionButton boton_registro = (FloatingActionButton) view.findViewById(R.id.registro);
        FloatingActionButton boton_datos = (FloatingActionButton) view.findViewById(R.id.datos);
        TextView registrado = (TextView) view.findViewById(R.id.registrado);
        TextView no_registrado = (TextView) view.findViewById(R.id.noregistrado);
        String username = Preferencias.getUsername(this.getContext());

        if(username != ""){
            boton_registro.setVisibility(View.INVISIBLE);
            boton_datos.setVisibility(View.VISIBLE);
            registrado.setText("Bienvenido usuario "+username);
            registrado.setVisibility(View.VISIBLE);
            no_registrado.setVisibility(View.INVISIBLE);

        }else{
            boton_registro.setVisibility(View.VISIBLE);
            boton_datos.setVisibility(View.INVISIBLE);
            registrado.setVisibility(View.INVISIBLE);
            no_registrado.setVisibility(View.VISIBLE);

        }
        boton_registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registroIntent = new Intent(v.getContext(), Registro.class);
                startActivityForResult(registroIntent, 1);
            }
        });

        boton_datos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registroIntent = new Intent(v.getContext(), Datos.class);
                startActivityForResult(registroIntent, 1);
            }
        });
    }


}
