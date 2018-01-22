package co.gov.idrd.ciclovia;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import java.util.LinkedHashMap;
import java.util.Map;

import co.gov.idrd.ciclovia.image.BitmapFromVectorFactory;
import co.gov.idrd.ciclovia.util.BuscadorDePuntos;
import co.gov.idrd.ciclovia.services.OnLocationHandler;
import co.gov.idrd.ciclovia.util.Preferencias;
import co.gov.idrd.ciclovia.util.RequestCaller;
import co.gov.idrd.ciclovia.util.RequestManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class Mapa extends Fragment implements View.OnClickListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveCanceledListener, DirectionCallback, RequestCaller, GoogleMap.OnCameraMoveListener {

    public static final int UBICAR = 0xC8;
    public static final int REGISTRAR = 0xD2;

    private final String TAG = Mapa.class.getName();
    private final String SIN_MEDIO = "";
    private final int DIALOGO_PUNTO_MAS_CERCANO = 0x64;
    private final int DIALOGO_INICIAR_RASTREO_RUTA = 0x6E;
    private final int DIALOGO_CANCELAR_RASTREO_RUTA = 0x6F;
    private final int DIALOGO_FINALIZAR_RASTREO_RUTA = 0x70;
    private final int COLOR_TRAMOS =  Color.rgb(176, 190, 197);
    private final int COLOR_RUTA_CALCULADA =  Color.rgb(33, 150, 243);
    private final int COLOR_RUTA_REGISTRADA =  Color.rgb(139, 195, 74);
    private final boolean ANIMAR = true;
    private final boolean NO_ANIMAR = false;

    private Context context;
    private Principal principal;
    private MapView map;
    private GoogleMap gmap;
    private RelativeLayout controles;
    private TextView cronometro;
    private FloatingActionMenu menu;
    private FloatingActionButton ir_a_punto, iniciar_recorrido;
    private ImageButton btn_location, finalizar_recorrido;

    private CameraPosition cameraposition;

    private ArrayList<Corredor> corredores;
    private LinkedHashMap<String, Location> registro_ruta;
    private ArrayList<String> tipos_puntos, medios_de_transporte;
    private Location bogota, ultima_ubicacion_conocida, punto_destino;
    private Polyline ruta_calculada, ruta_registrada;

    private boolean seguimiento = false;
    private boolean registrando = false;
    private boolean ruta = false;
    private boolean ubicado = false;
    private String medio_de_transporte = "";
    private long id_ruta;
    private int id_tipo_punto_destino;

    public Mapa() {
        // Required empty public constructor

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_mapa, container, false);
        context = getContext();
        principal = (Principal) getActivity();
        registro_ruta = new LinkedHashMap<String, Location>();
        corredores = new ArrayList<Corredor>();
        tipos_puntos = new ArrayList<String>();
        medios_de_transporte = new ArrayList<String>();
        medios_de_transporte.add("Bicicleta");
        medios_de_transporte.add("Patines");
        medios_de_transporte.add("Trotando");
        configureUI(rootView, savedInstanceState);

        //cargar la vuelta
        LatLng coordenadas = new LatLng(Preferencias.getlatitude(this.context), Preferencias.getlongitude(this.context));
       this.cameraposition = new CameraPosition.Builder().target(coordenadas).zoom(Preferencias.getzoom(this.context)).tilt(Preferencias.gettilt(this.context)).bearing(Preferencias.getbearing(this.context)).build();
        //fin la vuelta
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
    public void onCameraMoveStarted(int i) {
        updateUI();
    }



    @Override

    public void onCameraMove (){
        //preferences save
        Preferencias.setzoom(this.context,gmap.getCameraPosition().zoom);
        Log.e(TAG, ""+Preferencias.getzoom(this.context));
        Preferencias.settilt(this.context,gmap.getCameraPosition().tilt);
        Log.e(TAG, ""+Preferencias.gettilt(this.context));
        Preferencias.setbearing(this.context,gmap.getCameraPosition().bearing);
        Log.e(TAG, ""+Preferencias.getbearing(this.context));
        //end preferences save
    }



    @Override
    public void onCameraMoveCanceled() {
        ubicado = false;
        updateUI();
    }

    @Override
    public void onCameraIdle() {
        ubicado = false;
    }

    @Override
    public void onClick(View view) {
        if (!principal.checkPermissions()) {
            menu.close(true);
            principal.requestPermissions();
        } else {
            switch (view.getId()) {
                case R.id.btn_location:
                    if (ultima_ubicacion_conocida != null) moverCamara(ultima_ubicacion_conocida, ANIMAR);

                    startTrace(new OnLocationHandler() {
                        @Override
                        public void onStart() {
                            ubicado = true;
                            seguimiento = true;
                            enableLocation();
                            updateUI();

                            principal.setUbicado(ubicado);
                            principal.setSeguimiento(seguimiento);
                        }

                        @Override
                        public void onFail() {
                            ubicado = false;
                            seguimiento = false;
                            principal.setUbicado(ubicado);
                            principal.setSeguimiento(seguimiento);
                        }
                    }, Mapa.UBICAR, SIN_MEDIO);
                    updateUI();
                    break;
                case R.id.ir_a_punto:
                    menu.close(true);
                    Dialog dialog_punto_cercano = Mapa.this.crearDialogo(DIALOGO_PUNTO_MAS_CERCANO);
                    dialog_punto_cercano.show();
                    break;
                case R.id.iniciar_recorrido:
                    menu.close(true);
                    if (!registrando) {
                        Dialog dialog_iniciar_rastreo = Mapa.this.crearDialogo(DIALOGO_INICIAR_RASTREO_RUTA);
                        dialog_iniciar_rastreo.show();
                    } else {
                        Dialog dialog_rastreo_finalizar = Mapa.this.crearDialogo(DIALOGO_FINALIZAR_RASTREO_RUTA);
                        dialog_rastreo_finalizar.show();
                    }
                    break;
                case R.id.btn_finalizar_recorrido:
                    menu.close(true);
                    Dialog dialog_rastreo_finalizar = Mapa.this.crearDialogo(DIALOGO_FINALIZAR_RASTREO_RUTA);
                    dialog_rastreo_finalizar.show();
                    break;
            }
        }
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        Log.i(TAG, "Ruta obtenida: "+rawBody);
        if (direction.getRouteList().size() > 0) {
            Route route = direction.getRouteList().get(0);
            Leg leg = route.getLegList().get(0);
            ArrayList<LatLng> coordenadas = leg.getDirectionPoint();
            if(ruta_calculada != null) {
                ruta_calculada.setPoints(new ArrayList<LatLng>());
            }

            ruta_calculada = gmap.addPolyline(new PolylineOptions().addAll(coordenadas).zIndex(2f).width(12f).color(COLOR_RUTA_CALCULADA));
            ruta = false;
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Log.i(TAG, "No se logro obtener una ruta: "+t.getMessage());
    }

    public Dialog crearDialogo(int dialogId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        switch (dialogId)
        {
            case DIALOGO_PUNTO_MAS_CERCANO:
                if (!tipos_puntos.isEmpty()) {
                    builder.setTitle("Selecciona el punto al que deseas ir: ")
                            .setItems(tipos_puntos.toArray(new CharSequence[tipos_puntos.size()]), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    id_tipo_punto_destino = i;

                                    startTrace(new OnLocationHandler() {
                                        @Override
                                        public void onStart() {
                                            ubicado = true;
                                            ruta = true;
                                            seguimiento = true;
                                            enableLocation();
                                            updateUI();

                                            principal.setUbicado(ubicado);
                                            principal.setSeguimiento(seguimiento);
                                        }

                                        @Override
                                        public void onFail() {
                                            ubicado = false;
                                            ruta = false;
                                            seguimiento = false;

                                            principal.setUbicado(ubicado);
                                            principal.setSeguimiento(seguimiento);
                                        }
                                    }, Mapa.UBICAR, SIN_MEDIO);

                                }
                            });
                } else {
                    cargarCorredores();
                }

                break;
            case DIALOGO_INICIAR_RASTREO_RUTA:
                builder.setTitle("Iniciar recorrido en: ")
                        .setItems(medios_de_transporte.toArray(new CharSequence[medios_de_transporte.size()]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                registrando = true;
                                medio_de_transporte = medios_de_transporte.get(i);

                                startTrace(new OnLocationHandler() {
                                    @Override
                                    public void onStart() {
                                        registrando = true;
                                        seguimiento = true;
                                        enableLocation();
                                        updateUI();
                                        controles.setVisibility(View.VISIBLE);
                                        cronometro.setText("00:00");

                                        principal.setRegistrando(registrando);
                                        principal.setSeguimiento(seguimiento);
                                    }

                                    @Override
                                    public void onFail() {
                                        registrando = false;
                                        seguimiento = false;

                                        principal.setRegistrando(registrando);
                                        principal.setSeguimiento(seguimiento);
                                    }
                                }, Mapa.REGISTRAR, medio_de_transporte);
                            }
                        });
                break;
            default:
                builder.setTitle("Finalizar el registro de la ruta")
                        .setMessage("¿Realmente desea finalizar el registro de la ruta?")
                        .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                registrando = false;
                                registro_ruta.clear();
                                controles.setVisibility(View.INVISIBLE);
                                cronometro.setText("00:00");
                                ruta_registrada.setPoints(new ArrayList<LatLng>());
                                principal.setRegistrando(registrando);
                                detenerSeguimientoSiEsNecesario(Mapa.REGISTRAR);
                            }
                        }).setNegativeButton(R.string.no, null);
                break;
        }

        return builder.create();
    }

    public void onLocationChange(Location location, int opcion) {
        try {
            Log.i(TAG, "Ubicación obtenida: "+location.toString());
            ultima_ubicacion_conocida = location;

            if (ubicado) {
                moverCamara(location, ANIMAR);
            }

            if (ruta) {
                    punto_destino = BuscadorDePuntos.buscarPuntoCercano(ultima_ubicacion_conocida, tipos_puntos.get(id_tipo_punto_destino), Mapa.this.corredores);
                    LatLng actual = new LatLng(ultima_ubicacion_conocida.getLatitude(), ultima_ubicacion_conocida.getLongitude());
                    LatLng destino = new LatLng(punto_destino.getLatitude(), punto_destino.getLongitude());



                    GoogleDirection.withServerKey("AIzaSyAtoqLzwwEf2ZWa6MvmgqloZMe9YILPurE")
                            .from(actual)
                            .to(destino)
                            .language(Language.SPANISH)
                            .transportMode(TransportMode.WALKING)
                            .execute(Mapa.this);
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            detenerSeguimientoSiEsNecesario(Mapa.UBICAR);
        }
    }

    public void onRouteChange(LinkedHashMap<String, Location> registro_ruta) {
        if (registrando) {
            ArrayList<LatLng> coordenadas = new ArrayList<LatLng>();

            this.registro_ruta = registro_ruta;
            int i = 0;
            for (Map.Entry reg : registro_ruta.entrySet()) {
                i++;
                Location history_location = (Location) reg.getValue();
                coordenadas.add(new LatLng(history_location.getLatitude(), history_location.getLongitude()));

                if (i == registro_ruta.size()) {
                    moverCamara(history_location, ANIMAR);
                }
            }

            if (coordenadas.size() > 2) {
                if (ruta_registrada != null) {
                    ruta_registrada.setPoints(coordenadas);
                }
            }
        }
    }

    public void onRouteChangeTime(String time) {
        Log.i(TAG, "onRouteChangeTime()");
        if (registrando) {
            controles.setVisibility(View.VISIBLE);
            cronometro.setText(time);
        }
    }

    public void updateFragmentFromRoute(String time, long id_r, LinkedHashMap<String, Location> registro_ruta) {
        Log.i(TAG, "updateFragmentFromRoute()");
        registrando = true;
        seguimiento = true;
        id_ruta = id_r;

        onRouteChange(registro_ruta);
        onRouteChangeTime(time);
        principal.setRegistrando(registrando);
        principal.setSeguimiento(seguimiento);
    }

    public void detenerSeguimientoSiEsNecesario(int opcion) {
        Log.i(TAG, "detenerSeguimientoSiEsNecesario() " + opcion);
        Log.i(TAG, "Detener si es necesario: "+(registrando ? "1" : "0")+" - "+(ruta ? "1" : "0"));

        switch (opcion) {
            case Mapa.UBICAR:
                    principal.stopUpdatesHandler(opcion);
                    seguimiento = false;
                break;
            case Mapa.REGISTRAR:
                if (!registrando && !ruta) {
                    seguimiento = false;
                    principal.stopUpdatesHandler(opcion);
                    principal.setSeguimiento(false);
                }
                break;
        }
    }

    public void updateUI() {
        if (ubicado && principal.checkLocation() && principal.checkPermissions()) {
            btn_location.setImageResource(R.drawable.ic_location_enabled);

        } else if (!ubicado && principal.checkLocation() && principal.checkPermissions()) {
            btn_location.setImageResource(R.drawable.ic_location_inactive);
        } else if (!principal.checkLocation() || !principal.checkPermissions()) {
            btn_location.setImageResource(R.drawable.ic_location_disabled);
        }
    }

    public void restoreFromService(boolean ubicado, boolean registrando, boolean ruta, boolean seguimiento) {
        this.ubicado = ubicado;
        this.registrando = registrando;
        this.ruta = ruta;
        this.seguimiento = seguimiento;
    }

    private void configureUI(View rootView, Bundle savedInstanceState) {
        menu = (FloatingActionMenu) rootView.findViewById(R.id.menu);
        menu.setClosedOnTouchOutside(true);
        ir_a_punto = (FloatingActionButton) rootView.findViewById(R.id.ir_a_punto);
        ir_a_punto.setOnClickListener(this);
        iniciar_recorrido = (FloatingActionButton) rootView.findViewById(R.id.iniciar_recorrido);
        iniciar_recorrido.setOnClickListener(this);

        controles = (RelativeLayout) rootView.findViewById(R.id.controles);
        cronometro = (TextView) rootView.findViewById(R.id.cronometro);
        finalizar_recorrido = (ImageButton) rootView.findViewById(R.id.btn_finalizar_recorrido);
        finalizar_recorrido.setOnClickListener(this);

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
                gmap.getUiSettings().setCompassEnabled(false);
                gmap.setOnCameraMoveStartedListener(Mapa.this);
                gmap.setOnCameraMoveListener(Mapa.this);
                gmap.setOnCameraMoveCanceledListener(Mapa.this);
                gmap.setOnCameraIdleListener(Mapa.this);
                gmap.clear();

                ruta_calculada = gmap.addPolyline(new PolylineOptions().width(12f).color(COLOR_RUTA_CALCULADA));
                ruta_registrada = gmap.addPolyline(new PolylineOptions().width(12f).color(COLOR_RUTA_REGISTRADA));


                updateUI();
                Mapa.this.cargarCorredores();
                Mapa.this.camaraInicial(bogota);
                enableLocation();
            }
        });
    }

    private void startTrace(OnLocationHandler handler, int option, String medio) {
        Log.i(TAG, "startTrace() "+option+" "+(seguimiento ? "1" : "0")+" "+medio);
        if (!seguimiento) {
            principal.startUpdatesHandler(handler, option, medio);
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
                            int id_icon = 0;
                            // dibujar la ruta
                            for (int i = 0; i < json_corredores.length(); i++) {
                                Corredor corredor = Corredor.crearCorredorDeJSONObject(json_corredores.getJSONObject(i));
                                corredores.add(corredor);
                                gmap.addPolyline(corredor.obtenerRuta().width(12f).color(COLOR_TRAMOS));

                                ArrayList<Punto> puntos = corredor.obtenerPuntos();
                                for (int j = 0; j < puntos.size(); j++) {
                                    Punto punto = puntos.get(j);

                                    if (!tipos_puntos.contains(punto.getNombre())) {
                                        tipos_puntos.add(punto.getNombre());
                                    }

                                    try {
                                        id_icon = BitmapFromVectorFactory.getResourcesIdFromString(Mapa.this.getContext(), punto.getIcono());
                                        Marker temp = gmap.addMarker(new MarkerOptions()
                                                .position(punto.getLatLng())
                                                .title(punto.getNombre())
                                                .snippet(punto.getDescripcion())
                                                .icon(BitmapFromVectorFactory.fromResource(Mapa.this.getContext(), id_icon > 0 ? id_icon : R.drawable.ic_marcador_default))
                                        );
                                        temp.setTag(punto);
                                    } catch (Exception e) {
                                       e.printStackTrace();
                                    }
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
                        Log.e(TAG, error.toString());
                    }
                }
        );

        RequestManager.getInstance(Mapa.this.getContext()).addToRequestQueue(request);
    }

    private void enableLocation() {
        if (ActivityCompat.checkSelfPermission(principal, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(principal, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        if (!gmap.isMyLocationEnabled()) gmap.setMyLocationEnabled(true);
    }

    private void moverCamara(Location location, boolean animate) {
        LatLng coordenadas = new LatLng(location.getLatitude(), location.getLongitude());
        this.cameraposition = new CameraPosition.Builder().target(coordenadas).zoom(gmap.getCameraPosition().zoom).tilt(gmap.getCameraPosition().tilt).bearing(gmap.getCameraPosition().bearing).build();

        if (animate)
            gmap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraposition));
        else
            gmap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraposition));
    }

    private void camaraInicial(Location location) {
        LatLng coordenadas = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition cameraPosition = null;
        cameraPosition = new CameraPosition.Builder().target(coordenadas).zoom(13).build();

        gmap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
