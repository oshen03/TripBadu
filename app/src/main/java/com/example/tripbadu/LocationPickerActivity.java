package com.example.tripbadu;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import java.io.File;

public class LocationPickerActivity extends AppCompatActivity {

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
    private Marker selectedMarker = null;
    private Button btnConfirm;
    private double selectedLat = 0, selectedLng = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OpenStreetMap blocks any user agent with "com.example" or generic defaults.
        // Set a distinctive User-Agent BEFORE loading configuration.
        Configuration.getInstance().setUserAgentValue("TripBaduTravelApp/1.0 (Android; SriLanka Travel Rental; contact@tripbadu.lk)");
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        clearTileCache();
        
        setContentView(R.layout.activity_location_picker);

        map = findViewById(R.id.mapPicker);
        map.getTileProvider().clearTileCache();
        btnConfirm = findViewById(R.id.btnConfirmLocation);

        map.setTileSource(OSM_HOT);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        GeoPoint startPoint = new GeoPoint(7.8731, 80.7718); // Center of Sri Lanka
        map.getController().setZoom(8.0);
        map.getController().setCenter(startPoint);

        MapEventsReceiver receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                if (selectedMarker != null) {
                    map.getOverlays().remove(selectedMarker);
                }
                selectedMarker = new Marker(map);
                selectedMarker.setPosition(p);
                selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                selectedMarker.setTitle("Selected Location");
                map.getOverlays().add(selectedMarker);
                map.invalidate();

                selectedLat = p.getLatitude();
                selectedLng = p.getLongitude();
                btnConfirm.setVisibility(View.VISIBLE);
                return true;
            }
        };

        MapEventsOverlay eventsOverlay = new MapEventsOverlay(receiver);
        map.getOverlays().add(eventsOverlay);

        btnConfirm.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("lat", selectedLat);
            resultIntent.putExtra("lng", selectedLng);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
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
