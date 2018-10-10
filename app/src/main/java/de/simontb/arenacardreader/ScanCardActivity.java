package de.simontb.arenacardreader;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ScanCardActivity extends AppCompatActivity {

    private NfcAdapter adapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFilters;
    private String[][] supportedTechnologies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_card);
        handleCardNumberInput();
        handleCardScanViaNfc();
    }

    private void handleCardNumberInput() {
        final EditText cardNumberInput = findViewById(R.id.cardNumberInput);
        final Button okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(view -> {
            long cardNumber = Long.parseLong(cardNumberInput.getText().toString());
            displayBalanceOfCard(cardNumber);
        });
    }

    private void displayBalanceOfCard(long cardNumber) {
        final Intent displayCreditIntent = new Intent(this, CardBalanceActivity.class);
        displayCreditIntent.putExtra(CardBalanceActivity.EXTRA_CARD_NUMBER, cardNumber);
        startActivity(displayCreditIntent);
    }

    private void handleCardScanViaNfc() {
        final NfcManager manager = (NfcManager) getSystemService(NFC_SERVICE);
        adapter = manager.getDefaultAdapter();
        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        final IntentFilter intentFilter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        intentFilters = new IntentFilter[]{intentFilter};
        supportedTechnologies = new String[][]{new String[]{MifareClassic.class.getName()}};
        onNewIntent(getIntent());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null!= adapter) {
            //Device supports NFC
            adapter.enableForegroundDispatch(this, pendingIntent, intentFilters, supportedTechnologies);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null!= adapter) {
            adapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            final byte[] tagIdBytes = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            final long tagId = ByteBuffer.wrap(tagIdBytes).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFFFFFFL;
            Log.d(ScanCardActivity.class.getSimpleName(), "Tag: " + tagId);
            displayBalanceOfCard(tagId);
        }
    }

}
