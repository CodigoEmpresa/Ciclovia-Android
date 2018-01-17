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

    private static final String TAG = BuscadorDePuntos.class.getName();

    public static Location buscarPuntoCercano(Location actual, CharSequence tipo, ArrayList<Corredor> corredores) throws NullPointerException {
        Punto punto_cercano = null;

        float distancia = 1000000;
        float distancia_temporal = 0;
        int i = 0;
        for (Corredor corredor: corredores) {
            ArrayList<Punto> puntos = corredor.obtenerPuntosPorTipo(tipo.toString());
            for (Punto punto : puntos) {
                distancia_temporal = actual.distanceTo(punto.getLocation());
                Log.i(TAG, punto.getNombre()+" | "+punto.getDescripcion()+" | "+distancia_temporal);

                if (distancia_temporal < distancia)
                {
                    distancia = distancia_temporal;
                    punto_cercano = punto;
                }
            }
        }

        return punto_cercano.getLocation();
    }
}
