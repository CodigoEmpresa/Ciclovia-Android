package co.gov.idrd.ciclovia;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import co.gov.idrd.ciclovia.image.BitmapFromVectorFactory;
import co.gov.idrd.ciclovia.util.RequestCaller;
import co.gov.idrd.ciclovia.util.RequestManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class Mapa extends Fragment implements View.OnClickListener,  RequestCaller {

    private final int DIALOGO_PUNTO_MAS_CERCANO = 0x64;
    private final int DIALOGO_INICIAR_RASTRE_RUTA = 0x6E;

    private Context context;
    private Principal principal;
    private MapView map;
    private GoogleMap gmap;
    private LocationManager locationManager;
    private FloatingActionMenu menu;
    private FloatingActionButton ir_a_punto;
    private ImageButton btn_location;

    private ArrayList<Corredor> corredores;
    private ArrayList<String> tipos_puntos;
    private Polyline ruta_calculada;
    private Punto destino = null;

    private boolean ubicado = false;
    private boolean seguimiento = false;
    private boolean ruta = false;

    public Mapa() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_mapa, container, false);

        context = getContext();
        principal = (Principal) getActivity();
        corredores = new ArrayList<Corredor>();
        tipos_puntos = new ArrayList<String>();

        menu = (FloatingActionMenu) rootView.findViewById(R.id.menu);
        menu.setClosedOnTouchOutside(true);
        ir_a_punto = (FloatingActionButton) rootView.findViewById(R.id.ir_a_punto);
        ir_a_punto.setOnClickListener(this);

        btn_location = (ImageButton) rootView.findViewById(R.id.btn_location);
        btn_location.setOnClickListener(this);
        map = (MapView) rootView.findViewById(R.id.map);
        map.onCreate(savedInstanceState);
        map.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        map.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                gmap = mMap;
                gmap.getUiSettings().setMapToolbarEnabled(false);
                gmap.getUiSettings().setMyLocationButtonEnabled(false);
                gmap.clear();

                Mapa.this.cargarCorredores();
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Corredor vial");
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        map.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map.onLowMemory();
    }

    @Override
    public void onClick(View view) {
        // rastreador.iniciarRastreo();
        switch (view.getId()) {
            case R.id.btn_location:
                //ubicado = true;
                if (!principal.checkPermissions())
                {
                    principal.requestPermissions();
                } else {
                    principal.startUpdatesHandler();
                }
                break;
            case R.id.ir_a_punto:
                menu.close(true);
                Dialog dialog = this.crearDialogo(DIALOGO_PUNTO_MAS_CERCANO);
                dialog.show();
                break;
        }
    }

    private void cargarCorredores() {
        String api = "api/corredores/obtener";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                Mapa.URL + api,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray json_corredores = response.getJSONArray("corredores");
                            ruta_calculada = gmap.addPolyline(new PolylineOptions().width(12f).color(Color.rgb(96, 125, 139)));

                            // dibujar la ruta
                            for (int i = 0; i < json_corredores.length(); i++) {
                                Corredor corredor = Corredor.crearCorredorDeJSONObject(json_corredores.getJSONObject(i));
                                corredores.add(corredor);
                                gmap.addPolyline(corredor.obtenerRuta().width(12f).color(Color.rgb(176, 190, 197)));

                                ArrayList<Punto> puntos = corredor.obtenerPuntos();
                                for (int j = 0; j < puntos.size(); j++) {
                                    Punto punto = puntos.get(j);
                                    int id_icon = BitmapFromVectorFactory.getResourcesIdFromString(Mapa.this.getContext(), punto.getIcono());
                                    if (!tipos_puntos.contains(punto.getNombre())) {
                                        tipos_puntos.add(punto.getNombre());
                                    }

                                    Marker temp = gmap.addMarker(new MarkerOptions()
                                            .position(punto.getLatLng())
                                            .title(punto.getNombre())
                                            .snippet(punto.getDescripcion())
                                            .icon(BitmapFromVectorFactory.fromResource(Mapa.this.getContext(), id_icon > 0 ? id_icon : R.drawable.ic_marcador_default))
                                    );
                                    temp.setTag(punto);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(Mapa.TAG, error.toString());
                    }
                }
        );

        RequestManager.getInstance(Mapa.this.getContext()).addToRequestQueue(request);
    }

    /*@Override
    public boolean onMarkerClick(Marker marker) {
        destino = (Punto) marker.getTag();

        return false;
    }*/

    private void modificarIndicador(int resId) {
        Mapa.this.btn_location.setImageResource(resId);
    }

    public void updateUI() {

    }

    /*@Override
    public void onCameraMoveStarted(int i) {
        if(ubicado && this.rastreador.GPSActivo()) {
            this.modificarIndicador(R.drawable.ic_location_enabled);
        } else if(!ubicado && this.rastreador.GPSActivo()) {
            this.modificarIndicador(R.drawable.ic_location_inactive);
        } else if (!this.rastreador.GPSActivo()) {
            this.modificarIndicador(R.drawable.ic_location_disabled);
        }
    }*/

    public Dialog crearDialogo(int dialogId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        switch (dialogId)
        {
            case DIALOGO_PUNTO_MAS_CERCANO:
                builder.setTitle("Selecciona el punto al que deseas ir")
                        .setItems(tipos_puntos.toArray(new CharSequence[tipos_puntos.size()]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                break;
            default:
                break;
        }

        return builder.create();
    }
}
