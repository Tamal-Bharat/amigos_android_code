package com.amigos.attendance.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqliteDatabaseHelper extends SQLiteOpenHelper {

    Context context;

    private static final String DATABASE_NAME = ApplicationConstants.SQLITE_DATABASE_NAME;
    private static final int DATABASE_VERSION = ApplicationConstants.SQLITE_DATABASE_VERSION;

    public SqliteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        System.out.println("AAAA--> " + "onCreate called");

        String createPermissionDeniedTableSql =
                "CREATE TABLE " + ApplicationConstants.PERMISSION_DENIED_TABLE + " (" +
                        ApplicationConstants.PERMISSION_DENIED_TABLE_ROW_SL_NO + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ApplicationConstants.PERMISSION_DENIED_TABLE_ROW_PERMISSION_NAME + " TEXT NOT NULL, " +
                        ApplicationConstants.PERMISSION_DENIED_TABLE_ROW_PERMISSION_DENIED_COUNT + " INTEGER " +
                        ")";

        sqLiteDatabase.execSQL(createPermissionDeniedTableSql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ApplicationConstants.PERMISSION_DENIED_TABLE);
        onCreate(sqLiteDatabase);
    }

    public boolean recordPermissionDeniedCount(SqliteDatabaseHelper databaseHelper, String permissionName){

        System.out.println("AAAA--> " + "inside recordPermissionDeniedCount");

        boolean isOpenSettings = false;
        try{
            SQLiteDatabase readDb = databaseHelper.getReadableDatabase();
            SQLiteDatabase writeDb = databaseHelper.getWritableDatabase();

            String permissionDeniedQuery = "SELECT * FROM " +
                                ApplicationConstants.PERMISSION_DENIED_TABLE +
                                " WHERE " +
                                ApplicationConstants.PERMISSION_DENIED_TABLE_ROW_PERMISSION_NAME +
                                " = '" +
                                permissionName +
                                "'" ;

            Cursor cursor_read_table = readDb.rawQuery(permissionDeniedQuery, null);

            if (cursor_read_table.moveToFirst()){
                System.out.println("AAAA--> " + "Row Value: " + cursor_read_table.getInt(2));
                int deniedCount = cursor_read_table.getInt(2);
                System.out.println("AAAA--> " + "deniedCount Value: " + deniedCount);

                if(deniedCount < 2){
                    deniedCount = deniedCount + 1;
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(ApplicationConstants.PERMISSION_DENIED_TABLE_ROW_PERMISSION_DENIED_COUNT, deniedCount);
                    writeDb.update(ApplicationConstants.PERMISSION_DENIED_TABLE,  contentValues, ApplicationConstants.PERMISSION_DENIED_TABLE_ROW_PERMISSION_NAME + " = ? ", new String[]{permissionName});
                }
                else{
                    isOpenSettings = true;
                }

            }
            else{
                System.out.println("AAAA--> " + "getCount 0");
                ContentValues contentValues = new ContentValues();
                contentValues.put(ApplicationConstants.PERMISSION_DENIED_TABLE_ROW_PERMISSION_NAME, permissionName);
                contentValues.put(ApplicationConstants.PERMISSION_DENIED_TABLE_ROW_PERMISSION_DENIED_COUNT, 1);
                writeDb.insert(ApplicationConstants.PERMISSION_DENIED_TABLE, null, contentValues);
            }

        }
        catch(Exception e){
            //e.printStackTrace();
            new DialogUtility().getMeterialDialog(context, "Exception", e.getMessage(), "error");
            System.out.println("AAAA--> " + "Exception: " + e.getMessage());
        }

        return isOpenSettings;

    }
}
