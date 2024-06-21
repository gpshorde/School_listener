/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.model;

import java.util.Date;

/**
 *
 * @author addon10
 */
public class VideoData {

    private long video_id;
    private int business_device_id;
    private Date start_time;
    private Date end_time;
     private Date created_time;
    private int channel;

    public Date getCreated_time() {
        return created_time;
    }

    public void setCreated_time(Date created_time) {
        this.created_time = created_time;
    }

    
    
    public long getVideo_id() {
        return video_id;
    }

    public void setVideo_id(long video_id) {
        this.video_id = video_id;
    }
    
    

    public int getBusiness_device_id() {
        return business_device_id;
    }

    public Date getStart_time() {
        return start_time;
    }

    public Date getEnd_time() {
        return end_time;
    }

    public int getChannel() {
        return channel;
    }

    public void setBusiness_device_id(int business_device_id) {
        this.business_device_id = business_device_id;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }
    
    
}
