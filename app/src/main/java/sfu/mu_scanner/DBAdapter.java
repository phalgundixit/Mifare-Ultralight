package sfu.mu_scanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

/*import org.apache.commons.io.FileUtils;*/

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;




public class DBAdapter {

    private static final String TAG = "DBAdapter";

    private static final String DATABASE_NAME = "test.db";
    private static final int DATABASE_VERSION = 1;
    private static boolean Debug = false;
    String DB_PATH = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + "test" + "/";
    private static String DB_NAME = "test.db";

    public static Context context;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;
    public static String StrDeviceStorePath = "test";
    public static String BackUpfilename = "test.db";
    public static String StrFolderName = "NFC";

    public DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        //this oncreate is works on first time apk creation time
        public void onCreate(SQLiteDatabase db) {
            try {
                String sql = String.format("CREATE TABLE TBLNFCTESTDATA(NFCDATA_ID INTERGER PRIMARY KEY autoincrement,NFC_ID INTERGER,PASSWORD TEXT,CONTENT TEXT, CRAEATED_DATE_TIME DATE DEFAULT (datetime('now','localtime')), EXCEPTION TEXT)","TBLNFCTESTDATA");
                db.execSQL(sql);

                sql = String.format("Create Table TBLERRORLOG(_id integer primary key autoincrement," +
                                "ERR_PAGE_NAME TEXT," +
                                "ERR_FUNCTION TEXT" +
                                ",ERR_MESSAGE TEXT," +
                                "ERR_CAUSE TEXT," +
                                "ERR_DEVICE_ID TEXT," +
                                "ERR_FLAG INTEGER DEFAULT 0," +
                                "ERR_STACKTRACE varchar(5000)," +
                                "ERR_CRON DATE DEFAULT (datetime('now','localtime'))) ",
                        "TBLERRORLOG");
                db.execSQL(sql);

                if (Debug) {
                    Log.d(TAG, "onCreate Called.");
                }
            } catch (SQLException e) {
                //Error_log.logError(context, "LoginActivity", "oncreate", e);

            }

        }
        //when version is change Onupgrade part will working
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            int upgradeTo = oldVersion + 1;//1.5


