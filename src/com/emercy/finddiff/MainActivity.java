package com.emercy.finddiff;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ImageView;

public class MainActivity extends Activity
{
	
	Camera camera;
	SurfaceTexture surfaceTexture;
	static ImageView imageView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		imageView = (ImageView) findViewById(R.id.imageview);

		camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);

		surfaceTexture = new SurfaceTexture(0);
		try
		{
			camera.setPreviewTexture(surfaceTexture);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		camera.startPreview();
		camera.setPreviewCallback(new GetScreen(camera));

	}

	public static void setImageView(Bitmap bm)
	{
		imageView.setImageBitmap(bm);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
