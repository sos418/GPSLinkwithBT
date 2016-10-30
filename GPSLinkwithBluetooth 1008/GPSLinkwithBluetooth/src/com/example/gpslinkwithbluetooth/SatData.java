package com.example.gpslinkwithbluetooth;

import java.util.ArrayList;

public class SatData {

	
	int elevation,azimuth;
	int number,snr;

	public SatData(int angle1,int angle2,int innumber,int insnr)
	{
		elevation=angle1;
		azimuth=angle2;
		number=innumber;
		snr=insnr;
	}
	int getelevation()
	{
		return elevation;
		
	}
	int getazimuth()
	{
		return azimuth;
		
	}
	int getnumber()
	{
		return number;
		
	}
	int getsnr()
	{
		return snr;
		
	}
}
