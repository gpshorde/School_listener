/*
 * Copyright 2015 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.json.Json;

import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.traccar.Context;
import org.traccar.database.ActiveDevice;
import org.traccar.helper.JsonConverter;
import org.traccar.model.Command;




public class CommandServlet extends BaseServlet {
    
    @Override
    protected boolean handle(String command, HttpServletRequest req, HttpServletResponse resp) throws Exception {

        switch (command) {
            case "/send":
                send(req, resp);
                return true;
            case "/m":
                raw(req, resp);
                System.out.println("raw called--------------->>");
                return true;              
            case "/audio":
                sendAudio(req, resp);
                return true;    
            default:
                return false;
        }
    }

    

	public ActiveDevice getActiveDevice(long deviceId) {
        ActiveDevice activeDevice = Context.getConnectionManager().getActiveDevice(deviceId);
        if (activeDevice == null) {
            throw new RuntimeException("Your device is not connected with server please try to connect first.");
        }
        return activeDevice;
    }

    private void send(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        
        System.out.println("org.traccar.web.CommandServlet.send()---------------->>"+resp.toString());
        Command command = JsonConverter.objectFromJson(req.getReader(), Command.class);
        //Context.getPermissionsManager().checkDevice(getUserId(req), command.getDeviceId());
        getActiveDevice(command.getDeviceId()).sendCommand(command);
        sendResponse(resp.getWriter(), true);
    }

    /* make sure pass the two(##) sign from browser for getting single (#) at backend */
            
    private void raw(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //JsonObject json = Json.createReader(req.getReader()).readObject();
        //long deviceId = json.getJsonNumber("deviceId").longValue();
        //String command = json.getString("command");
        //req.setCharacterEncoding("UTF-8");
    	
    	long deviceId = Long.parseLong(req.getParameter("device_id"));
    	
    	//System.out.println("Device id found from database------------ after check==="+deviceId);
        //String command = req.getParameter("command")+"#".toString();
        String command = req.getParameter("command");
        String type = req.getParameter("type");
       
        if(type.equals("") && type.length()==0){
        	sendResponse(resp.getWriter());
        }
        //command = URLEncoder.encode(command, "UTF-8");
//        System.out.println("Decode parameter--------"+req);
        //Context.getPermissionsManager().checkDevice(getUserId(req), deviceId);
        
        
        getActiveDevice(deviceId).write(command,type,resp,req);

        sendResponse(resp.getWriter(), true);
    }
    
    
    private void sendAudio(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	//resp.setContentType("multipart/x-mixed-replace;boundary=END");

    	//System.out.println("Name is----------------------------->"+req.getParameter("name"));
    	
    	byte[] b= req.getParameter("at").getBytes();
    	
    	
    	//System.out.println("Attachment length is----------------------------->"+b.length);
    	//System.out.println("Attachment is----------------------------->"+new String(b));
    	
    	sendResponse(resp.getWriter(), true);
	}
    

	
}
