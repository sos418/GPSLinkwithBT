package com.example.gpslinkwithbluetooth;

import java.util.Date;

public class LocationData {
	private double lat,lng;
	private Date time;
	public LocationData(double in_lat,double in_lng,Date in_time)
	{
		lat=in_lat;
		lng=in_lng;
		time=in_time;
		
	}
	public double getlat(){
		return lat;
	}
	public double getlng(){
		return lng;
	}
	public Date gettime(){
		return time;
	}
	
	
	
}
