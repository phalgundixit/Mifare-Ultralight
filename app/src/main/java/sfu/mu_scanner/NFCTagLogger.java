package sfu.mu_scanner;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class NFCTagLogger {

    private Context context;

    public NFCTagLogger(Context context) {
        this.context = context;
    }

    public void writeLog(List<String> tagHexPages) {
        try {
            Date currentTime = Calendar.getInstance().getTime();
            String fileName = "log_" + DateFormat.format("yyyy-MM-dd-HH-mm-ss", currentTime).toString() + ".txt";

            try {
                FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                PrintWriter writer = new PrintWriter(outputStream);

                for (int i = 0; i < tagHexPages.size(); i++) {
                    writer.printf("PAGE %02d:\t%s\n", i, tagHexPages.get(i));
                }

                writer.flush();
                writer.close();
                outputStream.close();
            }
            catch (FileNotFoundException ex) {
                Log.e("LOGGER", "File Not Found!");
                Log.e("ERROR", ex.getMessage());
            }
        }
        catch (Exception ex) {
            Log.e("LOGGER", context.getResources().getString(R.string.message_write_log_error));
            Log.e("ERROR", ex.getMessage());
        }
    }
}
