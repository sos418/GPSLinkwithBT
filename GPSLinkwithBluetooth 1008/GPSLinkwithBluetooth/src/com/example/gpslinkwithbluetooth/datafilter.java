package com.example.gpslinkwithbluetooth;

import android.util.Log;

import java.util.ArrayList;

public class datafilter {
	int pos1 = 0, pos2 = 0;
	double lat, lon;
	int UTC=0;
	String GPSStatus;
	ArrayList<SatData> SatData = new ArrayList<SatData>();
	
	public String getlat(){
		if(lat==0)
			return null;
		else
			return String.valueOf(lat); }
	public String getlon(){
		if(lon==0)
			return null;
		else
			return String.valueOf(lon); }
	public String getUTC(){
		if(UTC==0)
			return null;
		else
			return String.valueOf(UTC);}
	public String getGPSStatus(){
			return GPSStatus;}

	public ArrayList<SatData> getsatdata(){return SatData; }
	
	
	public void filter(String msg)
	{
		//&&(msg.indexOf("GNGGA", pos1)) > 0
		while ((msg.indexOf("GPGSV", pos1))>0) {

			pos1 = msg.indexOf("GPGSV", pos1);
			pos2 = msg.indexOf("*", pos1) + 3;

			String temps = msg.substring(pos1, pos2);
			//Log.e("NmeaFilter", temps);
			if (IsCorrectchecksum(temps))
			{
				//Log.e("NmeaFilter", "filter3");
				pos2 = temps.indexOf("*", 0);
				temps = temps.substring(0, pos2);
				
				String[] temps2 = temps.split(",");
				int size = (temps2.length - 3) / 4;

				/*for (int i = 0; i < temps2.length; i++) {
					if (temps2[i].equals(""))
						check = false;

				}*/
				//Log.e("debug", "test3");
				//&&temps2.length>((size-1)*4+7)
				//if (check) {
					//Log.e("debug", "test4");
					//SatData.clear();

					for (int i = 0; i < size; i++) {
						boolean check = true;

						for(int j=0;j<4;j++)
						{
							if((i * 4 + 4 + j)<temps2.length) {
								if (temps2[i * 4 + 4 + j].equals("")) {
									check = false;
									//Log.e("NmeaFilter", "check false because not complete");
								}
							}
							else {
								check = false;
								//Log.e("NmeaFilter", "check false because small than length");
							}
						}
						if(check) {
							SatData.add(new SatData(
									Integer.parseInt(temps2[i * 4 + 4 + 1]),
									Integer.parseInt(temps2[i * 4 + 4 + 2]),
									Integer.parseInt(temps2[i * 4 + 4]),
									Integer.parseInt(temps2[i * 4 + 4 + 3])));
						}
					}
				//}
			}
			pos1 = msg.indexOf("GPGSV", pos1) + temps.length();
		}

		pos1=0;
		while ((msg.indexOf("GLGSV", pos1))>0) {

			pos1 = msg.indexOf("GLGSV", pos1);
			pos2 = msg.indexOf("*", pos1) + 3;

			String temps = msg.substring(pos1, pos2);
			//Log.e("NmeaFilter", temps);
			if (IsCorrectchecksum(temps))
			{
				//Log.e("NmeaFilter", "filter3");
				pos2 = temps.indexOf("*", 0);
				temps = temps.substring(0, pos2);

				String[] temps2 = temps.split(",");
				int size = (temps2.length - 3) / 4;

				/*for (int i = 0; i < temps2.length; i++) {
					if (temps2[i].equals(""))
						check = false;

				}*/
				//Log.e("debug", "test3");
				//&&temps2.length>((size-1)*4+7)
				//if (check) {
				//Log.e("debug", "test4");
				//SatData.clear();

				for (int i = 0; i < size; i++) {
					boolean check = true;

					for(int j=0;j<4;j++)
					{
						if((i * 4 + 4 + j)<temps2.length) {
							if (temps2[i * 4 + 4 + j].equals("")) {
								check = false;
								//Log.e("NmeaFilter", "check false because not complete");
							}
						}
						else {
							check = false;
							//Log.e("NmeaFilter", "check false because small than length");
						}
					}
					if(check) {
						SatData.add(new SatData(
								Integer.parseInt(temps2[i * 4 + 4 + 1]),
								Integer.parseInt(temps2[i * 4 + 4 + 2]),
								Integer.parseInt(temps2[i * 4 + 4]),
								Integer.parseInt(temps2[i * 4 + 4 + 3])));
					}
				}
				//}
			}
			pos1 = msg.indexOf("GLGSV", pos1) + temps.length();
		}

		pos1=0;
		if (msg.indexOf("GPGGA", pos1) > 0) {
			pos1 = msg.indexOf("GPGGA", pos1);
			pos2 = msg.indexOf("*", pos1) + 3;
			String temps = msg.substring(pos1, pos2);

			if (IsCorrectchecksum(temps))
			{
				pos2 = temps.indexOf("*", 0);
				temps = temps.substring(0, pos2);

				String[] temps2 = temps.split(",");
				boolean check = true;
				if (temps2[1].equals("")||temps2[2].equals("")||temps2[4].equals("")) {

					check=false;
				}
				if(check)
				{
					UTC=(int)Double.parseDouble(temps2[1]);
					switch (Integer.parseInt(temps2[6]))
					{
						case 0:
							GPSStatus="4";
							break;
						case 1:
							GPSStatus="5";
							break;
						case 2:
							GPSStatus="6";
							break;
					}
					double a = Double.parseDouble(temps2[2]);
					a = a / 100;
					double b = a - (int) Math.floor(a);
					b = (int) Math.floor(a) + b * 100 / 60;

					double c = Double.parseDouble(temps2[4]);
					c = c / 100;
					double d = c - (int) Math.floor(c);
					d = (int) Math.floor(c) + d * 100 / 60;

					if (temps2[3].equals("N")) {
						lat = b;
					} else {
						lat = -b;
					}
					if (temps2[5].equals("E")) {
						lon = d;
					} else {
						lon = -d;
					}

				}
			}
		}

		pos1=0;
		if (msg.indexOf("GNGGA", pos1) > 0) {
			pos1 = msg.indexOf("GNGGA", pos1);
			pos2 = msg.indexOf("*", pos1) + 3;
			String temps = msg.substring(pos1, pos2);
		
			if (IsCorrectchecksum(temps))
			{
				pos2 = temps.indexOf("*", 0);
				temps = temps.substring(0, pos2);

				String[] temps2 = temps.split(",");
				boolean check = true;
				if (temps2[1].equals("")||temps2[2].equals("")||temps2[4].equals("")) {
				
					check=false;
				}
				if(check)
				{
					UTC=(int)Double.parseDouble(temps2[1]);

					switch (Integer.parseInt(temps2[6]))
					{
						case 0:
							GPSStatus="4";
							break;
						case 1:
							GPSStatus="5";
							break;
						case 2:
							GPSStatus="6";
							break;
					}

					double a = Double.parseDouble(temps2[2]);
					a = a / 100;
					double b = a - (int) Math.floor(a);
					b = (int) Math.floor(a) + b * 100 / 60;

					double c = Double.parseDouble(temps2[4]);
					c = c / 100;
					double d = c - (int) Math.floor(c);
					d = (int) Math.floor(c) + d * 100 / 60;

					if (temps2[3].equals("N")) {
						lat = b;
					} else {
						lat = -b;
					}
					if (temps2[5].equals("E")) {
						lon = d;
					} else {
						lon = -d;
					}
				
				}
			}
		}
		
		
		
		
	}
	
	public boolean IsCorrectchecksum(String inputstring)
	{
	  char[] cArray = inputstring.toCharArray();
	  int checksum = 0;
		for (int l = 0; l < (cArray.length - 3); l++) {
			checksum = cArray[l] ^ checksum;

		}
	  if(checksum == (Character
				.getNumericValue(cArray[cArray.length - 2]) * 16 + Character
				.getNumericValue(cArray[cArray.length - 1])))
	  return true;
	  else
	  return false;	  
	}
	
	
	
}
