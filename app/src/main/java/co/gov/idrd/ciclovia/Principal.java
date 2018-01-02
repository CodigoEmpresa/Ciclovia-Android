package co.gov.idrd.ciclovia;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import co.gov.idrd.ciclovia.services.LocationRequestProvider;
import co.gov.idrd.ciclovia.services.LocationService;
import co.gov.idrd.ciclovia.services.Utils;
import co.gov.idrd.ciclovia.util.DatabaseManager;
import co.gov.idrd.ciclovia.services.OnLocationHandler;
import co.gov.idrd.ciclovia.util.Preferencias;
import co.gov.idrd.ciclovia.util.Tabla;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

public class Principal extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = Principal.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";

    private Fragment fragment = null;
    private NavigationView nav;
    private SettingsClient mSettingsClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationRequest mLocationRequest;
    private LocationManager locationManager;
    private Location mCurrentLocation;
    private Boolean mRequestingLocationUpdates;
    private String mLastUpdateTime;
    private TextView nombre;
    private Toolbar toolbar;

    private OnLocationHandler locationHandler;
    private long id_ruta = 0;
    private int opcion = 0;
    private String time = "";
    private String medio_de_transporte = "";
    private LinkedHashMap<String, Location> route = new LinkedHashMap<String, Location>();

    private DatabaseManager db;
    private Tabla puntos;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;
    private boolean fromNotification = false;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    public Principal() { }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseManager db = new DatabaseManager(this);
        setContentView(R.layout.activity_principal);
        configureGui();

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        mSettingsClient = LocationServices.getSettingsClient(this);
        mLocationRequest = LocationRequestProvider.get();
        buildLocationSettingsRequest();
        myReceiver = new MyReceiver();
        db = new DatabaseManager(this);
        puntos = db.getTabla(DatabaseManager.TABLA_PUNTOS_RUTA);
        fromNotification = getIntent().getBooleanExtra(LocationService.EXTRA_STARTED_FROM_NOTIFICATION,
                false);

        Log.i(TAG, "onCreate() extras");

        if (getIntent().hasExtra(LocationService.EXTRA_ROUTE)) {
            Log.i(TAG, "onCreate() Has EXTRA_ROUTE");

            id_ruta = getIntent().getLongExtra(LocationService.EXTRA_ROUTE, 0);

            if (id_ruta > 0) {
                route.clear();

                Cursor c = puntos.rawQuery("SELECT * FROM puntos WHERE id_ruta = '"+id_ruta+"' ORDER BY id ASC");
                if (c.moveToFirst()) {
                    int i = 0;
                    do {
                        double latitude = Double.parseDouble(c.getString(c.getColumnIndex("latitud")));
                        double longitude = Double.parseDouble(c.getString(c.getColumnIndex("longitud")));
                        Location temp = new Location("temp_"+i);
                        temp.setLatitude(latitude);
                        temp.setLongitude(longitude);
                        route.put(c.getString(c.getColumnIndex("tiempo")), temp);
                        time = c.getString(c.getColumnIndex("tiempo"));
                        i++;
                    } while(c.moveToNext());
                }
            }

            Log.i(TAG, "onCreate() "+route.size());
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");
        bindService(new Intent(this, LocationService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause()");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }

        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        break;
                }
                break;
        }

        updateUIMap();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates");
                    startLocationUpdates();
                }
            } else {
                // Permission denied.
                showSnackbar(R.string.location_permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }

        updateUIMap();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        displaySelectedScreen(item.getItemId());

        return true;
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    public void startUpdatesHandler(OnLocationHandler handler, int opcion, String medio_de_transporte) {
        if (!mRequestingLocationUpdates) {
            this.locationHandler = handler;
            this.opcion = opcion;
            this.medio_de_transporte = medio_de_transporte;
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.

        Log.i(TAG, "in startLocationUpdates()");
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        //noinspection MissingPermission
                        /*if (ActivityCompat.checkSelfPermission(Principal.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Principal.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }*/
                        //mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        Log.i(TAG, "in startLocationUpdates() - onSuccess()");
                        mRequestingLocationUpdates = true;
                        if (locationHandler != null) {
                            locationHandler.onStart();
                            mService.requestLocationUpdates(Principal.this.opcion, Principal.this.medio_de_transporte);
                            updateUIMap();
                        }

                }
            })
            .addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                    "location settings ");
                            try {
                                // Show the dialog by calling startResolutionForResult(), and check the
                                // result in onActivityResult().
                                ResolvableApiException rae = (ResolvableApiException) e;
                                rae.startResolutionForResult(Principal.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException sie) {
                                Log.i(TAG, "PendingIntent unable to execute request.");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                Log.i(TAG, ex.getMessage());
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            String errorMessage = "Location settings are inadequate, and cannot be " +
                                    "fixed here. Fix in Settings.";
                            Log.e(TAG, errorMessage);
                            Toast.makeText(Principal.this, errorMessage, Toast.LENGTH_LONG).show();
                            mRequestingLocationUpdates = false;
                    }

                    if(locationHandler != null) {
                        locationHandler.onFail();
                    }
                }
            });
    }

    public void stopUpdatesHandler(int opcion) {
        Log.i(TAG, "stopUpdatesHandler()");
        locationHandler = null;
        stopLocationUpdates(opcion);
    }

    private void stopLocationUpdates(int opcion) {
        Log.i(TAG, "---------------stopLocationUpdates()");
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            //return;
        }

        mRequestingLocationUpdates = false;
        mService.removeLocationUpdates(opcion);
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar snackbar;

        snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_LONG);

        snackbar.getView().setBackgroundColor(Color.rgb(255,255,255));
        ((TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.rgb(51, 51, 51));

        snackbar.setAction(getString(actionStringId), listener).show();
    }

    public boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(Principal.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Principal.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.location_permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(Principal.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(Principal.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    public boolean checkLocation() {
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void restoreFromNotification() {
        if (fromNotification) {
            Log.i(TAG, "restoreFromNotification() true");
            Mapa mapa = getActiveMap();

            if (mapa != null) {
                mapa.updateFragmentFromRoute(time, id_ruta, route);
                Log.i(TAG, "Principal restoreFromNotification()"+time+" / "+id_ruta+" / "+route.size());
                fromNotification = false;
            } else {
                Log.i(TAG, "mapa = null");
            }
        }
    }

    private void displaySelectedScreen(int id) {

        //initializing the fragment object which is selected
        switch (id)
        {
            case R.id.nav_mapa:
                fragment = new Mapa();
                break;
            case R.id.nav_perfil:
                fragment = new Perfil();
                break;
        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void updateUIMap(){
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (f != null && f.isVisible()) {
            if(f instanceof Mapa) {
                ((Mapa)f).updateUI();
            }
        }
    }

    @Nullable
    private Mapa getActiveMap() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_frame);

        if (f != null && f.isVisible()) {
            if (f instanceof Mapa) {
                return (Mapa) f;
            }
        }

        return null;
    }

    private void configureGui() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        nav = (NavigationView) findViewById(R.id.nav_view);
        nombre = (TextView) nav.getHeaderView(0).findViewById(R.id.nombreUsuario);
        setSupportActionBar(toolbar);
        
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        displaySelectedScreen(R.id.nav_mapa);

        updateMenuLabels();
    }

    public void updateMenuLabels() {
        String username = Preferencias.getUsername(Principal.this);
        if (username != "")
            nombre.setText(username);
        else
            nombre.setText("Usuario");
    }

    /**
     * Receiver for broadcasts sent by {@link LocationService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Mapa mapa = getActiveMap();
            int opcion = intent.getIntExtra(LocationService.EXTRA_ACTION, 0);

            if (intent.hasExtra(LocationService.EXTRA_TIME)) {
                if (mapa != null) {
                    time = intent.getStringExtra(LocationService.EXTRA_TIME);
                    mapa.onRouteChangeTime(time);
                }
            }

            restoreFromNotification();
            Log.i(TAG, "MyReceiver onReceive() con opción: "+opcion);

            switch (opcion) {
                case Mapa.UBICAR:
                    Log.i(TAG, "Mapa.UBICAR");
                    Location location = intent.getParcelableExtra(LocationService.EXTRA_LOCATION);
                    if (location != null) {
                        mCurrentLocation = location;
                        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_frame);
                        if (f != null && f.isVisible()) {
                            if (f instanceof Mapa) {
                                ((Mapa) f).onLocationChange(mCurrentLocation, opcion);
                            }
                        }
                    }
                break;
                case Mapa.REGISTRAR:
                    Log.i(TAG, "Mapa.REGISTRAR");
                    id_ruta = intent.getLongExtra(LocationService.EXTRA_ROUTE, 0);
                    if (id_ruta > 0) {
                        route.clear();

                        Cursor c = puntos.rawQuery("SELECT * FROM puntos WHERE id_ruta = '"+id_ruta+"' ORDER BY id ASC");
                        if (c.moveToFirst()) {
                            int i = 0;
                            do {
                                double latitude = Double.parseDouble(c.getString(c.getColumnIndex("latitud")));
                                double longitude = Double.parseDouble(c.getString(c.getColumnIndex("longitud")));
                                Location temp = new Location("temp_"+i);
                                temp.setLatitude(latitude);
                                temp.setLongitude(longitude);
                                route.put(c.getString(c.getColumnIndex("tiempo")), temp);
                                i++;
                            } while(c.moveToNext());
                        }
                    }

                    if (route != null) {
                        if (mapa != null) {
                            mapa.onRouteChange(route);
                        }
                    }
                break;
            }
        }
    }
}
