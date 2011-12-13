package tv.acfun.db;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class DBService {

	private SQLiteDatabase db;
	private DBOpenHelper dbHelper;
	public  DBService(Context context){
		dbHelper = new DBOpenHelper(context);
		try
		{
		db = dbHelper.getWritableDatabase();
		}
		catch (SQLiteException ex)
		{
		db = dbHelper.getReadableDatabase();
		}
	}
	
	public void addtoFov(String id,String title,String time){
		db.execSQL("INSERT INTO FAVORITES(VIDEOID,TITLE,TIME)" +
				"VALUES(?,?,?)", new Object[]{id,title
				 ,time});
		db.close();
	}
	
	public boolean isFoved(String id){
		Cursor cursor = db.rawQuery("SELECT VIDEOID FROM FAVORITES WHERE VIDEOID = ?",new String[]{id});
		boolean isexist = cursor.moveToFirst();
		cursor.close();
		db.close();
		return isexist ;
	}
	
	public void addtoHis(String id,String title,String time){
		db.execSQL("INSERT INTO HISTORY(VIDEOID,TITLE,TIME)" +
				"VALUES(?,?,?)", new Object[]{id,title
				 ,time});
		db.close();
	}
	
	public void addtoSHis(String title){
		db.execSQL("INSERT INTO SEARCHHISTORY(TITLE)" +
				"VALUES(?,?,?)", new Object[]{title});
		db.close();
	}
	
	public void deltoSFov(String id){
		
	}
	
	public void cleanHis(){
		db.execSQL("DELETE FROM HISTORY");
		db.close();
	}
	
	public void cleanSHis(String id,String title){
		db.execSQL("DELETE FROM SEARCHHISTORY");
		db.close();
	}
	
	public ArrayList<HashMap<String, String>> getFovs(){
		ArrayList<HashMap<String, String>> fovs = new ArrayList<HashMap<String, String>>();
		Cursor cursor = db.rawQuery("SELECT * FROM FAVORITES ORDER BY _ID DESC",null);
		while(cursor.moveToNext()){
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("id", cursor.getString(cursor.getColumnIndex("VIDEOID")));
		map.put("title", cursor.getString(cursor.getColumnIndex("TITLE")));
		map.put("time", cursor.getString(cursor.getColumnIndex("TIME")));
		fovs.add(map);
		}
		cursor.close();
		db.close();

		return fovs;
	}
	
	public ArrayList<HashMap<String, String>> getHiss(){
		ArrayList<HashMap<String, String>> hiss = new ArrayList<HashMap<String, String>>();
		Cursor cursor = db.rawQuery("SELECT * FROM HISTORY ORDER BY _ID DESC",null);
		while(cursor.moveToNext()){
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("id", cursor.getString(cursor.getColumnIndex("VIDEOID")));
		map.put("title", cursor.getString(cursor.getColumnIndex("TITLE")));
		map.put("time", cursor.getString(cursor.getColumnIndex("TIME")));
		hiss.add(map);
		}
		cursor.close();
		db.close();

		return hiss;
		
	}
	public ArrayList<String> getSHiss(){
		ArrayList<String> shiss = new ArrayList<String>();
		Cursor cursor = db.rawQuery("SELECT * FROM SEARCHHISTORY",null);
		while(cursor.moveToNext()){
			shiss.add(cursor.getString(cursor.getColumnIndex("TITLE")));
		}
		cursor.close();
		db.close();
		return shiss;
	}
	
}
