package com.elsinga.sample.nfc;

import java.nio.charset.Charset;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class WriteTagActivity extends Activity
{

  //NFC-related variables
  private NfcAdapter     _nfcAdapter;
  private PendingIntent  _nfcPendingIntent;
  private IntentFilter[] _writeTagFilters;
  private boolean        _writeMode = false;

  private ImageView      _imageViewImage;
  private EditText       _editTextData;
  private Button         _buttonWrite;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_writetag);

    _imageViewImage = (ImageView) findViewById(R.id.image);
    _editTextData = (EditText) findViewById(R.id.textData);
    _buttonWrite = (Button) findViewById(R.id.buttonWriteTag);
    _buttonWrite.setOnClickListener(_tagWriter);

    _nfcAdapter = NfcAdapter.getDefaultAdapter(this);

    if (_nfcAdapter == null)
    {
      Toast.makeText(this, "Your device does not support NFC. Cannot run this sample.", Toast.LENGTH_LONG).show();
      finish();
      return;
    }

    checkNfcEnabled();

    _nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

    IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

    _writeTagFilters = new IntentFilter[]{tagDetected};
  }

  @Override
  protected void onResume()
  {
    super.onResume();

    checkNfcEnabled();
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
    if (_writeMode)
    {
      if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED))
      {
        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        writeTag(buildNdefMessage(), detectedTag);

        _imageViewImage.setImageDrawable(getResources().getDrawable(R.drawable.android_blue_logo));
        _editTextData.setEnabled(true);
      }
    }
  }

  private final View.OnClickListener _tagWriter = new View.OnClickListener()
                                                {
                                                  @Override
                                                  public void onClick(View arg0)
                                                  {
                                                    if (_editTextData.getText().toString().trim().length() == 0)
                                                    {
                                                      Toast.makeText(WriteTagActivity.this, "The data to write is empty. Please fill it!",
                                                                     Toast.LENGTH_LONG).show();
                                                    }
                                                    else
                                                    {
                                                      enableTagWriteMode();
                                                    }
                                                  }
                                                };

  private void enableTagWriteMode()
  {
    _writeMode = true;
    _nfcAdapter.enableForegroundDispatch(this, _nfcPendingIntent, _writeTagFilters, null);

    _imageViewImage.setImageDrawable(getResources().getDrawable(R.drawable.android_writing_logo));
    _editTextData.setEnabled(false);
  }

  boolean writeTag(NdefMessage message, Tag tag)
  {
    int size = message.toByteArray().length;

    try
    {
      Ndef ndef = Ndef.get(tag);
      if (ndef != null)
      {
        ndef.connect();

        if (!ndef.isWritable())
        {
          Toast.makeText(this, "Cannot write to this tag. This tag is read-only.", Toast.LENGTH_LONG).show();
          return false;
        }

        if (ndef.getMaxSize() < size)
        {
          Toast.makeText(this,
                         "Cannot write to this tag. Message size (" + size + " bytes) exceeds this tag's capacity of " + ndef.getMaxSize()
                             + " bytes.", Toast.LENGTH_LONG).show();
          return false;
        }

        ndef.writeNdefMessage(message);
        Toast.makeText(this, "A pre-formatted tag was successfully updated.", Toast.LENGTH_LONG).show();
        return true;
      }

      Toast.makeText(this, "Cannot write to this tag. This tag does not support NDEF.", Toast.LENGTH_LONG).show();
      return false;

    }
    catch (Exception e)
    {
      Toast.makeText(this, "Cannot write to this tag due to an Exception.", Toast.LENGTH_LONG).show();
    }

    return false;
  }

  private NdefMessage buildNdefMessage()
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

  private void checkNfcEnabled()
  {
    Boolean nfcEnabled = _nfcAdapter.isEnabled();
    if (!nfcEnabled)
    {
      new AlertDialog.Builder(WriteTagActivity.this).setTitle(getString(R.string.text_warning_nfc_is_off))
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