            try {

                while (upgradeTo <= newVersion) {//1.5=1.5
                    switch (upgradeTo) {

                    }
                    upgradeTo++;


                }
            } catch (SQLException e) {
                Error_log.logError(context, "DBAdapter", "onUpgrade", e);

            }
        }
    }

    public String GetValuebyQuery(String qry) {
        String strValue = "";
        try {

            Cursor c = db.rawQuery(qry, null);
            if (c.moveToFirst()) {
                do {
                    strValue = c.getString(0);
                    if (strValue == null) {
                        strValue = "0";
                    } else if (strValue.equals("")) {
                        strValue = "0";
                    }
                } while (c.moveToNext());
            }
            c.close();

        } catch (Exception e) {
            Error_log.logError(context, "DBAdapter", "GetValuebyQuery", e);
            throw e;


        }
        return strValue;

    }

    public void beginTransaction() {
        db.beginTransaction();
    }

    public void CommittTransaction() {
        db.setTransactionSuccessful();
    }

    public void EndTransaction() {
        db.endTransaction();
    }


    public Cursor query(String query) {
        open();
        Cursor cursor = db.rawQuery(query, null);
        if (Debug) {
            Log.d(TAG, "Executing Query: " + query);
        }
        return cursor;
    }

    //Insert data into local DB for using column name and tablename
    public void Insert(ContentValues Values, String TblName) {
        try {
            db.insertOrThrow(TblName, null, Values);
        } catch (SQLException e) {
            Error_log.logError(context, "DBAdapter", "Insert", e);
            throw e;
        }


    }


    public String GetValueByQuery(String strQuery) {
        String strValue = "";
        Cursor c = db.rawQuery(strQuery, null);
        while (c.moveToNext()) {
            if (!c.isNull(0)) {
                strValue = c.getString(0).toString();
            }

        }
        return strValue;
    }


    //executes the sql statement statement=db.execSQL(sql); and tablename
    public String executeSqlStatement(String Statement, String strTableName) {
        try {
            String strCount = "";
            db.execSQL(Statement);
            Cursor curCount = query("SELECT changes() FROM " + strTableName);
            while (curCount.moveToNext()) {
                strCount = curCount.getString(0);
            }
            return strCount;

        } catch (SQLException e) {
            Error_log.logError(context, "DBAdapter", "executeSqlStatement", e);
            throw e;
        }


    }

    //delete the table using tablename
    public void Delete(String TblName) {

        try {
            db.delete(TblName, null, null);
        } catch (SQLException e) {
            Error_log.logError(context, "DBAdapter", "Delete", e);
            throw e;

        }
    }


    //---opens the database---
    //dbadapter open
    public DBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---
    public void close() {
        DBHelper.close();
    }

    public static Boolean copyFdToFile(Context activity) throws IOException
    {
        String filename = BackUpfilename;
        String PATH = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator +
                StrDeviceStorePath + "/";
        File file = new File(PATH);
        if (!file.exists()) {
            file.mkdir();
        }
        String PATH1 = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator +
                StrFolderName + "/";

        File filed = new File(PATH1);
        if (!filed.exists()) {
            filed.mkdir();
        }
        file = new File(PATH + filename);

        if (!file.exists())
        {
            try {
                InputStream myInput = activity.getAssets().open("databases/" + filename);
                String outFileName = PATH + filename;
                OutputStream myOutput = new FileOutputStream(outFileName);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }
                return true;

            } catch (IOException e) {
                Error_log.logError(context, "DBAdapter", "copyFdToFile", e);
                return false;
            }
        } else {
            return true;
        }

    }



    public ArrayList<ErrologInfo> GetEreol() {
        ArrayList<ErrologInfo> list = new ArrayList<>();
        try {

            //Cursor c = db.rawQuery("SELECT _id,ERR_PAGE_NAME,ERR_FUNCTION,ERR_MESSAGE,ERR_CRON,ERR_STACKTRACE FROM TBLERRORLOG WHERE ERR_FLAG='0' Order by _id desc LIMIT 50", null);


            Cursor c = db.rawQuery("SELECT _id,ERR_PAGE_NAME,ERR_FUNCTION,ERR_MESSAGE,ERR_CRON,ERR_STACKTRACE FROM TBLERRORLOG WHERE ERR_FLAG='0' Order by _id desc LIMIT 50", null);

            //Cursor c = db.rawQuery("SELECT _id,ERR_PAGE_NAME,ERR_FUNCTION,ERR_MESSAGE,ERR_CRON,ERR_STACKTRACE FROM TBLERRORLOG WHERE _id='179'", null);

            while (c.moveToNext())
            {
                ErrologInfo error = new ErrologInfo();
                error.set_id(c.getString(0));
                error.setERR_PAGE_NAME(c.getString(1));
                error.setERR_FUNCTION(c.getString(2));
                error.setERR_MESSAGE(c.getString(3));
                error.setERR_CRON(c.getString(4));
                error.setERR_STACKTRACE(c.getString(5));
                list.add(error);
            }
            c.close();
            return list;
        } catch (Exception e) {
            Error_log.logError(context, "DBAdapter", "GetEreol", e);
          //  CustomDaillog.showAlertDialogOK((Activity) context, "ERROR ! Please try Again !");

        }
        return list;
    }

    public String executeSql(String StatementQuery) {

        try {
            db.execSQL(StatementQuery);
            return "1";
        } catch (SQLException e) {
            Error_log.logError(context, "DBadapter", "executeSql", e);
            if (e.getMessage() != null) {
               // CustomDaillog.ShowDialogGeneral(context, GlobalVariables.ExceptionMessage + e.getMessage(), "ERROR");
            }
            return "0";
        }
    }

    public void UpdateTransaction(String strQry) {
        db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null,
                SQLiteDatabase.OPEN_READWRITE);
        db.execSQL(strQry);


    }

}
