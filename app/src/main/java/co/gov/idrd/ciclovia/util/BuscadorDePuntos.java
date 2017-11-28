package co.gov.idrd.ciclovia.util;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;

import co.gov.idrd.ciclovia.Corredor;
import co.gov.idrd.ciclovia.Punto;

/**
 * Created by JONATHAN.CASTRO on 28/11/2017.
 */

public class BuscadorDePuntos {
    public static Location buscarPuntoCercano(Location actual, CharSequence tipo, ArrayList<Corredor> corredores) {
        Location punto_cercano = new Location("Punto mas cercano");

        float distancia = 100000;
        float distancia_temporal = 0;
        for (Corredor corredor: corredores) {
            ArrayList<Punto> puntos = corredor.obtenerPuntos();
            for (Punto punto : puntos) {
                distancia_temporal = actual.distanceTo(punto.getLocation());

                if (distancia_temporal < distancia)
                {
                    distancia = distancia_temporal;
                    punto_cercano = punto.getLocation();
                }
            }
        }

        Log.d(RequestCaller.TAG, tipo+" - "+distancia);

        return punto_cercano;
    }
}
