/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.model;

import com.vividsolutions.jts.geom.Geometry;
import java.util.Date;

/**
 *
 * @author krishna
 */
public class LiveEvents extends Position {
    private int observation_id;   
    private Date event_time;
    private int event_type = 0;
    private String event_type_name ;

    public int getObservation_id() {
        return observation_id;
    }

    public int getEvent_type() {
        return event_type;
    }

    public void setObservation_id(int observation_id) {
        this.observation_id = observation_id;
    }

    public void setEvent_type(int event_type) {
        this.event_type = event_type;
    }

  

    public void setEvent_time(Date event_time) {
        this.event_time = event_time;
    }

    public void setEvent_type_name(String event_type_name) {
        this.event_type_name = event_type_name;
    }

    public Date getEvent_time() {
        return event_time;
    }

  
    public String getEvent_type_name() {
        return event_type_name;
    }
//    
//    private Geometry gps_location;
//
//    public Geometry getGps_location() {
//        return gps_location;
//    }
//
//    public void setGps_location(Geometry gps_location) {
//        this.gps_location = gps_location;
//    }
    
    private String gps_location;

    public void setGps_location(String gps_location) {
        this.gps_location = gps_location;
    }

    public String getGps_location() {
        return gps_location;
    }
    
    
    
        
}
