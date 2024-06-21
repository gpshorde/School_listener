/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.model;

import java.sql.Timestamp;

/**
 *
 * @author vishal
 */
public class DSCHisory {
    private long web_jar_id ;
    private String jar_version;
    private String jar_info;
    private String jar_name;
    private Timestamp created_time;
    private String jar_useinfo;

    public long getWeb_jar_id() {
        return web_jar_id;
    }

    public String getJar_version() {
        return jar_version;
    }

    public String getJar_info() {
        return jar_info;
    }

    public String getJar_name() {
        return jar_name;
    }

    public Timestamp getCreated_time() {
        return created_time;
    }

  

    public void setWeb_jar_id(long web_jar_id) {
        this.web_jar_id = web_jar_id;
    }

    public void setJar_version(String jar_version) {
        this.jar_version = jar_version;
    }

    public void setJar_info(String jar_info) {
        this.jar_info = jar_info;
    }

    public void setJar_name(String jar_name) {
        this.jar_name = jar_name;
    }

    public void setCreated_time(Timestamp created_time) {
        this.created_time = created_time;
    }

    public String getJar_useinfo() {
        return jar_useinfo;
    }

    public void setJar_useinfo(String jar_useinfo) {
        this.jar_useinfo = jar_useinfo;
    }

   
    
    
    
    
    
    

   
    
    
    
    
}
