package co.gov.idrd.ciclovia.util;

/**
 * Created by daniel on 2/01/18.
 */

public class Datamodel_rutas {


        String fecha;
        String medio;

        public Datamodel_rutas(String fecha, String medio ) {
            this.fecha=fecha;
            this.medio=medio;


        }

        public String getfecha() {
            return fecha;
        }

        public String getmedio() {
            return medio;
        }

}
