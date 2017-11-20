package co.gov.idrd.ciclovia;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import co.gov.idrd.ciclovia.util.RequestCaller;
import co.gov.idrd.ciclovia.util.RequestManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class Mapa extends Fragment implements View.OnClickListener, RequestCaller{

    private MapView map;
    private GoogleMap gmap;
    private Context context;
    private Activity activity;
    private LocationManager locationManager;
    private FloatingActionButton fab;
    private LocationTracker rastreador;
    private JSONArray corredores;
    private ArrayList<Punto> puntos;
    private ArrayList<Marker> marcadores;
    private final int PERMISO_DE_RASTREO_UBICACION = 1;

    public Mapa() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_mapa, container, false);

        context = getContext();
        activity = getActivity();
        puntos = new ArrayList<Punto>();
        marcadores = new ArrayList<Marker>();

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(this);
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
                gmap.clear();
                // For dropping a marker at a point on the Mapa
                Mapa.this.rastreador = new LocationTracker(activity, gmap);
                Mapa.this.cargarCorredores();
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Mapa del sistema");
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
        //rastreador.iniciarRastreo();
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
                        corredores = response.getJSONArray("corredores");

                        // dibujar la ruta
                        for (int i = 0; i < corredores.length(); i++) {
                            PolylineOptions ruta_corredores = new PolylineOptions();
                            JSONObject corredor = corredores.getJSONObject(i);
                            JSONArray array_coordenadas = corredor.getJSONArray("coordenadas");
                            JSONArray array_puntos = corredor.getJSONArray("puntos");

                            for (int j = 0; j < array_coordenadas.length(); j++) {
                                JSONObject coordenada = array_coordenadas.getJSONObject(j);
                                ruta_corredores.add(new LatLng(coordenada.getDouble("latitud"), coordenada.getDouble("longitud")));
                            }

                            for (int j = 0; j < array_puntos.length(); j++) {
                                JSONObject punto = array_puntos.getJSONObject(j);
                                puntos.add(Punto.crearPuntoDeJSONObject(punto));
                            }

                            gmap.addPolyline(ruta_corredores.width(12f).color(Color.rgb(79, 195, 247)));
                        }

                        // dibujar puntos
                        for (int i = 0; i < puntos.size(); i++) {
                            Punto punto = puntos.get(i);
                            Marker temp = gmap.addMarker(new MarkerOptions()
                                            .position(punto.getLatLng())
                                            .title(punto.getNombre())
                                        );
                            marcadores.add(temp);
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

    private class LocationTracker implements LocationListener, ActivityCompat.OnRequestPermissionsResultCallback {
        private LocationManager locationManager;
        private String[] permisos = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        private ArrayList<Location> ubicaciones = new ArrayList<Location>();
        private GoogleMap map;
        private Location bogota;
        private boolean rastrear = false;

        public LocationTracker(Activity activity, GoogleMap map) {
            this.locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            this.map = map;
            bogota = new Location("Bogota");
            bogota.setLatitude(4.6097100);
            bogota.setLongitude(-74.0817500);
            moverCamara(bogota, false);
        }

        @Override
        public void onLocationChanged(Location location) {
            if(rastrear) {
                moverCamara(location, true);
            } else {
                moverCamara(location, false);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            switch (requestCode) {
                case PERMISO_DE_RASTREO_UBICACION:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(activity, "Permission Granted!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, "Permission Denied!", Toast.LENGTH_SHORT).show();
                    }
            }
        }

        public void iniciarRastreo() {
            this.rastrear = true;

            if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(activity, permisos, PERMISO_DE_RASTREO_UBICACION);

                return;
            }

            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            else
                this.mostrarAlertaActivarGPS();

            gmap.setMyLocationEnabled(true);
        }

        public void detenerRastreo() {
            this.rastrear = false;
        }

        public boolean obtenerEstadoRastreo() {
            return this.rastrear;
        }

        private void mostrarAlertaActivarGPS() {
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage("Tu GPS parece estar deshabilitado, deseas habilitarlo?")
                    .setCancelable(false)
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }

        private void moverCamara(Location location, boolean tracking) {
            LatLng coordenadas = new LatLng(location.getLatitude(), location.getLongitude());
            CameraPosition cameraPosition = null;
            if(tracking) {
                cameraPosition = new CameraPosition.Builder().target(coordenadas).zoom(17).tilt(30).build();
            } else {
                cameraPosition = new CameraPosition.Builder().target(coordenadas).zoom(12).tilt(70).build();
            }
            gmap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

    }
}
