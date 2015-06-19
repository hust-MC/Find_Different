package com.emercy.finddiff;

public abstract class Althorithm
{
	int[] rgb;

	public int[] getPicture()
	{
		return rgb;
	};

	public Althorithm()
	{
	}
	public Althorithm(int[] rgb)
	{
		this.rgb = rgb;
	}
}

class Decorator extends Althorithm
{
	Althorithm althorithm;

	public void setAlthorithm(Althorithm althorithm)
	{
		this.althorithm = althorithm;
	}
}

class ToGrey extends Althorithm
{
	int[] grey;

	@Override
	public int[] getPicture()
	{
		Pictures.convertToGrey(rgb, grey);
		return grey;
	}
}