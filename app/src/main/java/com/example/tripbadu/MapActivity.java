package com.example.tripbadu;

import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapActivity extends AppCompatActivity {

    private MapView map = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Handle permissions and user agent configuration as required by OSM
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_map);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        // Enable zoom controls and multi-touch
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        // Set starting point: Pidurangala Rock, Sri Lanka
        GeoPoint startPoint = new GeoPoint(7.9649, 80.7618);
        map.getController().setZoom(15.0);
        map.getController().setCenter(startPoint);

        // Add a marker
        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("Pidurangala Trail Start");
        map.getOverlays().add(startMarker);
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume(); // Needed for osmdroid
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();  // Needed for osmdroid
    }
}
