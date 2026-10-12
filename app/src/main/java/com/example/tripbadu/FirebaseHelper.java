package com.example.tripbadu;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FirebaseHelper {

    private static FirebaseHelper instance;
    private final FirebaseDatabase database;
    private final DatabaseReference rootRef;

    // Node names in Firebase
    public static final String NODE_USERS = "users";
    public static final String NODE_GEAR = "gear";
    public static final String NODE_NOTIFICATIONS = "notifications";
    // Database URL for Singapore region (asia-southeast1)
    public static final String DATABASE_URL = "https://tripbadu-2026-default-rtdb.asia-southeast1.firebasedatabase.app";

    private FirebaseHelper() {
        database = FirebaseDatabase.getInstance(DATABASE_URL);
        rootRef = database.getReference();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public DatabaseReference getRootRef() {
        return rootRef;
    }

    public DatabaseReference getUsersRef() {
        return rootRef.child(NODE_USERS);
    }

    public DatabaseReference getGearRef() {
        return rootRef.child(NODE_GEAR);
    }

    public DatabaseReference getNotificationsRef() {
        return rootRef.child(NODE_NOTIFICATIONS);
    }

    // Firebase keys cannot contain '.' so we replace it with ','
    public static String encodeEmail(String email) {
        if (email == null) return "";
        return email.replace(".", ",");
    }

    public static String decodeEmail(String key) {
        if (key == null) return "";
        return key.replace(",", ".");
    }

    // Save user to cloud database
    public void saveUser(String name, String email, String role, String passwordHash, DatabaseReference.CompletionListener listener) {
        String key = encodeEmail(email);
        CloudUser user = new CloudUser(name, email, role, passwordHash);
        getUsersRef().child(key).setValue(user, listener);
    }

    // Fetch user from cloud database by email
    public void getUser(String email, ValueEventListener listener) {
        String key = encodeEmail(email);
        getUsersRef().child(key).addListenerForSingleValueEvent(listener);
    }

    // Query for only approved gear to display in marketplace
    public Query getApprovedGearQuery() {
        return getGearRef().orderByChild("status").equalTo("approved");
    }

    // Query for pending ads to display in Admin approval screen
    public Query getPendingGearQuery() {
        return getGearRef().orderByChild("status").equalTo("pending");
    }

    // Query for ads belonging to a specific VIP owner
    public Query getMyAdsQuery(String ownerEmail) {
        return getGearRef().orderByChild("ownerEmail").equalTo(ownerEmail);
    }

    // Add or update gear in Firebase
    public void saveGear(Gear gear, DatabaseReference.CompletionListener listener) {
        String key = (gear.getId() > 0) ? String.valueOf(gear.getId()) : getGearRef().push().getKey();
        if (key != null) {
            getGearRef().child(key).setValue(gear, listener);
        }
    }

    // Update gear status (approved, rejected, pending)
    public void updateGearStatus(int gearId, String status) {
        getGearRef().child(String.valueOf(gearId)).child("status").setValue(status);
    }

    // Delete gear from Firebase
    public void deleteGear(int gearId) {
        getGearRef().child(String.valueOf(gearId)).removeValue();
    }
}
