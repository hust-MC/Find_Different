package com.emercy.finddiff;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class MainActivity extends Activity
{

	SurfaceTexture surfaceTexture;
	static ImageView imageView;
	static Button takePic_bt;

	GetScreen getScreen;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);
		imageView = (ImageView) findViewById(R.id.preview);
		takePic_bt = (Button) findViewById(R.id.takePic_bt);

			try
			{
				getScreen = new GetScreen(this);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
	}
	public static void setImageView(Bitmap bm)
	{
		imageView.setImageBitmap(bm);
		imageView.setScaleType(ScaleType.CENTER_CROP);
	}

	public void onClick_takePic(View view) throws IOException
	{
		getScreen.takePic();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
