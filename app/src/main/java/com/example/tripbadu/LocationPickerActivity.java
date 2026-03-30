package com.example.tripbadu;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

public class LocationPickerActivity extends AppCompatActivity {

    private MapView map = null;
    private Marker selectedMarker = null;
    private Button btnConfirm;
    private double selectedLat = 0, selectedLng = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_location_picker);

        map = findViewById(R.id.mapPicker);
        btnConfirm = findViewById(R.id.btnConfirmLocation);

        map.setTileSource(TileSourceFactory.MAPNIK);
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
}
