package tv.acfun.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class PersonService {
	private DBOpenHelper dbOpenHelper;
	
	public PersonService(Context context){
		dbOpenHelper = new DBOpenHelper(context);
	}
	
	public void save(String id){
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		//
	}
}
