package com.emercy.finddiff;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.util.Log;

public class GetScreen implements PreviewCallback
{
	private Camera camera;
	Size size;
	Parameters parameters;

	Boolean go = true;
	private SurfaceTexture surfaceTexture;

	private double bright, lastBright;
	private boolean hasFocus;

	int width, height;

	public boolean isCameraOpened = false;	// 标志相机状态

	/*
	 * 常量定义
	 */
	private final int focusThreshold = 5;
	private final int TAKE_PIC = 1;			// 按键选项
	private final int PROCESS_PIC = 2; 		// 处理图像
	private final int PREVIEW_PIC = 3;		// 继续拍照

	int state = TAKE_PIC;					// 按键状态机

	private Bitmap bitmap;

	private File file;					// 存储数据
	private FileWriter fw;

	/*
	 * 图片数组
	 */
	private int[] rgb, sobelPic, grey, display;

	public GetScreen(Context context) throws IOException
	{
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
		if (!isCameraOpened)
		{
			camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
			parameters = camera.getParameters();
			parameters.setPreviewSize(Pictures.WIDTH, Pictures.HEIGHT);
			camera.setParameters(parameters);

			size = parameters.getPreviewSize();
			width = size.width;
			height = size.height;

			camera.setPreviewTexture(surfaceTexture);
			camera.startPreview();
			camera.setPreviewCallback(this);
			isCameraOpened = true;
		}
	}
	void stopCamera()
	{
		if (isCameraOpened)
		{
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
			isCameraOpened = false;
		}
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
		rgb = new int[Pictures.PIC_LENGTH];
		sobelPic = new int[Pictures.PIC_LENGTH];
		grey = new int[Pictures.PIC_LENGTH];

		Pictures.decodeYUV420SP(rgb, data, width, height);

		switch (MainActivity.whichToDisplay)
		{
		case 1:										// 显示灰度图
			Pictures.convertToGrey(rgb, grey);
			display = grey;
			break;

		case 2:
			Pictures.convertToGrey(rgb, grey);		// sobel变换之后显示
			Pictures.sobel(grey, width, height, sobelPic);
			display = sobelPic;
			break;

		case 3:										// 二值化显示
			Pictures.convertToGrey(rgb, grey);
			Pictures.sobel(grey, width, height, sobelPic);
			Pictures.turnTo2(sobelPic);
			display = sobelPic;
			break;
		default:
			display = rgb;
		}
		bitmap = Bitmap.createBitmap(display, width, height, Config.RGB_565);
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
			int[] thresholding = Pictures.findFrame(sobelPic, width, height);
			bitmap = Bitmap.createBitmap(thresholding, width, height,
					Config.RGB_565);

			MainActivity.setImageView(bitmap);
			// saveRGB(thresholding);
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

}