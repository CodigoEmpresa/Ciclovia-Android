package co.gov.idrd.ciclovia.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;

import co.gov.idrd.ciclovia.Mapa;
import co.gov.idrd.ciclovia.Principal;
import co.gov.idrd.ciclovia.R;
import co.gov.idrd.ciclovia.util.DatabaseManager;
import co.gov.idrd.ciclovia.util.Tabla;

public class LocationService extends Service {

    private static final int NOTIFICATION_ID = 10000001;
    private static final String PACKAGE_NAME = "co.gov.idrd.ciclovia.services";
    private static final String TAG = LocationService.class.getSimpleName();
    private static final String CHANNEL_ID = "channel_01";
    public static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
    public static final String EXTRA_ACTION = PACKAGE_NAME + ".action";
    public static final String EXTRA_TIME = PACKAGE_NAME + ".time";
    public static final String EXTRA_TRANSPORT = PACKAGE_NAME + ".transporte";
    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    public static final String EXTRA_ROUTE = PACKAGE_NAME + ".route";

    private final IBinder mBinder = new LocalBinder();

    private boolean mChangingConfiguration = false;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;
    private Timer timer;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Handler mServiceHandler;
    private LinkedHashMap<String, Location> registro_ruta;
    private Location mLocation;
    private String tiempo = "";
    private String medio_transporte = "";
    private PendingIntent activityPendingIntent;
    private Intent intent;
    private int opcion = 0;
    private DatabaseManager db;
    private Tabla rutas;

    public LocationService() {}

    @Override
    public void onCreate() {
        Log.i(TAG, "in onCreate()");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            onNewLocation(locationResult.getLastLocation());
            }
        };

        mLocationRequest = LocationRequestProvider.get();
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        timer = new Timer();

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);

            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        registro_ruta = new LinkedHashMap<String, Location>();
        db = new DatabaseManager(this);
        rutas = db.getTabla(DatabaseManager.TABLA_RUTAS);
        configureNotificationBuilder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");

        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");

        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration && Utils.requestingLocationUpdates(this)) {
            Log.i(TAG, "Starting foreground service");

            startForeground(NOTIFICATION_ID, getNotification());
        }

        return true;
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void requestLocationUpdates(int opcion, String medio_transporte) {
        Log.i(TAG, "Requesting location updates");
        this.opcion = opcion;
        this.medio_transporte = medio_transporte;

        switch (this.opcion) {
            case Mapa.UBICAR:
                tiempo = "";
                mNotificationManager.notify(NOTIFICATION_ID, getNotification());
                break;
            case Mapa.REGISTRAR:
                if(timer == null) timer = new Timer();

                TimerTask task = new TimerTask()
                {
                    private final Handler mHandler = new Handler(Looper.getMainLooper());
                    private long time = 0;

                    @Override
                    public void run()
                    {
                        mHandler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                time = time + 1;
                                tiempo = DateUtils.formatElapsedTime(time);
                                mNotificationManager.notify(NOTIFICATION_ID, getNotification());
                            }
                        });
                    }
                };
                timer.scheduleAtFixedRate(task, 0, 1000);
                break;
        }

        Utils.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), LocationService.class));

        // Update notification content if running as a foreground service.
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            Log.i(TAG, "Error");
            Utils.setRequestingLocationUpdates(this, false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            Utils.setRequestingLocationUpdates(this, false);
            mNotificationManager.cancel(NOTIFICATION_ID);
            tiempo = "";
            opcion = 0;
            medio_transporte = "";
            if (timer != null) {
                timer.cancel();
                timer.purge();
                timer = null;
            }
            stopSelf();
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        } catch (IllegalStateException ise) {
            Log.e(TAG, "Timer already canceled. ");
        }
    }

    private void configureNotificationBuilder() {

    }

    private Notification getNotification() {
        intent = new Intent(this, Principal.class);
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);
        intent.putExtra(EXTRA_TIME, tiempo);
        intent.putExtra(EXTRA_TRANSPORT, medio_transporte);
        intent.putExtra(EXTRA_ROUTE, Utils.routeToString(registro_ruta));

        CharSequence text = Utils.getLocationText(mLocation);

        // The PendingIntent to launch activity.
        activityPendingIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String title = "";
        String content = "";
        int priority = 0;

        switch (this.opcion) {
            case Mapa.UBICAR:
                    title = "Localizando";
                    content = "Utilizando los servicios de georeferenciación para establecer tu ubicación.";
                    priority = Notification.PRIORITY_LOW;
                break;
            case Mapa.REGISTRAR:
                    title = "Registrando recorrido";
                    content = "Tiempo transcurrido " + tiempo;
                    priority = Notification.PRIORITY_HIGH;
                break;
        }

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(priority)
                .addAction(R.drawable.ic_launch, getString(R.string.launch_activity), activityPendingIntent);

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }

    private void onNewLocation(Location location) {
        Log.i(TAG, "New location: " + location);
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_ACTION, opcion);

        mLocation = location;
        switch (opcion)
        {
            case Mapa.UBICAR:
                    intent.putExtra(EXTRA_LOCATION, mLocation);
                break;
            case Mapa.REGISTRAR:
                /*if (registro_ruta.size() > 0) {
                    int i = 0;
                    for (Map.Entry reg : registro_ruta.entrySet()) {
                        i++;
                        Location history_location = (Location) reg.getValue();

                        if (i == registro_ruta.size()) {
                            if (history_location.distanceTo(mLocation) > 5 && location.getAccuracy() < 100) {
                                registro_ruta.put(tiempo, mLocation);
                            }
                        }
                    }
                } else {
                    registro_ruta.put(tiempo, mLocation);
                }*/
                //intent.putExtra(EXTRA_TIME, tiempo);
                //intent.putExtra(EXTRA_ROUTE, Utils.routeToString(registro_ruta));
                Log.i(TAG, registro_ruta.size()+": "+tiempo+" | "+location.toString());
                break;
        }

        // Notify anyone listening for broadcasts about the new location.
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        Log.i(TAG, "in serviceIsRunningInForeground()");
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }

        return false;
    }
}
