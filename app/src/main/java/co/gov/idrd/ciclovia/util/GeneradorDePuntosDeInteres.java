package co.gov.idrd.ciclovia.util;

import android.location.Location;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import co.gov.idrd.ciclovia.Corredor;
import co.gov.idrd.ciclovia.Punto;

/**
 * Created by JONATHAN.CASTRO on 23/11/2017.
 */

public class GeneradorDePuntosDeInteres {

    public static ArrayList<LatLng> obtenerPuntosDeInteres(LatLng actual, Punto destino, ArrayList<Corredor> corredores) {
        ArrayList<LatLng> puntos_de_interes = new ArrayList<LatLng>();
        Location loc_actual = new Location("actual");
        loc_actual.setLatitude(actual.latitude);
        loc_actual.setLongitude(actual.longitude);

        Location loc_destino = new Location("destino");
        loc_destino.setLatitude(destino.getLatLng().latitude);
        loc_destino.setLongitude(destino.getLatLng().longitude);

        for(Corredor corredor : corredores)
        {

        }

        return puntos_de_interes;
    }
}
