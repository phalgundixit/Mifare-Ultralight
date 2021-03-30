package sfu.mu_scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.IntStream;

import static android.content.ContentValues.TAG;
import static java.util.stream.Collectors.toList;

public class ProtectionPage extends Activity {

    Button btnWriteTag,btnReadTag,btnSetPassword,btnRemovePwd,btnFormatTag;
    private NfcAdapter nfcAdapter;
    Tag tag;
    String Action = "INITIALIZED YET";
    private static final int NUM_PAGES = 16;
    Intent intent = new Intent();

    private NFCTagLogger nfcTagLogger;
    private NFCTagManager nfcTagManager;
    EditText txtPassword,txtContent;
    private static final int MU_BYTE_LIMIT = 64;

    AlertDialog dlg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.protction_page);

        btnWriteTag = (Button) findViewById(R.id.btnWriteTag);
        btnReadTag = (Button) findViewById(R.id.btnReadTag);
        btnSetPassword = (Button) findViewById(R.id.btnSetPassword);
        btnRemovePwd = (Button) findViewById(R.id.btnRemovePwd);
        btnFormatTag = (Button) findViewById(R.id.btnFormatTag);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtContent = (EditText) findViewById(R.id.txtContent);

        try {
            //Initializing nfc adapter
            initialize();
        } catch (NoSuchFieldException ex) {
            //Error_log.logError(ProtectionPage.this,"Protection Page","onCreate Initilize",ex);
            Log.e("INIT", "Initialization Failed!");
            ex.printStackTrace();
        }


        if (nfcAdapter == null) {
            Toast.makeText(this, R.string.message_nfc_not_supported, Toast.LENGTH_LONG).show();
            finish();
        }
        else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, R.string.message_nfc_not_enabled, Toast.LENGTH_LONG).show();
            finish();
        }

        btnWriteTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(txtContent.getText().toString().trim().length()==0)
                {
                    txtContent.setText(txtContent.getText().toString().trim());
                    Toast.makeText(ProtectionPage.this, "Enter the Content to write", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(txtContent.getText().toString().trim().length() > 64)
                {
                    txtContent.setText(txtContent.getText().toString().trim());
                    Toast.makeText(ProtectionPage.this, "Enter less than 64 characters : Entered: " + txtContent.getText().toString().trim().length() , Toast.LENGTH_SHORT).show();
                    return;
                }
                Action = "WRITE";
                showIndicator(5);
                // Toast.makeText(ProtectionPage.this, Action, Toast.LENGTH_SHORT).show();
                //WriteTag(tag,"123456789");
            }
        });

        btnReadTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Action = "READ";
                txtContent.setText("");
                showIndicator(5);

                // Toast.makeText(ProtectionPage.this, Action, Toast.LENGTH_SHORT).show();
            }
        });

        btnSetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(txtPassword.getText().toString().trim().length()==0)
                {
                    Toast.makeText(ProtectionPage.this, "Set The Password to Write", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(txtPassword.getText().toString().trim().length() != 4)
                {
                    txtPassword.setText(txtPassword.getText().toString().trim());
                    Toast.makeText(ProtectionPage.this, "Password Should Be 4 digit", Toast.LENGTH_SHORT).show();
                    return;
                }

                //PasswordProtect(tag);
                Action = "SETPWD";
                showIndicator(5);
                //Toast.makeText(ProtectionPage.this, Action, Toast.LENGTH_SHORT).show();
                // handleIntent(intent);


            }
        });

        btnRemovePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(txtPassword.getText().toString().trim().length()==0)
                {
                    Toast.makeText(ProtectionPage.this, "Enter the Valid password to View data", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(txtPassword.getText().toString().trim().length() != 4)
                {
                    txtPassword.setText(txtPassword.getText().toString().trim());
                    Toast.makeText(ProtectionPage.this, "Password Should Be 4 digit", Toast.LENGTH_SHORT).show();
                    return;
                }

                showIndicator(5);
                Action = "REMOVE";
                // Toast.makeText(ProtectionPage.this, Action, Toast.LENGTH_SHORT).show();

            }
        });

        btnFormatTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Action = "FORMAT";
                showIndicator(5);

                Toast.makeText(ProtectionPage.this, Action, Toast.LENGTH_SHORT).show();
            }
        });



    }


    protected void onPause() {
        super.onPause();
        stopForegroundDispatch(this, nfcAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupForegroundDispatch(this, nfcAdapter);
    }
    private void initialize() throws NoSuchFieldException {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        nfcTagManager = new NFCTagManager();

    }
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }


    public static String rightPadZeros(String str, int num) {
        return String.format("%1$-" + num + "s", str).replace(' ', '0');
    }

    public static List<String> splitString(final String string, final int chunkSize) {
        final int numberOfChunks = (string.length() + chunkSize - 1) / chunkSize;
        return IntStream.range(0, numberOfChunks)
                .mapToObj(index -> string.substring(index * chunkSize, Math.min((index + 1) * chunkSize, string.length())))
                .collect(toList());
    }



    public String  ReadTag(Tag Tag) throws IOException {
        byte[] payload = new byte[16];
        MifareUltralight tag = MifareUltralight.get(Tag);
        String base64 = "";
        String Result = "";
        try {

            tag.connect();

            byte[] data = new byte[64];

            // reading tag;

           /* private static final int MU_BYTE_LIMIT = 64;
            private static final int MU_BYTES_PER_PAGE = 4;
            private static final int MU_BYTES_PER_READ = 16;
            private static final int MU_NUM_PAGES = 16;
            private static final int MU_PAGES_PER_READ = 4;
            private static final int MU_FIRST_WRITABLE_PAGE = 4;*/

           /* for (int i = 0; i < 16; i += 4) {
                System.arraycopy(tag.readPages(i), 0, data, (i * 4), 16);
            }*/

            for (int i = 0; i < 16; i += 4) {
                byte[] payloadd = tag.readPages(i + 4);
                base64 =  new String(payloadd, Charset.forName("US-ASCII"));
                Result = Result + base64;
                Result = Result.replace('$',' ');
                Result = Result.trim();

                DBAdapter db = new DBAdapter(ProtectionPage.this);
                db.open();
                ContentValues val = new ContentValues();
                val.put("CONTENT",Result);
                db.Insert(val,"TBLNFCTESTDATA");
                db.close();


            }


           // base64 = new String(data,"UTF-8");
           // base64 = base64.replaceAll("[^\\x00-\\x7F]", "");
            txtContent.setText(Result);



            DBAdapter db = new DBAdapter(ProtectionPage.this);
            db.open();
            ContentValues val = new ContentValues();
            val.put("EXCEPTION","READ SUCCESSFULL!!");
            db.Insert(val,"TBLNFCTESTDATA");
            db.close();
            Toast.makeText(this, "READ SUCCESSFULL!!", Toast.LENGTH_LONG).show();


            return base64;
        } catch (IOException e) {
           // Error_log.logError(ProtectionPage.this,"Protection Page","Read Tag",e);
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
           // Error_log.logError(ProtectionPage.this,"Protection Page","Read Tag",e);
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
        } finally {
            tag.close();
        }

        return "";
    }


    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            //System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }


    public void ErasePages(Tag tag)
    {
        MifareUltralight ultralight = MifareUltralight.get(tag);
        try {

            ultralight.connect();

            for (int j = 0; j <= 16; j++) {
                ultralight.writePage(j + 4, "    ".getBytes(Charset.forName("US-ASCII")));
            }

            DBAdapter db = new DBAdapter(ProtectionPage.this);
            db.open();
            ContentValues val = new ContentValues();
            val.put("EXCEPTION","Erased Successfully!!");
            db.Insert(val,"TBLNFCTESTDATA");
            db.close();

            Toast.makeText(this, "Erased Successfully!!", Toast.LENGTH_LONG).show();

        }
        catch (IOException e)
        {
            //Error_log.logError(ProtectionPage.this,"Protection Page","ErasePages",e);

            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {           // Error_log.logError(ProtectionPage.this,"Protection Page","ErasePages",e);

            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
        finally {
            try {
                ultralight.close();
            } catch (IOException e) {
               // Error_log.logError(ProtectionPage.this,"Protection Page","ErasePages",e);

                Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
            }
        }

    }

    public void WriteTag(Tag tag, String tagText) {


        MifareUltralight ultralight = MifareUltralight.get(tag);
        try {

            /*tagText = "";
            for (int j = 0; j < 16; j++) {
                if (j < 10) {
                    tagText = tagText + "DDDD";
                }
                if (j >= 10 && j < 20) {
                    tagText = tagText + "XXXX";
                }
                if (j > 20) {
                    tagText = tagText + "2222";
                }

            }*/

            int len = tagText.length();

            List<String> Data = splitString(tagText, 4);

            ultralight.connect();

            String ValidateInput = "";
            // Smallest tag only has 64 bytes


            if (Data.size() <= 16) {

                for (int i = 0; i < Data.size(); i++) {

                    ultralight.writePage(i + 4, "    ".getBytes(Charset.forName("US-ASCII")));
                    ValidateInput = Data.get(i);
                    ValidateInput = String.format("%1$-" + 4 + "s", ValidateInput).replace(' ', '$');
                    ultralight.writePage(i + 4, ValidateInput.getBytes(Charset.forName("US-ASCII")));
                    // Log.d(TAG, "NFC Writte:" + tagText.substring(i * 4, end));
                }
                DBAdapter db = new DBAdapter(ProtectionPage.this);
                db.open();
                ContentValues val = new ContentValues();
                val.put("EXCEPTION","WRITE SUCCESSFULL");
                db.Insert(val,"TBLNFCTESTDATA");
                db.close();

                DBAdapter.copyFdToFile(ProtectionPage.this);


                Toast.makeText(this, "WRITE SUCCESSFULL", Toast.LENGTH_LONG).show();
            }
            else
            {
                DBAdapter db = new DBAdapter(ProtectionPage.this);
                db.open();
                ContentValues val = new ContentValues();
                val.put("EXCEPTION","FAILED TO WRITE \" + Data.size() + \" PAGES EXP 16");
                db.Insert(val,"TBLNFCTESTDATA");
                db.close();

                Toast.makeText(this, "FAILED TO WRITE " + Data.size() + " PAGES EXP 16", Toast.LENGTH_LONG).show();
            }


        }
        catch (IOException e)
        {
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
        finally {
            try {
                ultralight.close();
            } catch (IOException e) {
                Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void showIndicator(int Seconds)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(ProtectionPage.this);
        builder.setTitle("Approach NFC Card!!");
        builder.setMessage("Please get the NFC card close to phone");
        builder.setCancelable(false);

        dlg = builder.create();

        dlg.show();

        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                dlg.dismiss(); // when the task active then close the dialog
                t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
            }
        }, 1000 * Seconds); // after 2 second (or 2000 miliseconds), the task will be active.

    }

    public void PasswordProtect(Tag Tag,String Pwd) throws IOException {

        String Password = "11";
        MifareUltralight ultralight = MifareUltralight.get(Tag);
        try {


            ultralight.connect();
            Password = Pwd;
            byte[] t = Password.getBytes(Charset.forName("US-ASCII"));

            //SET PASSWORD
            //ultralight.writePage( 43, Password.getBytes(Charset.forName("US-ASCII")));
            byte[] response = ultralight.transceive(new byte[] {
                    (byte) 0xA2, // WRITE
                    (byte) 43,   // page address
                    t[0],t[1],t[2],t[3]
            });

            //  SET ACKNOLOWLEDGEMENT
            Password = "00";

            byte[] tt = Password.getBytes(Charset.forName("US-ASCII"));

            // ultralight.connect();
            byte[] responseAut = ultralight.transceive(new byte[] {
                    (byte) 0xA2, // WRITE
                    (byte) 44,   // page address
                    tt[0],tt[1],(byte) 0, (byte) 0
            });

            byte[] responseAuth0 = ultralight.transceive(new byte[] {
                    (byte) 0x30, // READ
                    (byte) 41    // page address
            });
            if ((responseAuth0 != null) && (responseAuth0.length >= 16)) {  // read always returns 4 pages
                boolean prot = false;  // false = PWD_AUTH for write only, true = PWD_AUTH for read and write
                int auth0 = 0; // first page to be protected, set to a value between 0 and 37 for NTAG212
                responseAuth0 = ultralight.transceive(new byte[] {
                        (byte) 0xA2, // WRITE
                        (byte) 41,   // page address
                        responseAuth0[0], // keep old value for byte 0
                        responseAuth0[1], // keep old value for byte 1
                        responseAuth0[2], // keep old value for byte 2
                        (byte) (auth0 & 0x0ff)
                });
            }

            Toast.makeText(this, "CARD PROTECTED SUCCESSFULLY!!", Toast.LENGTH_LONG).show();

        }
        catch (IOException e)
        {
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
        finally {
            ultralight.close();
        }


    }


    public void RemovePassword(Tag Tag,String Pwd) throws IOException {

        String Password = "11";
        MifareUltralight ultralight = MifareUltralight.get(Tag);
        try {


            ultralight.connect();

            Password = Pwd;
            byte[] t = Password.getBytes(Charset.forName("US-ASCII"));

            byte[] presponse = ultralight.transceive(new byte[] {
                    (byte) 0x1B, // PWD_AUTH
                    t[0],t[1],t[2],t[3]
            });
            byte[] pack = null;
            if ((presponse != null) && (presponse.length >= 2)) {
                pack = Arrays.copyOf(presponse, 2);
                // TODO: verify PACK to confirm that tag is authentic (not really,
                // but that whole PWD_AUTH/PACK authentication mechanism was not
                // really meant to bring much security, I hope; same with the
                // NTAG signature btw.)
            }



            byte[] responseAuth0 = ultralight.transceive(new byte[] {
                    (byte) 0x30, // READ
                    (byte) 41    // page address
            });
            if ((responseAuth0 != null) && (responseAuth0.length >= 16)) {  // read always returns 4 pages
                boolean prot = false;  // false = PWD_AUTH for write only, true = PWD_AUTH for read and write
                int auth0 = 0; // first page to be protected, set to a value between 0 and 37 for NTAG212
                responseAuth0 = ultralight.transceive(new byte[] {
                        (byte) 0xA2, // WRITE
                        (byte) 41,   // page address
                        responseAuth0[0], // keep old value for byte 0
                        responseAuth0[1], // keep old value for byte 1
                        responseAuth0[2], // keep old value for byte 2
                        (byte) (0xff)
                });
            }

            Toast.makeText(this, "PASSWORD REMOVED SUCCESSFULLY!!", Toast.LENGTH_LONG).show();


        }
        catch (IOException e)
        {
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
        }

        finally {
            ultralight.close();
        }


    }


    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        Log.d("DEBUG", "handleIntent() was called.");

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            Log.d("DEBUG", "Tag Found.");
            if (tag != null) {

                handleTag(tag);
            }
            else {
                Log.d("DEBUG", "Tag Is NULL.");
            }
        }
    }



    private static void setupForegroundDispatch(Activity activity, NfcAdapter adapter) {
        Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter techFilter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] filters = new IntentFilter[] {techFilter};
        String[][] techLists = new String[][] {new String[] {MifareUltralight.class.getName()}};

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techLists);
    }

    private static void stopForegroundDispatch(Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }
    private void handleTag(Tag tag) {
        Log.v("TAG", tag.toString());
        Log.d("DEBUG", "handleTag() was called.");

        if(Action == "WRITE")
        {

            dlg.dismiss();
           // ErasePages(tag);
            WriteTag(tag,txtContent.getText().toString().trim());

        }

        else if(Action =="READ")
        {
            try {
                dlg.dismiss();
                ReadTag(tag);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if(Action =="SETPWD")
        {
            try {
                dlg.dismiss();
                PasswordProtect(tag,txtPassword.getText().toString().trim());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if(Action == "REMOVE")
        {
            try {
                dlg.dismiss();
                RemovePassword(tag,txtPassword.getText().toString().trim());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if(Action =="FORMAT")
        {
            dlg.dismiss();
            ErasePages(tag);
        }


    }


}
