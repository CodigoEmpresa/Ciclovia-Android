package co.gov.idrd.ciclovia.util;

/**
 * Created by daniel on 28/11/17.
 */

import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseManejador extends SQLiteOpenHelper {


    public static final String DB_NAME = "CicloviaDB";
    public static final String TABLE_NAME = "datos";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ID_USUARIO = "id_usuario";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_FECHA = "fecha";
    public static final String COLUMN_ALTURA = "altura";
    public static final String COLUMN_PESO = "peso";
    public static final String COLUMN_SEXO = "sexo";
    public static final String COLUMN_SINC = "sincronizado";



    private static final int DB_VERSION = 1;


    public DatabaseManejador(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME
                + "(" + COLUMN_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NOMBRE +" TEXT ,"
                + COLUMN_FECHA +" DATE ,"
                + COLUMN_ALTURA +" INT(10) ,"
                + COLUMN_PESO +" INT(10) ,"
                + COLUMN_SEXO +" INT(10) "
                + COLUMN_ID_USUARIO+" INT(10) "
                + COLUMN_SINC+" TINYINT);";
        db.execSQL(sql);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS "+TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }


    public boolean agregar_datos(String nombre ,String fecha ,int altura ,int peso ,long sexo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NOMBRE,nombre);
        contentValues.put(COLUMN_FECHA,fecha);
        contentValues.put(COLUMN_ALTURA,altura);
        contentValues.put(COLUMN_PESO,peso);
        contentValues.put(COLUMN_SEXO,sexo);
        db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }

    public boolean actualizarestado(int id, int estado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_SINC, estado);
        db.update(TABLE_NAME, contentValues, COLUMN_ID + "=" + id, null);
        db.close();
        return true;
    }


    public boolean actualizar_datos(int id ,String nombre ,String fecha ,int altura ,int peso ,long sexo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NOMBRE,nombre);
        contentValues.put(COLUMN_FECHA,fecha);
        contentValues.put(COLUMN_ALTURA,altura);
        contentValues.put(COLUMN_PESO,peso);
        contentValues.put(COLUMN_SEXO,sexo);
        db.update(TABLE_NAME, contentValues, COLUMN_ID + "=" + id, null);
        db.close();
        return true;
    }


    public Cursor obtenerdatos() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " ASC;";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }


    public Cursor obtener_nosincronizados() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_SINC + " = 0;";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }
}