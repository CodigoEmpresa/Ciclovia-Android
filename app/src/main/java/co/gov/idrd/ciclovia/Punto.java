package co.gov.idrd.ciclovia;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by JONATHAN.CASTRO on 20/11/2017.
 */

public class Punto {
    
    private String tipo, nombre, descripcion, logo;
    private Double latitud, longitud;

    private Punto(String tipo, String nombre, String descripcion, String logo, Double latitud, Double longitud) {
        this.tipo = tipo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.logo = logo;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public static Punto crearPuntoDeJSONObject(JSONObject punto) throws JSONException {
        return new Punto(
                        punto.getString("nombrePunto"),
                        punto.getJSONObject("pivot").getString("nombreCP"),
                        punto.getJSONObject("pivot").getString("descripcionCP"),
                        punto.getString("imagenPunto"),
                        punto.getJSONObject("pivot").getDouble("latitud"),
                        punto.getJSONObject("pivot").getDouble("longitud")
                );
    }

    public String getTipo() {
        return this.tipo;
    }

    public String getNombre() {
        return this.nombre;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public String logo() {
        return this.logo;
    }

    public LatLng getCoords() {
        return new LatLng(this.latitud, this.longitud);
    }
}
