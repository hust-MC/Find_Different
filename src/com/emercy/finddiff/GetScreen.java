package com.emercy.finddiff;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;

public class GetScreen implements PreviewCallback
{
	private Camera camera;
	Size size;

	Boolean go = true;
	private SurfaceTexture surfaceTexture;
	private final int focusThreshold = 5;

	private double bright, lastBright;
	private boolean hasFocus;

	int width, height;
	final int picThreshold = 100;

	private boolean takePic = true;		// 按键选项

	private Bitmap bitmap;

	private File file;					// 存储数据
	private FileWriter fw;

	private int[] rgb;
	private int[] sobelArray;

	public GetScreen(Context context) throws IOException
	{
		// file = new File(Environment.getExternalStorageDirectory().getPath()
		// + "/360/a123.txt");
		// if (!file.exists())
		// {
		// file.createNewFile();
		// }
		surfaceTexture = new SurfaceTexture(0);

		startCamera();
	}
	void startCamera() throws IOException
	{
		camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
		size = camera.getParameters().getPreviewSize();
		width = size.width;
		height = size.height;
		camera.setPreviewTexture(surfaceTexture);
		camera.startPreview();
		camera.setPreviewCallback(this);
	}

	void stopCamera()
	{
		camera.setPreviewCallback(null);
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	private void autoFocus()
	{
		long currentTime = 0, stableTime = 0;

		currentTime = System.currentTimeMillis();
		bright = Pictures.getLight(rgb);

		if (Math.abs(bright - lastBright) > focusThreshold)
		{
			lastBright = bright;
			hasFocus = false;
			stableTime = currentTime;
		}
		else
		{
			if (!hasFocus && currentTime - stableTime >= 500)
			{
				camera.autoFocus(new AutoFocusCallback()
				{
					@Override
					public void onAutoFocus(boolean success, Camera camera)
					{

					}
				});

				hasFocus = true;
			}
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera)
	{
		rgb = new int[width * height];
		sobelArray = new int[rgb.length];

		Pictures.decodeYUV420SP(rgb, data, width, height);

		Pictures.convertToGrey(rgb);

		Matrix m = new Matrix();
		m.postRotate(90);

		sobelArray = Pictures.sobel(rgb, width, height);
		Pictures.turnTo2(sobelArray);
		
		Bitmap temp = Bitmap.createBitmap(sobelArray, width, height,
				Config.RGB_565);
		bitmap = Bitmap.createBitmap(temp, 0, 0, width, height, m, true);
		MainActivity.setImageView(bitmap);

		autoFocus();
	}

	private void saveRGB() throws IOException
	{
		if (file.exists())
		{
			fw = new FileWriter(file, false);
		}

		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				fw.write(Integer.toHexString(rgb[height * i + j]) + " ");
			}
			fw.write("\n\r");
		}
		fw.close();
	}
	void takePic() throws IOException
	{
//		if (file.exists())
//		{
//			fw = new FileWriter(file, false);
//		}
		if (takePic)
		{
			camera.takePicture(null, null, pictureCallback);
			stopCamera();

			// saveRGB();
			takePic = false;
		}
		else
		{
			startCamera();
			takePic = true;
		}
	}

	PictureCallback pictureCallback = new PictureCallback()
	{
		@Override
		public void onPictureTaken(byte[] data, Camera camera)
		{
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		}
	};

}