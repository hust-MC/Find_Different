package com.emercy.finddiff;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

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
import android.os.Environment;
import android.util.Log;

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

	private boolean takePic = true;		// 按键选项

	private Bitmap bitmap;

	private File file;					// 存储数据
	private FileWriter fw;
	private FileInputStream fis;

	private int[] rgb;

	public GetScreen(Context context) throws IOException
	{
		Log.d("MC", "constructor");
		file = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/360/a123.txt");
		if (!file.exists())
		{
			file.createNewFile();
		}
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

	public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height)
	{
		final int frameSize = width * height;
		for (int j = 0, yp = 0; j < height; j++)
		{
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++)
			{
				int y = (0xff & ((int) yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0)
				{
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}

				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;

				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
						| ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
			}
		}
	}

	public double getLight(int rgb[])
	{
		int i;
		double bright = 0;
		for (i = 0; i < rgb.length; ++i)
		{
			int localTemp = rgb[i];
			int r = (localTemp | 0xff00ffff) >> 16 & 0x00ff;
			int g = (localTemp | 0xffff00ff) >> 8 & 0x0000ff;
			int b = (localTemp | 0xffffff00) & 0x0000ff;
			bright = bright + 0.299 * r + 0.587 * g + 0.114 * b;
		}
		return bright / rgb.length;
	}

	private void autoFocus()
	{
		camera.autoFocus(new AutoFocusCallback()
		{
			@Override
			public void onAutoFocus(boolean success, Camera camera)
			{

			}
		});
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera)
	{
		rgb = new int[width * height];
		long currentTime = 0, stableTime = 0;
		decodeYUV420SP(rgb, data, width, height);

		Matrix m = new Matrix();
		m.postRotate(90);

		Bitmap temp = Bitmap.createBitmap(rgb, width, height, Config.RGB_565);
		Log.d("MC", rgb.length + "");
		bitmap = Bitmap.createBitmap(temp, 0, 0, width, height, m, true);
		MainActivity.setImageView(bitmap);

		currentTime = System.currentTimeMillis();
		bright = getLight(rgb);
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
				autoFocus();
				hasFocus = true;
			}
		}
	}

	private void saveRGB() throws IOException
	{
		if (file.exists())
		{
			fw = new FileWriter(file, false);
			Log.d("MC", "overwrite");
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
		Log.d("MC", "saveRGB");
	}
	void takePic() throws IOException
	{
		if (file.exists())
		{
			fw = new FileWriter(file, false);
			Log.d("MC", "overwrite");
		}
		if (takePic)
		{
			camera.takePicture(null, null, pictureCallback);
			stopCamera();

			saveRGB();

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