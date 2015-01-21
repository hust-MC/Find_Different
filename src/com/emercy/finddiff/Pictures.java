package com.emercy.finddiff;

import android.util.Log;

public class Pictures
{
	private static final int ALPHA = 0xff << 24;
	private static final int BLACK = ALPHA | 0x0;
	private static final int WHITE = ALPHA | 0xffffff;
	private static final int picThreshold = 240;

	public static final int WIDTH = 480, HEIGHT = 320;
	public static final int PIC_LENGTH = WIDTH * HEIGHT;

	private static boolean hasFirstPointFound;

	private static Vertext[] vertexts = new Vertext[4];

	private static int[][] sobel_h =
	{
	{ 1, 2, 1 },
	{ 0, 0, 0 },
	{ -1, -2, -1 } };
	private static int sobel_v[][] =
	{
	{ 1, 0, -1 },
	{ 2, 0, -2 },
	{ 1, 0, -1 } };

	static public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width,
			int height)
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

	static public double getLight(int rgb[])
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

	private static void getBrightArray(int[] rgb, short[] brightArray)
	{
		int localTemp, r, g, b;
		for (int i = 0; i < rgb.length; ++i)
		{
			localTemp = rgb[i];
			r = (localTemp >> 16) & 0xff;
			g = (localTemp >> 8) & 0xff;
			b = localTemp & 0xff;

			brightArray[i] = (short) (0.299 * r + 0.587 * g + 0.114 * b);
		}
	}
	static public void convertToGrey(int[] rgb, int[] grey)
	{
		int bright = 0;
		short[] brightArray = new short[rgb.length];

		getBrightArray(rgb, brightArray);

		for (int i = 0; i < Pictures.PIC_LENGTH; i++)
		{
			bright = brightArray[i] & 0xff;
			grey[i] = (ALPHA | bright << 16 | bright << 8 | bright);
		}
	}
	static public void sobel(int[] grey, int width, int height, int[] sobelPic)
	{
		short[] brightArray = new short[grey.length];

		int sumH = 0, sumV = 0, sum = 0;

		getBrightArray(grey, brightArray);

		for (int i = 1; i < height - 1; i++)
		{
			for (int j = 1; j < width - 1; j++)
			{
				sumH = sumV = 0;
				for (int m = 0; m < 3; m++)
				{
					for (int n = 0; n < 3; n++)
					{
						sumH += sobel_h[m][n]
								* brightArray[(i - 1 + m) * width + j - 1 + n];
						sumV += sobel_v[m][n]
								* brightArray[(i - 1 + m) * width + j - 1 + n];
					}
				}
				sumH = sumH > 0 ? sumH : sumH * -1;
				sumH = sumH > 0xff ? 0xff : sumH;

				sumV = sumV > 0 ? sumV : sumV * -1;
				sumV = sumV > 0xff ? 0xff : sumV;

				sum = (sumV + sumH) > 0xff ? 0xff : (sumH + sumV);
				sobelPic[i * width + j] = (ALPHA | sum << 16 | sum << 8 | sum);
			}
		}
	}
	static void turnTo2(int[] pic)
	{
		for (int i = 0; i < pic.length; i++)
		{
			pic[i] = (pic[i] & 0xff) > picThreshold ? WHITE : BLACK;
		}
	}

	static int[] findFrame(int[] pic, int width, int height)
	{
		int[] frame = new int[pic.length];
		for (Vertext v : vertexts)
		{
			v = new Pictures().new Vertext();
		}

		hasFirstPointFound = false;
		hasLastPointFound = false;

		for (int i = 0; i < width; i++)				// 上边缘
		{
			for (int j = 0; j < height / 2; j++)
			{
				if (pic[j * width + i] == WHITE)
				{
					if (i % 50 == 0)
					{
						if (!hasFirstPointFound)
						{
							vertexts[0].x = i;
							vertexts[0].y = j;
							hasFirstPointFound = true;
						}
						else
						{
							vertexts[1].x = i;
							vertexts[1].y = j;
						}
					}
					frame[j * width + i] = WHITE;
					break;
				}
			}

			hasFirstPointFound = false;

			for (int j = height - 1; j > height / 2; j--)
			{
				if (pic[j * width + i] == WHITE)
				{
					if (i % 50 == 0)
					{
						if (!hasFirstPointFound)
						{
							vertexts[2].x = i;
							vertexts[2].y = j;
							hasFirstPointFound = true;
						}
						else
						{
							vertexts[3].x = i;
							vertexts[4].y = j;
						}
					}
					frame[j * width + i] = WHITE;
					break;
				}
			}
		}

		for (int i = 0; i < height; i++)				// 左边缘
		{
			for (int j = 0; j < width / 2; j++)
			{
				if (pic[i * width + j] == WHITE)
				{
					frame[i * width + j] = WHITE;

					// if (!hasTopPointFound)
					// {
					// vertexts[3].x = j;
					// vertexts[3].y = i;
					// hasTopPointFound = true;
					// }
					break;
				}
			}
			for (int j = width - 1; j > width / 2; j--)
			{
				if (pic[i * width + j] == WHITE)
				{
					frame[i * width + j] = WHITE;
					break;
				}
			}
		}

		for (Vertext v : vertexts)
			Log.d("MC", v.x + " " + v.y);

		return frame;
	}

	//
	// public static void removeLonely(int[] pic)
	// {
	// if()
	// }

	class Vertext
	{
		int x = 0;
		int y = 0;
	}
}
