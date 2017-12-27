package co.gov.idrd.ciclovia.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;

/**
 * Created by daniel on 12/12/17.
 */

public class Tabla {

    private String[][] campos;
    private String primarykey = "";
    private String nombre = "";
    private SQLiteDatabase db;
    private long id;

    public Tabla(String nombre) {
        this.nombre = nombre;
    }

    public Tabla setCampos(String[][] campos) {
        this.campos = campos;
        return this;
    }

    public Tabla setPrimaryKey(String primarykey) {
        this.primarykey = primarykey;
        return this;
    }

    public Tabla setDatabase(SQLiteDatabase db) {
        this.db = db;
        return this;
    }

    public String getCreateQuery() {
        String query = "CREATE TABLE "+nombre+" (";
        for(String[] campo : campos) {
            query += campo[0]+" "+campo[1]+", ";
        }
        query = query.substring(0, query.length() - 2) + ")";
        return query;
    }

    public long insertar(String[][] campos) throws NullPointerException{
        this.id = db.insert(this.nombre, null, this.make(campos));
        return this.id;
    }

    public long actualizar(String[][] campos, String where) throws NullPointerException{
        this.id = db.update(this.nombre, make(campos), where, null);
        return id;
    }

    public Cursor obtenerdatos() throws NullPointerException {
        String sql = "SELECT * FROM " + this.nombre + " ORDER BY " + this.primarykey + " ASC;";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }

    public Cursor rawQuery(String sql) {
        Cursor c = db.rawQuery(sql, null);
        return c;
    }

    public void close() {
        db.close();
    }

    private ContentValues make(String[][] campos) {
        ContentValues contentValues = new ContentValues();
        for(String[] campo : campos) {
            contentValues.put(campo[0], campo[1]);
        }
        return contentValues;
    }
}
