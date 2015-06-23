package com.emercy.finddiff;

import android.content.pm.PackageInfo;

public class Althorithm
{
	int[] picture;
	int width;
	int height;

	public Althorithm()
	{
	}
	public Althorithm(int[] picture, int width, int height)
	{
		this.picture = picture;
		this.width = width;
		this.height = height;
	}
	public int[] getPicture()
	{
		return picture;
	}
	public void setPicture()
	{
	};
}

abstract class Decorator extends Althorithm
{
	Althorithm althorithm;

	public void setAlthorithm(Althorithm althorithm)
	{
		this.althorithm = althorithm;
	}
	@Override
	public void setPicture()
	{
		if (althorithm != null)
		{
			althorithm.setPicture();
		}
	}
}

class ToGrey extends Decorator
{
	@Override
	public void setPicture()
	{
		int[] grey = null;
		super.setPicture();
		Pictures.convertToGrey(picture, grey);
		althorithm.picture = grey;
	}
}

class Sobel extends Decorator
{

	@Override
	public void setPicture()
	{
		int[] sobel = null;
		super.setPicture();
		Pictures.sobel(picture, width, height, sobel);
		althorithm.picture = sobel;
	}
}
