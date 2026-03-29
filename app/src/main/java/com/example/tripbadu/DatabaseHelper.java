package com.example.tripbadu;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tripbadu.db";
    private static final int DATABASE_VERSION = 3; // Incremented version for P2P expansion

    // User table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ROLE = "role"; // User, VIP, Admin

    // Cart table
    public static final String TABLE_CART = "cart";
    public static final String COLUMN_CART_ID = "cart_id";
    public static final String COLUMN_PROD_NAME = "prod_name";
    public static final String COLUMN_PROD_PRICE = "prod_price";
    public static final String COLUMN_PROD_IMAGE = "prod_image";

    // Gear table (Local cache/Admin)
    public static final String TABLE_GEAR = "gear";
    public static final String COLUMN_GEAR_ID = "gear_id";
    public static final String COLUMN_GEAR_NAME = "gear_name";
    public static final String COLUMN_GEAR_PRICE = "gear_price";
    public static final String COLUMN_GEAR_IMAGE = "gear_image";
    public static final String COLUMN_GEAR_STATUS = "status"; // pending, approved

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
                + COLUMN_GEAR_STATUS + " TEXT DEFAULT 'approved'" + ")";
        db.execSQL(CREATE_GEAR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GEAR);
        onCreate(db);
    }

    public boolean registerUser(String email, String password, String name, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_ROLE, role);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public Cursor getUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_EMAIL + " = ?" + " AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};
        return db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
    }

    public boolean checkUser(String email, String password) {
        Cursor cursor = getUser(email, password);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    // Admin Methods
    public void addGear(String name, double price, String image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GEAR_NAME, name);
        values.put(COLUMN_GEAR_PRICE, price);
        values.put(COLUMN_GEAR_IMAGE, image);
        values.put(COLUMN_GEAR_STATUS, "approved");
        db.insert(TABLE_GEAR, null, values);
    }

    public Cursor getAllGear() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_GEAR, null);
    }

    // Cart Methods
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
}
