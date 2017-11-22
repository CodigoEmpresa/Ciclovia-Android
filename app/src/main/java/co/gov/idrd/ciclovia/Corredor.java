package co.gov.idrd.ciclovia;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * Created by JONATHAN.CASTRO on 22/11/2017.
 */

public class Corredor {

    private ArrayList<Punto> puntos;
    private ArrayList<LatLng> coordenadas;

    private Corredor(JSONObject corredor)
    {
        coordenadas = new ArrayList<LatLng>();
        puntos = new ArrayList<Punto>();

        try {
            JSONArray array_coordenadas = corredor.getJSONArray("coordenadas");
            JSONArray array_puntos = corredor.getJSONArray("puntos");

            for (int j = 0; j < array_coordenadas.length(); j++) {
                JSONObject coordenada = array_coordenadas.getJSONObject(j);
                coordenadas.add(new LatLng(coordenada.getDouble("latitud"), coordenada.getDouble("longitud")));
            }

            for (int j = 0; j < array_puntos.length(); j++) {
                JSONObject punto = array_puntos.getJSONObject(j);
                puntos.add(Punto.crearPuntoDeJSONObject(punto));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static Corredor crearCorredorDeJSONObject(JSONObject corredor) {
        return new Corredor(corredor);
    }

    public PolylineOptions obtenerRuta() {
        PolylineOptions ruta_corredor = new PolylineOptions();

        for(LatLng coordenada : coordenadas) {
            ruta_corredor.add(coordenada);
        }

        return ruta_corredor;
    }

    public ArrayList<Punto> obtenerPuntos() {
        return this.puntos;
    }

    public ArrayList<LatLng> obtenerCoordenadas() {
        return this.coordenadas;
    }

    public String obtenerRutaCodificada() {
        return PolyUtil.encode(this.coordenadas);
    }
}
