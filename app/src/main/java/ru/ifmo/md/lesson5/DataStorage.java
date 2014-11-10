package ru.ifmo.md.lesson5;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Svet on 19.10.2014.
 */
public class DataStorage {

    DatabaseHelper db;
    SQLiteDatabase sql;

    DataStorage(Context c) {
        db = new DatabaseHelper(c, "database.db", null, 1);
    }

    public void updateInformation() {
        ArrayList<Resource> resources = readResourceData();
        ArrayList<Message> oldMessages = readRssData();
        for(Resource resource : resources) {
            try {
                String data = new LoadInfoTask().execute(resource.url).get();
                ArrayList<Message> messages = XMLParser.parse(resource.name, data);
                for(Message m : getChanges(oldMessages, messages)) {
                    putRssData(m);
                }
            }catch(ExecutionException e) {
                Log.i("ERROR", "ExecutionException exception");
            } catch(InterruptedException e) {
                Log.i("ERROR", "Interrupted exception");
            }
        }
    }

    private ArrayList<Message> getChanges(ArrayList<Message> ar1, ArrayList<Message> ar2) {
        ArrayList<Message> result = new ArrayList<Message>();
        for(Message i : ar2) {
            boolean c = true;
            for(Message j : ar1) {
                if(i.link.equals(j.link)) {
                    c = false;
                    break;
                }
            }
            if(c) result.add(i);
        }
        return result;
    }

    public void putRssData(Message m) {
        sql = db.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(db.TITLE_COLUM, m.title);
        values.put(db.DESCRIPTION_COLUM, m.description);
        values.put(db.SOURCE_NAME_COLUM, m.source);
        values.put(db.URL_COLUM, m.link);

        sql.insert(db.rssTable, null, values);

        db.close();
    }

    public void putSourceData(String name, String url) {
        sql = db.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(db.NAME_COLUM, name);
        values.put(db.URL_SOURCE_COLUM, url);

        sql.insert(db.urlTable, null, values);

        db.close();
    }

    public ArrayList<Message> readRssData() {
        sql = db.getWritableDatabase();

        Cursor cursor = sql.query(db.rssTable, new String[] {db.TITLE_COLUM, db.DESCRIPTION_COLUM, db.SOURCE_NAME_COLUM, db.URL_COLUM}, null, null, null, null, null);
        cursor.moveToFirst();
        ArrayList<Message> result = new ArrayList<Message>();
        while(!cursor.isAfterLast()) {
            String title = cursor.getString(cursor.getColumnIndex(db.TITLE_COLUM));
            String description = cursor.getString(cursor.getColumnIndex(db.DESCRIPTION_COLUM));
            String source = cursor.getString(cursor.getColumnIndex(db.SOURCE_NAME_COLUM));
            String url = cursor.getString(cursor.getColumnIndex(db.URL_COLUM));
            Message cur = new Message(title, description, url, source);
            result.add(cur);
            cursor.moveToNext();
        }
        cursor.close();

        db.close();
        return result;
    }

    public ArrayList<Resource> readResourceData() {
        sql = db.getWritableDatabase();

        Cursor cursor = sql.query(db.urlTable, new String[] {db.NAME_COLUM, db.URL_SOURCE_COLUM}, null, null, null, null, null);
        cursor.moveToFirst();
        ArrayList<Resource> res = new ArrayList<Resource>();
        while(!cursor.isAfterLast()) {
            String name = cursor.getString(cursor.getColumnIndex(db.NAME_COLUM));
            String url = cursor.getString(cursor.getColumnIndex(db.URL_SOURCE_COLUM));
            Resource r = new Resource(name, url);
            res.add(r);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return res;
    }

    public void deleteTables() {
        sql = db.getWritableDatabase();

        sql.delete(db.urlTable, null, null);
        sql.delete(db.rssTable, null, null);

        db.close();
    }

    public void deleteResource(String key) {
        sql = db.getWritableDatabase();

        sql.delete(db.urlTable, db.NAME_COLUM + " = '" + key + "'", null);
        sql.delete(db.rssTable, db.SOURCE_NAME_COLUM + " = '" + key + "'", null);

        sql.close();
    }
}
