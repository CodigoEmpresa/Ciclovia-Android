package co.gov.idrd.ciclovia;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import co.gov.idrd.ciclovia.util.CanAccessPrincipal;
import co.gov.idrd.ciclovia.util.Preferencias;

import static com.android.volley.VolleyLog.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Perfil.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class Perfil extends Fragment implements CanAccessPrincipal{

    private View view;
    private FloatingActionButton boton_registro;
    private FloatingActionButton boton_datos;
    private TextView registrado;
    private TextView no_registrado;
    private String username;
    private Principal principal;

    public Perfil() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view = inflater.inflate(R.layout.fragment_perfil, container, false);
        this.boton_registro = (FloatingActionButton) view.findViewById(R.id.registro);
        this.boton_datos = (FloatingActionButton) view.findViewById(R.id.datos);
        this.registrado = (TextView) view.findViewById(R.id.registrado);
        this.no_registrado = (TextView) view.findViewById(R.id.noregistrado);
        this.username = Preferencias.getUsername(this.getContext());

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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);


        if (requestCode == 1) {
            String username = Preferencias.getUsername(this.getContext());
            principal.referenciaDesdeFragment();
            if(username != ""){
                this.boton_registro.setVisibility(View.INVISIBLE);
                this.boton_datos.setVisibility(View.VISIBLE);
                this.registrado.setText("Bienvenido usuario "+username);
                this.registrado.setVisibility(View.VISIBLE);
                this.no_registrado.setVisibility(View.INVISIBLE);

            }else{
                this.boton_registro.setVisibility(View.VISIBLE);
                this.boton_datos.setVisibility(View.INVISIBLE);
                this.registrado.setVisibility(View.INVISIBLE);
                this.no_registrado.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

}
