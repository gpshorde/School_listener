package org.traccar.model;

import java.util.Date;

public class LiveObervation extends Position{


	private int observation_id=0;
	
	private int device_id=0;
	
	private int date_diff=0;
	
	private Date gps_time;
        
        private Date servertime;
	
	private String extra=null;
	
	private int fuel=0;

        private int al_type=0;
	public int getAl_type() {
		return al_type;
	}
	public void setAl_type(int al_type) {
		this.al_type = al_type;
	}



 private int temperature = 0;
 
        public int getTemperature() {
		return temperature;
	}

	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}        
        private double cmileage=0.0;

    public double getCmileage() {
        return cmileage;
    }

    public void setCmileage(double cmileage) {
        this.cmileage = cmileage;
    }

    public Date getServertime() {
        return servertime;
    }

    public void setServertime(Date servertime) {
        this.servertime = servertime;
    }
        
        

        
	public int getObservation_id() {
		return observation_id;
	}
	public void setObservation_id(int observation_id) {
		this.observation_id = observation_id;
	}
	public int getDevice_id() {
		return device_id;
	}
	public void setDevice_id(int device_id) {
		this.device_id = device_id;
	}
	public Date getGps_time() {
		return gps_time;
	}
	public void setGps_time(Date gps_time) {
		this.gps_time = gps_time;
	}
	public int getDate_diff() {
		return date_diff;
	}
	public void setDate_diff(int date_diff) {
		this.date_diff = date_diff;
	}
	public String getExtra() {
		return extra;
	}
	public void setExtra(String extra) {
		this.extra = extra;
	}
	public int getFuel() {
		return fuel;
	}
	public void setFuel(int fuel) {
		this.fuel = fuel;
	}

	
	

}
