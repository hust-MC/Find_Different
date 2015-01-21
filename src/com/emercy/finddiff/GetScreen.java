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
	private final int picThreshold = 240;

	private final int TAKE_PIC = 1;		// 按键选项
	private final int PROCESS_PIC = 2; 	// 处理图像
	private final int PREVIEW_PIC = 3;	// 继续拍照
	int state = TAKE_PIC;			// 按键状态机

	private Bitmap bitmap;

	private File file;					// 存储数据
	private FileWriter fw;

	/*
	 * 图片数组
	 */
	private int[] rgb;
	private int[] sobelArray;

	Matrix m = new Matrix();

	public GetScreen(Context context) throws IOException
	{
		file = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/360/a123.txt");
		if (!file.exists())
		{
			file.createNewFile();
		}

		m.postRotate(90);
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

		sobelArray = Pictures.sobel(rgb, width, height);
		Pictures.turnTo2(sobelArray);

		Bitmap temp = Bitmap.createBitmap(sobelArray, width, height,
				Config.RGB_565);
		bitmap = Bitmap.createBitmap(temp, 0, 0, width, height, m, true);
		MainActivity.setImageView(bitmap);

		autoFocus();
	}

	private void saveRGB(int[] pic) throws IOException
	{
		if (file.exists())
		{
			fw = new FileWriter(file, false);
		}

		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				fw.write(Integer.toHexString(pic[width * i + j]) + " ");
			}
			fw.write("\n\r");
		}
		fw.close();
	}
	void takePic() throws IOException
	{

		switch (state)
		{
		case TAKE_PIC:
		{
			Log.d("MC", "take_pic");
			stopCamera();

			state = PROCESS_PIC;

			break;
		}
		case PROCESS_PIC:
		{
			int[] thresholding = Pictures.findFrame(sobelArray, width, height);
			Bitmap temp = Bitmap.createBitmap(thresholding, width, height,
					Config.RGB_565);

			bitmap = Bitmap.createBitmap(temp, 0, 0, width, height, m, true);
			MainActivity.setImageView(bitmap);
			saveRGB(thresholding);
			state = PREVIEW_PIC;
			// camera.takePicture(null, null, pictureCallback);

			break;
		}
		case PREVIEW_PIC:
		{
			startCamera();
			state = TAKE_PIC;

			break;
		}
		}
	}

	PictureCallback pictureCallback = new PictureCallback()
	{
		@Override
		public void onPictureTaken(byte[] data, Camera camera)
		{
			// bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

			Bitmap temp = Bitmap.createBitmap(
					Pictures.findFrame(sobelArray, width, height), width,
					height, Config.RGB_565);
			bitmap = Bitmap.createBitmap(temp, 0, 0, width, height, m, true);
			Log.d("MC", "call back");
			MainActivity.setImageView(bitmap);
		}
	};

}