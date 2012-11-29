package com.elsinga.sample.nfc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class ReadTagActivity extends Activity
{

  private static final String TAG = ReadTagActivity.class.getSimpleName();

  // NFC-related variables
  private NfcAdapter          _nfcAdapter;
  private PendingIntent       _nfcPendingIntent;
  IntentFilter[]              _readTagFilters;

  private TextView            _textViewData;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_readtag);

    _textViewData = (TextView) findViewById(R.id.textData);

    _nfcAdapter = NfcAdapter.getDefaultAdapter(this);

    if (_nfcAdapter == null)
    {
      Toast.makeText(this, "Your device does not support NFC. Cannot run this demo.", Toast.LENGTH_LONG).show();
      finish();
      return;
    }

    checkNfcEnabled();

    _nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

    IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
    try
    {
      ndefDetected.addDataType("application/com.elsinga.sample.nfc");
    }
    catch (MalformedMimeTypeException e)
    {
      throw new RuntimeException("Could not add MIME type.", e);
    }

    _readTagFilters = new IntentFilter[]{ndefDetected};
  }

  @Override
  protected void onResume()
  {
    super.onResume();

    checkNfcEnabled();

    if (getIntent().getAction() != null)
    {
      if (getIntent().getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED))
      {
        NdefMessage[] msgs = getNdefMessagesFromIntent(getIntent());
        NdefRecord record = msgs[0].getRecords()[0];
        byte[] payload = record.getPayload();

        String payloadString = new String(payload);

        _textViewData.setText(payloadString);
      }
    }

    _nfcAdapter.enableForegroundDispatch(this, _nfcPendingIntent, _readTagFilters, null);

  }

  @Override
  protected void onPause()
  {
    super.onPause();

    _nfcAdapter.disableForegroundDispatch(this);
  }

  @Override
  protected void onNewIntent(Intent intent)
  {
    if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED))
    {
      NdefMessage[] msgs = getNdefMessagesFromIntent(intent);
      confirmDisplayedContentOverwrite(msgs[0]);

    }
    else if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED))
    {
      Toast.makeText(this, "This NFC tag has no NDEF data.", Toast.LENGTH_LONG).show();
    }
  }

  NdefMessage[] getNdefMessagesFromIntent(Intent intent)
  {
    // Parse the intent
    NdefMessage[] msgs = null;
    String action = intent.getAction();
    if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED) || action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED))
    {
      Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
      if (rawMsgs != null)
      {
        msgs = new NdefMessage[rawMsgs.length];
        for (int i = 0; i < rawMsgs.length; i++)
        {
          msgs[i] = (NdefMessage) rawMsgs[i];
        }

      }
      else
      {
        // Unknown tag type
        byte[] empty = new byte[]{};
        NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
        NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
        msgs = new NdefMessage[]{msg};
      }

    }
    else
    {
      Log.e(TAG, "Unknown intent.");
      finish();
    }
    return msgs;
  }

  private void confirmDisplayedContentOverwrite(final NdefMessage msg)
  {
    final String data = _textViewData.getText().toString().trim();

    new AlertDialog.Builder(this).setTitle("New tag found!").setMessage("Do you wanna show the content of this tag?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
          @Override
          public void onClick(DialogInterface dialog, int id)
          {
            String payload = new String(msg.getRecords()[0].getPayload());

            _textViewData.setText(new String(payload));
          }
        }).setNegativeButton("No", new DialogInterface.OnClickListener()
        {
          @Override
          public void onClick(DialogInterface dialog, int id)
          {
            _textViewData.setText(data);
            dialog.cancel();
          }
        }).show();
  }

  private void checkNfcEnabled()
  {
    Boolean nfcEnabled = _nfcAdapter.isEnabled();
    if (!nfcEnabled)
    {
      new AlertDialog.Builder(ReadTagActivity.this).setTitle(getString(R.string.text_warning_nfc_is_off))
          .setMessage(getString(R.string.text_turn_on_nfc)).setCancelable(false)
          .setPositiveButton(getString(R.string.text_update_settings), new DialogInterface.OnClickListener()
          {
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
              startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
            }
          }).create().show();
    }
  }
}