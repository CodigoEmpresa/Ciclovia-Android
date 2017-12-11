package co.gov.idrd.ciclovia;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by JONATHAN.CASTRO on 20/11/2017.
 */

public class Punto {

    private String tipo, nombre, descripcion, logo, icono;
    private Double latitud, longitud;

    private Punto(String tipo, String nombre, String descripcion, String logo, String icono, Double latitud, Double longitud) {
        this.tipo = tipo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.logo = logo;
        this.icono = icono;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public static Punto crearPuntoDeJSONObject(JSONObject punto) throws JSONException {
        return new Punto(
                    punto.getString("nombrePunto"),
                    punto.getJSONObject("pivot").getString("nombreCP"),
                    punto.getJSONObject("pivot").getString("descripcionCP"),
                    punto.getString("imagenPunto"),
                    punto.getString("icono"),
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

    public String getIcono() {
        return this.icono;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public String logo() {
        return this.logo;
    }

    public LatLng getLatLng() {
        return new LatLng(this.latitud, this.longitud);
    }

    public Location getLocation() {
        Location location = new Location(this.getNombre()+" "+this.getDescripcion());
        location.setLatitude(this.getLatLng().latitude);
        location.setLongitude(this.getLatLng().longitude);
        return location;
    }

    public String toString() {
        return this.nombre+" "+this.descripcion;
    }
}
