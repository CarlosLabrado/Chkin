package chkin.app_labs.com.chkin;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import chkin.app_labs.com.chkin.NFCManager.NFCManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private NFCManager mNFCManager;

    private View v;
    private NdefMessage mNdefMessage = null;
    private ProgressDialog dialog;
    private boolean isOnWrite = false;

    Tag currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        v = fab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                mNdefMessage = mNFCManager.createUriMessage("petrologweb.com ", "http://");
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mNFCManager = new NFCManager(this);


    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            mNFCManager.verifyNFC();
            //mNFCManager.enableDispatch();

            Intent nfcIntent = new Intent(this, getClass());
            nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
            IntentFilter[] intentFiltersArray = new IntentFilter[]{};
            String[][] techList = new String[][]{{android.nfc.tech.Ndef.class.getName()}, {android.nfc.tech.NdefFormatable.class.getName()}};
            NfcAdapter nfcAdpt = NfcAdapter.getDefaultAdapter(this);
            nfcAdpt.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techList);
        } catch (NFCManager.NFCNotSupported nfcnsup) {
            Snackbar.make(v, "NFC not supported", Snackbar.LENGTH_LONG).show();
        } catch (NFCManager.NFCNotEnabled nfcnEn) {
            Snackbar.make(v, "NFC Not enabled", Snackbar.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mNFCManager.disableDispatch();
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d("Nfc", "New intent");
        // It is the time to write the tag
        currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (isOnWrite) {
            if (mNdefMessage != null) {
                mNFCManager.writeTag(currentTag, mNdefMessage);
//            dialog.dismiss();
                Snackbar.make(v, "Tag written", Snackbar.LENGTH_LONG).show();
            } else {
                Log.d("empty mNdefMessage", "empty mNdefMessag, we will not write");
                // Handle intent

            }
        } else {
            if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
                NdefMessage[] messages = null;
                Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                if (rawMsgs != null) {
                    messages = new NdefMessage[rawMsgs.length];
                    for (int i = 0; i < rawMsgs.length; i++) {
                        messages[i] = (NdefMessage) rawMsgs[i];
                    }
                }
                if (messages != null) {
                    if (messages[0] != null) {
                        String result = "";
                        byte[] payload = messages[0].getRecords()[0].getPayload();
                        // this assumes that we get back am SOH followed by host/code
                        for (int b = 1; b < payload.length; b++) { // skip SOH
                            result += (char) payload[b];
                        }
                        Toast.makeText(this, "TAG found", Toast.LENGTH_SHORT).show();
                        separateNFCMessage(result);
                    }
                } else {
                    Toast.makeText(this, "The NFC tag appears to be empty", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    /**
     * Since the nfc message is a Comma Separated Value type, we need to format it and save it to
     * the DB
     *
     * @param nfcMessage
     */
    public void separateNFCMessage(String nfcMessage) {
        Log.d("message", nfcMessage);
//        String[] csv = nfcMessage.split(",");
//
//        // 8 is the number of fields that our NFC tags have
//        // And it must start with our Identifier
//        if (csv.length == 8 && csv[0].equals(NFC_PETROLOG_IDENTIFIER)) {
//            int serial = Integer.parseInt(csv[1]);
//            String comment = csv[2];
//            Double lat = Double.parseDouble(csv[3]);
//            Double lng = Double.parseDouble(csv[4]);
//            String bluetooth = csv[5];
//            String wifiAddress = csv[6];
//            String wifiPass = csv[7];
//
//        } else {
//            Toast.makeText(this, "Sorry this NFC tag is not a Petrolog tag", Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
