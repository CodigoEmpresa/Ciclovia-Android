package co.gov.idrd.ciclovia;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.Language;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import co.gov.idrd.ciclovia.image.BitmapFromVectorFactory;
import co.gov.idrd.ciclovia.util.BuscadorDePuntos;
import co.gov.idrd.ciclovia.util.OnLocationTry;
import co.gov.idrd.ciclovia.util.RequestCaller;
import co.gov.idrd.ciclovia.util.RequestManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class Mapa extends Fragment implements View.OnClickListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener, DirectionCallback, RequestCaller {

    private final int DIALOGO_PUNTO_MAS_CERCANO = 0x64;
    private final int DIALOGO_INICIAR_RASTREO_RUTA = 0x6E;
    private final int COLOR_TRAMOS =  Color.rgb(176, 190, 197);
    private final int COLOR_RUTA_CALCULADA =  Color.rgb(33, 150, 243);
    private final boolean ANIMAR = true;
    private final boolean NO_ANIMAR = false;

    private Context context;
    private Principal principal;
    private MapView map;
    private GoogleMap gmap;
    private LocationManager locationManager;
    private FloatingActionMenu menu;
    private FloatingActionButton ir_a_punto;
    private ImageButton btn_location;
    private ProgressDialog dialogo_cargando;

    private ArrayList<Corredor> corredores;
    private ArrayList<String> tipos_puntos;
    private Location bogota, ultima_ubicacion_conocida, punto_destino;
    private Polyline ruta_calculada;

    private boolean seguimiento = false;

    private boolean ubicado = false;
    private boolean registrando = false;
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

        bogota = new Location("Bogota");
        bogota.setLatitude(4.6097100);
        bogota.setLongitude(-74.0817500);

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
                gmap.setOnCameraMoveStartedListener(Mapa.this);
                gmap.setOnCameraIdleListener(Mapa.this);
                gmap.clear();
                updateUI();
                Mapa.this.cargarCorredores();
                Mapa.this.camaraInicial(bogota);
                enableLocation();
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
                if (ultima_ubicacion_conocida != null) moverCamara(ultima_ubicacion_conocida, ANIMAR);

                startTrace(new OnLocationTry() {
                    @Override
                    public void onStart() {
                        ubicado = true;
                        seguimiento = true;
                        enableLocation();
                        updateUI();
                    }

                    @Override
                    public void onFail() {
                        seguimiento = false;
                    }
                });
                updateUI();
                break;
            case R.id.ir_a_punto:

                menu.close(true);
                startTrace(new OnLocationTry() {
                    @Override
                    public void onStart() {
                        seguimiento = true;
                        enableLocation();
                        updateUI();
                        Dialog dialog = Mapa.this.crearDialogo(DIALOGO_PUNTO_MAS_CERCANO);
                        dialog.show();
                    }

                    @Override
                    public void onFail() {
                        seguimiento = false;
                    }
                });
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
                            ruta_calculada = gmap.addPolyline(new PolylineOptions().width(12f).color(COLOR_RUTA_CALCULADA));

                            // dibujar la ruta
                            for (int i = 0; i < json_corredores.length(); i++) {
                                Corredor corredor = Corredor.crearCorredorDeJSONObject(json_corredores.getJSONObject(i));
                                corredores.add(corredor);
                                gmap.addPolyline(corredor.obtenerRuta().width(12f).color(COLOR_TRAMOS));

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

    @Override
    public void onCameraMoveStarted(int i) {
        updateUI();
    }

    @Override
    public void onCameraIdle() {
        if (ubicado) ubicado = false;
    }

    public void onLocationChange(Location location) {
        ultima_ubicacion_conocida = location;

        if (ubicado) {
            moverCamara(location, ANIMAR);
        }

        detenerSeguimientoSiEsNecesario();
    }

    public void updateUI() {
        if (ubicado && principal.checkLocation() && principal.checkPermissions()) {
            this.modificarIndicador(R.drawable.ic_location_enabled);
        } else if (!ubicado && principal.checkLocation() && principal.checkPermissions()) {
            this.modificarIndicador(R.drawable.ic_location_inactive);
        } else if (!principal.checkLocation() || !principal.checkPermissions()) {
            this.modificarIndicador(R.drawable.ic_location_disabled);
        }
    }

    private void moverCamara(Location location, boolean animate) {
        LatLng coordenadas = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition cameraPosition = null;
        cameraPosition = new CameraPosition.Builder().target(coordenadas).zoom(gmap.getCameraPosition().zoom).tilt(gmap.getCameraPosition().tilt).bearing(gmap.getCameraPosition().bearing).build();

        if (animate)
            gmap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        else
            gmap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void camaraInicial(Location location) {
        LatLng coordenadas = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition cameraPosition = null;
        cameraPosition = new CameraPosition.Builder().target(coordenadas).zoom(11).build();

        gmap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void enableLocation() {
        if (ActivityCompat.checkSelfPermission(principal, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(principal, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }

        if (!gmap.isMyLocationEnabled())
            gmap.setMyLocationEnabled(true);
    }

    private void startTrace(OnLocationTry handler) {
        if (!principal.checkPermissions()) {
            principal.requestPermissions();
        } else {
            detenerSeguimientoSiEsNecesario();

            if (!seguimiento) {
                principal.startUpdatesHandler(handler);
            } else {
                handler.onStart();

            }
        }
    }

    public void detenerSeguimientoSiEsNecesario() {
        if (!registrando && !ruta) {
            seguimiento = false;
            principal.stopUpdatesHandler();
        }
    }

    public Dialog crearDialogo(int dialogId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        switch (dialogId)
        {
            case DIALOGO_PUNTO_MAS_CERCANO:
                builder.setTitle("Selecciona el punto al que deseas ir")
                        .setItems(tipos_puntos.toArray(new CharSequence[tipos_puntos.size()]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ruta = true;
                                try {
                                    punto_destino = BuscadorDePuntos.buscarPuntoCercano(ultima_ubicacion_conocida, tipos_puntos.get(i), Mapa.this.corredores);
                                    LatLng actual = new LatLng(ultima_ubicacion_conocida.getLatitude(), ultima_ubicacion_conocida.getLongitude());
                                    LatLng destino = new LatLng(punto_destino.getLatitude(), punto_destino.getLongitude());
                                    Log.i(TAG, "Actual: "+actual.latitude+" "+actual.longitude);
                                    Log.i(TAG, "Destino: "+destino.latitude+" "+destino.longitude);

                                    GoogleDirection.withServerKey("AIzaSyAtoqLzwwEf2ZWa6MvmgqloZMe9YILPurE")
                                                    .from(actual)
                                                    .to(destino)
                                                    .language(Language.SPANISH)
                                                    .transportMode(TransportMode.WALKING)
                                                    .execute(Mapa.this);
                                } catch (NullPointerException npe) {
                                    npe.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                break;
            default:

                break;
        }

        return builder.create();
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        Log.i(TAG, "Rutas encontradas: "+direction.getRouteList().size()+" detalles: "+rawBody);
        if(direction.getRouteList().size() > 0) {
            for(Route r : direction.getRouteList()) {
                Log.i(TAG, "Ruta: "+r);
            }
            Route route = direction.getRouteList().get(0);
            Leg leg = route.getLegList().get(0);
            ArrayList<LatLng> coordenadas = leg.getDirectionPoint();
            if(ruta_calculada != null) {
                ruta_calculada.remove();
            }

            ruta_calculada = gmap.addPolyline(new PolylineOptions().addAll(coordenadas).zIndex(2f).width(12f).color(COLOR_RUTA_CALCULADA));
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Log.i(TAG, t.getMessage());
    }
}
