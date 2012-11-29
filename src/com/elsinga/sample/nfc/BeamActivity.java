package com.elsinga.sample.nfc;

import java.nio.charset.Charset;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class BeamActivity extends Activity implements CreateNdefMessageCallback
{

  private EditText _editTextData;

  NfcAdapter       _nfcAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_beam);

    _editTextData = (EditText) findViewById(R.id.textData);

    _nfcAdapter = NfcAdapter.getDefaultAdapter(this);

    if (_nfcAdapter == null)
    {
      Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
      finish();
      return;
    }

    _nfcAdapter.setNdefPushMessageCallback(this, this);
  }

  @Override
  public NdefMessage createNdefMessage(NfcEvent event)
  {

    String data = _editTextData.getText().toString().trim();

    String mimeType = "application/com.elsinga.sample.nfc";

    byte[] mimeBytes = mimeType.getBytes(Charset.forName("UTF-8"));
    byte[] dataBytes = data.getBytes(Charset.forName("UTF-8"));
    byte[] id = new byte[0];

    NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, id, dataBytes);

    NdefMessage message = new NdefMessage(new NdefRecord[]{record});

    return message;
  }

}
