package com.emercy.finddiff;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class MainActivity extends Activity
{

	SurfaceTexture surfaceTexture;
	static ImageView imageView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_main);
		imageView = (ImageView) findViewById(R.id.preview);

		try
		{
			new GetScreen(this);
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
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
