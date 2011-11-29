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
	
	public void addtoHis(String id,String title){
		
	}
	
	public void addtoSHis(String id,String title){
		
	}
	
	public void deltoSFov(String id){
		
	}
	
	public void cleanHis(String id,String title){
		
	}
	
	public void cleanSHis(String id,String title){
		
	}
	
	public ArrayList<HashMap<String, String>> getFovs(){
		ArrayList<HashMap<String, String>> fovs = new ArrayList<HashMap<String, String>>();
		Cursor cursor = db.rawQuery("SELECT * FROM FAVORITES",null);
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
		return null;
		
	}
	public ArrayList<HashMap<String, String>> getSHiss(){
		return null;
		
	}
	
}
