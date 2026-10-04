package com.example.tripbadu;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    public static final OnlineTileSourceBase OSM_HOT = new XYTileSource(
            "OpenStreetMapHot",
            0,
            19,
            256,
            ".png",
            new String[]{
                    "https://a.tile.openstreetmap.fr/hot/",
                    "https://b.tile.openstreetmap.fr/hot/",
                    "https://c.tile.openstreetmap.fr/hot/"
            }
    );

    private MapView map = null;
    private DatabaseHelper dbHelper;
    private MyLocationNewOverlay mLocationOverlay;
    private CompassOverlay mCompassOverlay;
    private ScaleBarOverlay mScaleBarOverlay;
    
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DatabaseHelper(this);
        
        // OpenStreetMap blocks any user agent with "com.example" or generic defaults.
        // Set a distinctive User-Agent BEFORE loading configuration.
        Configuration.getInstance().setUserAgentValue("TripBaduTravelApp/1.0 (Android; SriLanka Travel Rental; contact@tripbadu.lk)");
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        // Purge old cached 403 error tiles from disk so fresh tiles are downloaded
        clearTileCache();

        setContentView(R.layout.activity_map);

        map = findViewById(R.id.map);
        map.getTileProvider().clearTileCache();
        map.setTileSource(OSM_HOT);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        // Add Scale Bar
        mScaleBarOverlay = new ScaleBarOverlay(map);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setScaleBarOffset(getResources().getDisplayMetrics().widthPixels / 2, 10);
        map.getOverlays().add(mScaleBarOverlay);

        // Add Compass
        mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), map);
        mCompassOverlay.enableCompass();
        map.getOverlays().add(mCompassOverlay);

        // Default center: Pidurangala
        GeoPoint startPoint = new GeoPoint(7.9649, 80.7618);
        map.getController().setZoom(10.0);
        map.getController().setCenter(startPoint);

        loadGearMarkers();
        
        requestLocationPermissions();
    }
    
    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableMyLocation();
        }
    }

    private void enableMyLocation() {
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.enableFollowLocation(); // zoom to user
        map.getOverlays().add(mLocationOverlay);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadGearMarkers() {
        Cursor cursor = dbHelper.getAllGear();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_PRICE));
                String image = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_IMAGE));
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_LAT));
                double lng = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_LNG));
                String contact = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GEAR_CONTACT));

                if (lat != 0 && lng != 0) {
                    Marker marker = new Marker(map);
                    marker.setPosition(new GeoPoint(lat, lng));
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    marker.setTitle(name);
                    
                    String snippet = "LKR " + price;
                    if (contact != null && !contact.isEmpty()) {
                        snippet += "\nContact: " + contact;
                    }
                    marker.setSnippet(snippet);
                    
                    // Simple InfoWindow setup via setOnMarkerClickListener
                    marker.setOnMarkerClickListener((m, mapView) -> {
                        m.showInfoWindow();
                        mapView.getController().animateTo(m.getPosition());
                        return true;
                    });
                    
                    map.getOverlays().add(marker);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        map.invalidate(); // Refresh map
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
        if (mLocationOverlay != null) {
            mLocationOverlay.enableMyLocation();
        }
        if (mCompassOverlay != null) {
            mCompassOverlay.enableCompass();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
        if (mLocationOverlay != null) {
            mLocationOverlay.disableMyLocation();
        }
        if (mCompassOverlay != null) {
            mCompassOverlay.disableCompass();
        }
    }

    private void clearTileCache() {
        try {
            File tileCache = Configuration.getInstance().getOsmdroidTileCache();
            if (tileCache != null && tileCache.exists()) {
                deleteDir(tileCache);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}
