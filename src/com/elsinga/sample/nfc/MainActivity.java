package com.elsinga.sample.nfc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity
{

  private Button _buttonWriteTag;
  private Button _buttonReadTag;
  private Button _buttonBeam;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    _buttonWriteTag = (Button) findViewById(R.id.buttonWriteTag);
    _buttonWriteTag.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        startActivity(new Intent(MainActivity.this, WriteTagActivity.class));
      }
    });

    _buttonReadTag = (Button) findViewById(R.id.buttonReadTag);
    _buttonReadTag.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        startActivity(new Intent(MainActivity.this, ReadTagActivity.class));
      }
    });

    _buttonBeam = (Button) findViewById(R.id.buttonBeam);
    _buttonBeam.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        startActivity(new Intent(MainActivity.this, BeamActivity.class));
      }
    });
  }
}
