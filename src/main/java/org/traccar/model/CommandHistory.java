/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.traccar.model;

import java.util.Date;

/**
 *
 * @author aspl25
 */
public class CommandHistory extends Position {
    
    private int business_device_id;
    private int device_id;
     private Date created_time;
     private  String data;
     private  String protocol;

   

    /**
     * @return the business_device_id
     */
    public int getBusiness_device_id() {
        return business_device_id;
    }

    /**
     * @param business_device_id the business_device_id to set
     */
    public void setBusiness_device_id(int business_device_id) {
        this.business_device_id = business_device_id;
    }

    /**
     * @return the device_id
     */
    public int getDevice_id() {
        return device_id;
    }

    /**
     * @param device_id the device_id to set
     */
    public void setDevice_id(int device_id) {
        this.device_id = device_id;
    }

    /**
     * @return the created_time
     */
    public Date getCreated_time() {
        return created_time;
    }

    /**
     * @param created_time the created_time to set
     */
    public void setCreated_time(Date created_time) {
        this.created_time = created_time;
    }

    /**
     * @return the Data
     */
    public String getData() {
        return data;
    }

    /**
     * @param Data the Data to set
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
     
     
 
}
