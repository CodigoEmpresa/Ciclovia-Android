package co.gov.idrd.ciclovia.util;

/**
 * Created by daniel on 2/01/18.
 */

public class Datamodel_rutas {


        String fecha;
        String medio;
        String imagen;
        String tiempo;

        public Datamodel_rutas(String fecha, String medio,String tiempo,String imagen) {
            this.fecha=fecha;
            this.medio=medio;
            this.imagen=imagen;


        }

        public String getfecha() {
            return fecha;
        }

        public String gettiempo() {
        return tiempo;
    }

        public String getmedio() {
            return medio;
        }

        public String getImagen() {
            return imagen;
        }

}
