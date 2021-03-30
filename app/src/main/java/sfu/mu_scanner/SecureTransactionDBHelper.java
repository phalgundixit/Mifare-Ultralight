package sfu.mu_scanner;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



import static android.content.ContentValues.TAG;

public class SecureTransactionDBHelper extends SQLiteOpenHelper {

    private static String DB_NAME = "test.db";

    String PATH = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + "test" + "/";

    private SQLiteDatabase db;
    private final Context context;
    private String DB_PATH;
    public static String StrDeviceStorePath = "test";
    public static String BackUpfilename = "test.db";
    public static String StrFolderName = "NFC";

    public SecureTransactionDBHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.context = context;
        DB_PATH = PATH;

    }


    public void closeTransDB() {

        if (db != null) {
            if (db.isOpen()) {
                db.close();
            }
        }


    }

    public Cursor getCursor(String strQry) {
        String myPath = DB_PATH + DB_NAME;
        db = SQLiteDatabase.openDatabase(myPath, null,
                SQLiteDatabase.OPEN_READONLY);
        Cursor c = db.rawQuery(strQry, null);
        // Note: Master is the one table in External db. Here we trying to access the records of table from external db.
        return c;
    }

    public boolean checkTransactionDataBase() {

        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    public void insertTransaction(ContentValues cntValues, String Tablename) {
        try {
            try {
                copyFdToFile(context);
            } catch (IOException e) {
                Error_log.logError(context, "SecureTransactionDbHelper", "insertTransaction", e);


                e.printStackTrace();
            }
            db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
            db.insert(Tablename, null, cntValues);


        } catch (SQLException e) {
            Error_log.logError(context, "SecureTransactionDbHelper", "insertTransaction", e);
            //CustomDiallog.showAlertDialogOK((Activity) context,"ERROR ! Please Try Again!");
            Log.d(TAG, e.getMessage());
        }
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
        if (!file.exists()) {
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
                Error_log.logError(activity, "DBAdapter", "copyFdToFile", e);
                return false;
            }
        } else {
            return true;
        }

    }


    public void UpdateTransaction(String strQry) {
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null,
                    SQLiteDatabase.OPEN_READWRITE);
            db.execSQL(strQry);
        } catch (SQLException e) {
            Error_log.logError(context, "SecureTransactionDbHelper", "UpdateTransaction", e);
          //  CustomDiallog.showAlertDialogOK((Activity) context,"ERROR ! Please Try Again!");
            Log.d(TAG, e.getMessage());
        }
    }


    public void DeleteTransction(String strQry)
    {
        db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null,
                SQLiteDatabase.OPEN_READWRITE);
        db.execSQL(strQry);
    }


    @Override
    public void onCreate(SQLiteDatabase arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    }
} 