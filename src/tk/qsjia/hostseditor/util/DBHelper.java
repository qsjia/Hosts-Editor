package tk.qsjia.hostseditor.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DB_NAME = "hosts";
	public static final String REMOTE_TABLE_NAME = "remote_hosts";
	public static final String CUSTOM_TABLE_NAME = "custom_hosts";

	public DBHelper(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table " + REMOTE_TABLE_NAME + " (_id integer primary key autoincrement, " +
				" url text not null, local_file text, local_date integer, remote_date integer, used integer) ");
		db.execSQL("create table " + CUSTOM_TABLE_NAME + " (_id integer primary key autoincrement, " +
				" ip text not null, host text not null, used integer) ");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
