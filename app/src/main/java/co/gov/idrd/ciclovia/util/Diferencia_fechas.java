package co.gov.idrd.ciclovia.util;

import android.util.Log;

import java.util.Date;

/**
 * Created by daniel on 2/01/18.
 */

public class Diferencia_fechas {
    private Date fechaInicial,fechaFinal;

    public void setFechainicial(Date f){
        fechaInicial = f;
    }

    public void setFechaFinal(Date f){
        fechaFinal = f;
    }

    public String getDiferencia(){

        long diferencia = fechaFinal.getTime() - fechaInicial.getTime();

        Log.i("MainActivity", "fechaInicial : " + fechaInicial);
        Log.i("MainActivity", "fechaFinal : " + fechaFinal);

        long segsMilli = 1000;
        long minsMilli = segsMilli * 60;
        long horasMilli = minsMilli * 60;
        long diasMilli = horasMilli * 24;

        long diasTranscurridos = diferencia / diasMilli;
        diferencia = diferencia % diasMilli;

        long horasTranscurridos = diferencia / horasMilli;
        diferencia = diferencia % horasMilli;

        long minutosTranscurridos = diferencia / minsMilli;
        diferencia = diferencia % minsMilli;

        long segsTranscurridos = diferencia / segsMilli;

        return horasTranscurridos +
                " Horas," + minutosTranscurridos + " min , " + segsTranscurridos + " seg";


    }
}
