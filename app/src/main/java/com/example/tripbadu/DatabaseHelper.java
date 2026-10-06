package com.example.tripbadu;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tripbadu.db";
    private static final int DATABASE_VERSION = 7;

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ROLE = "role";

    public static final String TABLE_CART = "cart";
    public static final String COLUMN_CART_ID = "cart_id";
    public static final String COLUMN_PROD_NAME = "prod_name";
    public static final String COLUMN_PROD_PRICE = "prod_price";
    public static final String COLUMN_PROD_IMAGE = "prod_image";

    public static final String TABLE_GEAR = "gear";
    public static final String COLUMN_GEAR_ID = "gear_id";
    public static final String COLUMN_GEAR_NAME = "gear_name";
    public static final String COLUMN_GEAR_PRICE = "gear_price";
    public static final String COLUMN_GEAR_IMAGE = "gear_image";
    public static final String COLUMN_GEAR_STATUS = "status"; 
    public static final String COLUMN_GEAR_LAT = "latitude";
    public static final String COLUMN_GEAR_LNG = "longitude";
    public static final String COLUMN_GEAR_CONTACT = "contact";
    public static final String COLUMN_GEAR_OWNER = "owner_email";

    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String COLUMN_NOTIF_ID = "notif_id";
    public static final String COLUMN_NOTIF_USER = "user_email";
    public static final String COLUMN_NOTIF_MSG = "message";
    public static final String COLUMN_NOTIF_READ = "is_read";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_ROLE + " TEXT DEFAULT 'User'" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_CART_TABLE = "CREATE TABLE " + TABLE_CART + "("
                + COLUMN_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PROD_NAME + " TEXT,"
                + COLUMN_PROD_PRICE + " REAL,"
                + COLUMN_PROD_IMAGE + " TEXT" + ")";
        db.execSQL(CREATE_CART_TABLE);

        String CREATE_GEAR_TABLE = "CREATE TABLE " + TABLE_GEAR + "("
                + COLUMN_GEAR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_GEAR_NAME + " TEXT,"
                + COLUMN_GEAR_PRICE + " REAL,"
                + COLUMN_GEAR_IMAGE + " TEXT,"
                + COLUMN_GEAR_STATUS + " TEXT DEFAULT 'approved',"
                + COLUMN_GEAR_LAT + " REAL,"
                + COLUMN_GEAR_LNG + " REAL,"
                + COLUMN_GEAR_CONTACT + " TEXT,"
                + COLUMN_GEAR_OWNER + " TEXT" + ")";
        db.execSQL(CREATE_GEAR_TABLE);

        String CREATE_NOTIF_TABLE = "CREATE TABLE " + TABLE_NOTIFICATIONS + "("
                + COLUMN_NOTIF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NOTIF_USER + " TEXT,"
                + COLUMN_NOTIF_MSG + " TEXT,"
                + COLUMN_NOTIF_READ + " INTEGER DEFAULT 0" + ")";
        db.execSQL(CREATE_NOTIF_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GEAR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        onCreate(db);
    }

    public static String hashPassword(String password) {
        if (password == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return password;
        }
    }

    public boolean registerUser(String email, String password, String name, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, hashPassword(password));
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_ROLE, role);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public Cursor getUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);
        String selection = COLUMN_EMAIL + " = ? AND (" + COLUMN_PASSWORD + " = ? OR " + COLUMN_PASSWORD + " = ?)";
        String[] selectionArgs = {email, hashedPassword, password};
        return db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
    }

    public boolean checkUser(String email, String password) {
        Cursor cursor = getUser(email, password);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
    }

    public void deleteUser(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void addGear(String name, double price, String image, String status, double lat, double lng, String contact, String ownerEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GEAR_NAME, name);
        values.put(COLUMN_GEAR_PRICE, price);
        values.put(COLUMN_GEAR_IMAGE, image);
        values.put(COLUMN_GEAR_STATUS, status);
        values.put(COLUMN_GEAR_LAT, lat);
        values.put(COLUMN_GEAR_LNG, lng);
        values.put(COLUMN_GEAR_CONTACT, contact);
        values.put(COLUMN_GEAR_OWNER, ownerEmail);
        db.insert(TABLE_GEAR, null, values);
    }

    public void updateGear(int id, String name, double price, String image, double lat, double lng, String contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GEAR_NAME, name);
        values.put(COLUMN_GEAR_PRICE, price);
        values.put(COLUMN_GEAR_IMAGE, image);
        values.put(COLUMN_GEAR_LAT, lat);
        values.put(COLUMN_GEAR_LNG, lng);
        values.put(COLUMN_GEAR_CONTACT, contact);
        db.update(TABLE_GEAR, values, COLUMN_GEAR_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public Cursor getAllGear() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_GEAR + " WHERE " + COLUMN_GEAR_STATUS + " = 'approved'", null);
    }

    public Cursor getPendingGear() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_GEAR + " WHERE " + COLUMN_GEAR_STATUS + " = 'pending'", null);
    }

    public void approveGear(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        String ownerEmail = "";
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_GEAR_OWNER + ", " + COLUMN_GEAR_NAME + " FROM " + TABLE_GEAR + " WHERE " + COLUMN_GEAR_ID + " = ?", new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            ownerEmail = cursor.getString(0);
            String gearName = cursor.getString(1);
            if (ownerEmail != null && !ownerEmail.isEmpty()) {
                addNotification(ownerEmail, "Your ad for '" + gearName + "' has been approved!");
            }
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COLUMN_GEAR_STATUS, "approved");
        db.update(TABLE_GEAR, values, COLUMN_GEAR_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void rejectGear(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        String ownerEmail = "";
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_GEAR_OWNER + ", " + COLUMN_GEAR_NAME + " FROM " + TABLE_GEAR + " WHERE " + COLUMN_GEAR_ID + " = ?", new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            ownerEmail = cursor.getString(0);
            String gearName = cursor.getString(1);
            if (ownerEmail != null && !ownerEmail.isEmpty()) {
                addNotification(ownerEmail, "Your ad for '" + gearName + "' was rejected by the administrator.");
            }
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COLUMN_GEAR_STATUS, "rejected");
        db.update(TABLE_GEAR, values, COLUMN_GEAR_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deleteGear(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GEAR, COLUMN_GEAR_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void addNotification(String email, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIF_USER, email);
        values.put(COLUMN_NOTIF_MSG, message);
        db.insert(TABLE_NOTIFICATIONS, null, values);
    }

    public Cursor getNotifications(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NOTIFICATIONS + " WHERE " + COLUMN_NOTIF_USER + " = ? ORDER BY " + COLUMN_NOTIF_ID + " DESC", new String[]{email});
    }

    public void markNotificationsAsRead(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIF_READ, 1);
        db.update(TABLE_NOTIFICATIONS, values, COLUMN_NOTIF_USER + " = ?", new String[]{email});
    }

    public void addToCart(String name, double price, String image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROD_NAME, name);
        values.put(COLUMN_PROD_PRICE, price);
        values.put(COLUMN_PROD_IMAGE, image);
        db.insert(TABLE_CART, null, values);
    }

    public Cursor getCartItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_CART, null);
    }

    public void clearCart() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_CART);
    }

    public void deleteCartItem(int cartId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, COLUMN_CART_ID + " = ?", new String[]{String.valueOf(cartId)});
    }
}
