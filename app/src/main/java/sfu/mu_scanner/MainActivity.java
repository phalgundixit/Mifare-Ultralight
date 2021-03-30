package sfu.mu_scanner;

import android.app.Activity;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements CompoundButton.OnCheckedChangeListener {

    private static final int NUM_PAGES = 16;
    private boolean isReadOnly;
    private List<TextView> tagPages;

    private NfcAdapter nfcAdapter;
    private NFCTagLogger nfcTagLogger;
    private NFCTagManager nfcTagManager;
    Button btnProtection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnProtection = (Button) findViewById(R.id.btnProtection);
        btnProtection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(MainActivity.this,ProtectionPage.class);
                startActivity(i);
            }
        });

        Log.d("DEBUG", "onCreate() was called.");

        try {
            initialize();
        } catch (NoSuchFieldException ex) {
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
    }

    private void initialize() throws NoSuchFieldException {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcTagLogger = new NFCTagLogger(this);
        nfcTagManager = new NFCTagManager();

        Switch swReadOnly = findViewById(R.id.switchReadOnly);
        swReadOnly.setOnCheckedChangeListener(this);
        isReadOnly = swReadOnly.isChecked();

        tagPages = new ArrayList<>();

        for (int i = 0; i < NUM_PAGES; i++) {
            try {
                int id = R.id.class.getField("textViewPageData" + i).getInt(0);
                tagPages.add((TextView) findViewById(id));
            }
            catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonSwitch, boolean isChecked) {
        isReadOnly = isChecked;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopForegroundDispatch(this, nfcAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupForegroundDispatch(this, nfcAdapter);
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

    private void handleTag(Tag tag) {
        Log.v("TAG", tag.toString());
        Log.d("DEBUG", "handleTag() was called.");

        /*if(Action == "WIRTE")
        {
            NFCWIRTE
        }
        if(action ="READ")
        {

        }*/
        try {
            List<String> tagHexPages = nfcTagManager.streamTagHexValue(tag, true);

            nfcTagLogger.writeLog(tagHexPages);

            if (isReadOnly) {
                //nfcTagManager.readTag(tag, this.getApplicationContext());
                //byte[] bytes =  nfcTagManager.NewreadMifareUltralight(tag);

                nfcTagManager.PasswordProtect(tag);

                //nfcTagManager.RemovePassword(tag);


               // nfcTagManager.NewwriteTag(tag,"NITHIN");
                //String base64 = Convert.ToBase64String(bytes);
                //String str = new String(bytes);
                //str = str ;
               // Toast.makeText(this, "PROTECTED", Toast.LENGTH_LONG).show();

            }
            else {
                //nfcTagManager.readWriteTag(tag, this.getApplicationContext());

                //nfcTagManager.NewwriteTag(tag,"NITHIN");
            }

            String st = "";
            for (int i = 0; i < NUM_PAGES; i++) {
                String tagPageText = tagHexPages.get(i);
                tagPages.get(i).setText(tagPageText);
                st = st + "\n" +  i + "-" + tagPageText;
            }
            Toast.makeText(this, st, Toast.LENGTH_LONG).show();
        }
        catch (Exception ex) {
            Log.e("ERROR", ex.getMessage());

            if (isReadOnly) {
                Toast.makeText(this, R.string.message_read_tag_error, Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(this, R.string.message_write_tag_error, Toast.LENGTH_LONG).show();
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
}
