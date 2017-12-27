package co.gov.idrd.ciclovia;

import android.app.Application;
import com.facebook.stetho.Stetho;
import co.gov.idrd.ciclovia.util.DatabaseManager;

/**
 * Created by Jona on 27/12/2017.
 */

public class Ciclovia extends Application {

    private DatabaseManager db;

    @Override
    public void onCreate() {
        super.onCreate();

        db = new DatabaseManager(this);
        db.getWritableDatabase();
        Stetho.initializeWithDefaults(this);
    }
}
