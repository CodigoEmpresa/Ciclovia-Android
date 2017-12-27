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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import co.gov.idrd.ciclovia.Datos;


public class DatabaseManager extends SQLiteOpenHelper {

    public static final String DB_NAME = "CicloviaDB";
    public static final String TABLE_NAME = "datos";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_FECHA = "fecha";
    public static final String COLUMN_ALTURA = "altura";
    public static final String COLUMN_PESO = "peso";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_SEXO = "sexo";
    public static final String COLUMN_SINC = "sincronizado";

    public static final String TABLA_RUTAS = "rutas";
    public static final String TABLA_PUNTOS_RUTA = "puntos";
    private HashMap<String, Tabla> tablas = new HashMap<String, Tabla>();
    private static final int DB_VERSION = 1;


    public DatabaseManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Tabla datos = new Tabla("datos");
        Tabla rutas = new Tabla("rutas");
        Tabla puntos = new Tabla("puntos");

        datos.setCampos(new String[][]{
                {"id", "INTEGER PRIMARY KEY AUTOINCREMENT"},
                {"nombre", "text"},
                {"fecha", "date"},
                {"altura", "int(10)"},
                {"peso", "int(10)"},
                {"email", "text"},
                {"sexo", "int(10)"},
                {"sincronizado", "int(10)"}
             })
             .setPrimaryKey("id");

        rutas.setCampos(new String[][]{
                {"id", "INTEGER PRIMARY KEY AUTOINCREMENT"},
                {"creacion", "DATETIME DEFAULT CURRENT_TIMESTAMP"},
                {"medio", "text"},
                {"finalizado", "int(10)"},
                {"sincronizado", "int(10)"}
            })
            .setPrimaryKey("id");

        puntos.setCampos(new String[][]{
                {"id", "INTEGER PRIMARY KEY AUTOINCREMENT"},
                {"id_ruta", "int(10)"},
                {"tiempo", "int(10)"},
                {"hora", "string"},
                {"latitud", "string"},
                {"longitud", "string"},
                {"sincronizado", "int(10)"}
             })
             .setPrimaryKey("id");

        tablas.put("datos", datos);
        tablas.put("rutas", rutas);
        tablas.put("puntos", puntos);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Map.Entry kv : tablas.entrySet())
        {
            Tabla tabla = (Tabla)kv.getValue();
            tabla.setDatabase(db);
            db.execSQL(tabla.getCreateQuery());
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS "+TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }

    public Tabla getTabla(String nombre) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (Map.Entry kv : tablas.entrySet())
        {
            Tabla tabla = (Tabla)kv.getValue();
            tabla.setDatabase(db);
        }

        return tablas.get(nombre);
    }

    public boolean agregar_datos(String nombre ,String fecha ,int altura ,int peso ,long sexo,String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NOMBRE,nombre);
        contentValues.put(COLUMN_FECHA,fecha);
        contentValues.put(COLUMN_ALTURA,altura);
        contentValues.put(COLUMN_PESO,peso);
        contentValues.put(COLUMN_SEXO,sexo);
        contentValues.put(COLUMN_EMAIL,email);
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


    public boolean actualizar_datos(String email ,String nombre ,String fecha ,int altura ,int peso ,long sexo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NOMBRE,nombre);
        contentValues.put(COLUMN_FECHA,fecha);
        contentValues.put(COLUMN_ALTURA,altura);
        contentValues.put(COLUMN_PESO,peso);
        contentValues.put(COLUMN_SEXO,sexo);
        db.update(TABLE_NAME, contentValues, COLUMN_EMAIL + "= \"" + email+"\"", null);
        db.close();
        return true;
    }


    public Cursor obtenerdatos_persona(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE "+COLUMN_EMAIL+" = \""+email+"\" ORDER BY " + COLUMN_ID + " ASC;";
        Cursor c = db.rawQuery(sql, null);
        return c;
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