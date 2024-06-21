package org.traccar.model;


import java.util.Date;


public class Alert extends Position{
	
	 private String uuid;

    public String getUuid () {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
	
	private Date gps_time;
        
        private String gps_location;

    public void setGps_location(String gps_location) {
        this.gps_location = gps_location;
    }

    public String getGps_location() {
        return gps_location;
    }
    
	
	public Date getGps_time() {
		return gps_time;
	}
	public void setGps_time(Date gps_time) {
		this.gps_time = gps_time;
	}
private int al_type=0;
	public int getAl_type() {
		return al_type;
	}
	public void setAl_type(int al_type) {
		this.al_type = al_type;
	}
        
      	private String al_type_name;

        public String getAl_type_name() {
		return al_type_name;
	}
	public void setAl_type_name(String al_type_name) {
		this.al_type_name = al_type_name;
	}
	
	private int observationid;
	
	public int getObservationid() {
		return observationid;
	}
	public void setObservationid(int observationid) {
		this.observationid = observationid;
	}
	private int key_id=0;
	
	public int getKey_id() {
		return key_id;
	}
	public void setKey_id(int key_id) {
		this.key_id = key_id;
	}
	
}	