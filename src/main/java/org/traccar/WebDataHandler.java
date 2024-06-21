/*
 * Copyright 2015 - 2020 Anton Tananaev (anton@traccar.org)
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
package org.traccar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandler;
import io.netty.util.Timer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.database.IdentityManager;
import org.traccar.helper.Checksum;
import org.traccar.model.*;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import java.util.HashMap;
import java.util.Map;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.json.JSONObject;
import org.traccar.common.AlertType;

@ChannelHandler.Sharable
public class WebDataHandler extends BaseDataHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebDataHandler.class);

    private static final String KEY_POSITION = "position";
    private static final String KEY_DEVICE = "device";
    private static final String KEY_EVENET = "event";

    private final IdentityManager identityManager;
    private final ObjectMapper objectMapper;
    private final Client client;

    private final String url;
    private final String header;
    private final boolean json;
    private final boolean urlVariables;

    private final boolean retryEnabled;
    private final int retryDelay;
    private final int retryCount;
    private final int retryLimit;

    private AtomicInteger deliveryPending;

    @Inject
    public WebDataHandler(
            Config config, IdentityManager identityManager, ObjectMapper objectMapper, Client client) {

        this.identityManager = identityManager;
        this.objectMapper = objectMapper;
        this.client = client;
        this.url = config.getString(Keys.FORWARD_URL);
        this.header = config.getString(Keys.FORWARD_HEADER);
        this.json = config.getBoolean(Keys.FORWARD_JSON);
        this.urlVariables = config.getBoolean(Keys.FORWARD_URL_VARIABLES);

        this.retryEnabled = config.getBoolean(Keys.FORWARD_RETRY_ENABLE);
        this.retryDelay = config.getInteger(Keys.FORWARD_RETRY_DELAY, 100);
        this.retryCount = config.getInteger(Keys.FORWARD_RETRY_COUNT, 10);
        this.retryLimit = config.getInteger(Keys.FORWARD_RETRY_LIMIT, 100);

        this.deliveryPending = new AtomicInteger(0);
    }

    private static String formatSentence(Position position) {

        StringBuilder s = new StringBuilder("$GPRMC,");

        try (Formatter f = new Formatter(s, Locale.ENGLISH)) {

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
            calendar.setTimeInMillis(position.getFixTime().getTime());

            f.format("%1$tH%1$tM%1$tS.%1$tL,A,", calendar);

            double lat = position.getLatitude();
            double lon = position.getLongitude();

            f.format("%02d%07.4f,%c,", (int) Math.abs(lat), Math.abs(lat) % 1 * 60, lat < 0 ? 'S' : 'N');
            f.format("%03d%07.4f,%c,", (int) Math.abs(lon), Math.abs(lon) % 1 * 60, lon < 0 ? 'W' : 'E');

            f.format("%.2f,%.2f,", position.getSpeed(), position.getCourse());
            f.format("%1$td%1$tm%1$ty,,", calendar);
        }

        s.append(Checksum.nmea(s.toString()));

        return s.toString();
    }

    private String calculateStatus(Position position) {
        if (position.getAttributes().containsKey(Position.KEY_ALARM)) {
            return "0xF841"; // STATUS_PANIC_ON
        } else if (position.getSpeed() < 1.0) {
            return "0xF020"; // STATUS_LOCATION
        } else {
            return "0xF11C"; // STATUS_MOTION_MOVING
        }
    }

    public String formatRequest(Position position) throws UnsupportedEncodingException, JsonProcessingException {

        Device device = identityManager.getById(position.getDeviceId());
//        System.out.println("hello my name is trupti");

        String request = url
//                .replace("{name}", URLEncoder.encode(device.getName(), StandardCharsets.UTF_8.name()))
//                .replace("{uniqueId}", device.getUniqueId());
//                .replace("{uniqueId}", "017195301069");

//                .replace("{status}", device.getStatus())
                .replace("{deviceId}", String.valueOf(position.getDeviceId()));
//                .replace("{protocol}", String.valueOf(position.getProtocol()))
//                .replace("{deviceTime}", String.valueOf(position.getDeviceTime().getTime()))
//                .replace("{fixTime}", String.valueOf(position.getFixTime().getTime()))
//                .replace("{valid}", String.valueOf(position.getValid()))
//                .replace("{latitude}", "4545")
//                .replace("{longitude}", "4545")
//                .replace("{altitude}", "445454")
//                .replace("{speed}", "4545");
//                .replace("{course}", String.valueOf(position.getCourse()))
//                .replace("{accuracy}", String.valueOf(position.getAccuracy()))
//                .replace("{statusCode}", calculateStatus(position));
//        System.out.println("hello my name is test");

//        if (position.getAddress() != null) {
//            request = request.replace(
//                    "{address}", URLEncoder.encode(position.getAddress(), StandardCharsets.UTF_8.name()));
//        }
//
//        if (request.contains("{attributes}")) {
//            String attributes = objectMapper.writeValueAsString(position.getAttributes());
//            request = request.replace(
//                    "{attributes}", URLEncoder.encode(attributes, StandardCharsets.UTF_8.name()));
//        }
//
//        if (request.contains("{gprmc}")) {
//            request = request.replace("{gprmc}", formatSentence(position));
//        }
//
//        if (request.contains("{group}")) {
//            String deviceGroupName = "";
//            if (device.getGroupId() != 0) {
//                Group group = Context.getGroupsManager().getById(device.getGroupId());
//                if (group != null) {
//                    deviceGroupName = group.getName();
//                }
//            }
//
//            request = request.replace("{group}", URLEncoder.encode(deviceGroupName, StandardCharsets.UTF_8.name()));
//        }
//System.out.println("request"+request);
        return request;
    }

    class AsyncRequestAndCallback implements InvocationCallback<Response>, TimerTask {

        private int retries = 0;
        private Map<String, Object> payload;
        private final Invocation.Builder requestBuilder;
        private MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;

        AsyncRequestAndCallback(Position position, Alert event) throws JsonProcessingException, Exception {

            String formattedUrl;
            try {
                formattedUrl = json && !urlVariables ? url : formatRequest(position);
//                System.out.println("formattedUrl"+formattedUrl);
            } catch (UnsupportedEncodingException | JsonProcessingException e) {
                throw new RuntimeException("Forwarding formatting error", e);
            }

            requestBuilder = client.target(formattedUrl).request();
            if (header != null && !header.isEmpty()) {
                for (String line: header.split("\\r?\\n")) {
                    String[] values = line.split(":", 2);
                    String headerName = values[0].trim();
                    String headerValue = values[1].trim();
                    if (headerName.equals(HttpHeaders.CONTENT_TYPE)) {
                        mediaType = MediaType.valueOf(headerValue);
                    } else {
                        requestBuilder.header(headerName, headerValue);
                    }
                }
            }

            if (json) {
                payload = prepareJsonPayload(position);
//                System.out.println("payload"+payload);

            }

            deliveryPending.incrementAndGet();
        }

        private void send() {
            if (json) {
//                System.out.println("json"+json);

                try {
                    Entity<String> entity = Entity.entity(objectMapper.writeValueAsString(payload), mediaType);
                    requestBuilder.async().post(entity, this);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to serialize location to json", e);
                }
            } else {
                requestBuilder.async().get(this);
            }
        }

        private void retry() {
            boolean scheduled = false;
            try {
                if (retryEnabled && deliveryPending.get() <= retryLimit && retries < retryCount) {
                    schedule();
                    scheduled = true;
                }
            } finally {
                int pending = scheduled ? deliveryPending.get() : deliveryPending.decrementAndGet();
                LOGGER.warn("Position forwarding failed: " + pending + " pending");
            }
        }

        private void schedule() {
            Main.getInjector().getInstance(Timer.class).newTimeout(
                this, retryDelay * (int) Math.pow(2, retries++), TimeUnit.MILLISECONDS);
        }

        @Override
        public void completed(Response response) {
            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                deliveryPending.decrementAndGet();
            } else {
                retry();
            }
        }

        @Override
        public void failed(Throwable throwable) {
            retry();
        }

        @Override
        public void run(Timeout timeout) {
            boolean sent = false;
            try {
                if (!timeout.isCancelled()) {
                    send();
                    sent = true;
                }
            } finally {
                if (!sent) {
                    deliveryPending.decrementAndGet();
                }
            }
        }

    }


    @Override
    protected Position handlePosition(Position position) throws JsonProcessingException {
        Alert event = null;
        AsyncRequestAndCallback request = null;
        try {
            request = new AsyncRequestAndCallback(position, event);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(WebDataHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        request.send();

        return position;    }

    @Override
    protected Position handlePosition(Position position,Alert event) throws JsonProcessingException {

        AsyncRequestAndCallback request = null;
        try {
            request = new AsyncRequestAndCallback(position,event);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(WebDataHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        request.send();

        return position;
    }

//    private Map<String, Object> prepareJsonPayload(Position position) throws JsonProcessingException {
//
//        Map<String, Object> data = new HashMap<>();
//        Device device = identityManager.getDeviceById(position.getDeviceId());
//        ObjectMapper mapper = new ObjectMapper();
//        System.out.println("ignition"+position.getIgnition());
//        if (position.getIgnition()==2) {
//            Alert alert = new Alert();
//            alert.setAl_type_name("ignition_off");
//            alert.setAl_type(2);
//            alert.setLatitude(position.getLatitude());
//            alert.setLongitude(position.getLongitude());
//            alert.setDeviceId(position.getDeviceId());
//            alert.setGps_time(position.getDeviceTime());
//            alert.setUuid(position.getUuid());
//            alert.setObservationid((int) position.getId());
//            String jsona = mapper.writeValueAsString(alert);
//            System.out.println("ResultingJSONstring = " + jsona);
//            data.put(KEY_EVENET, alert);
//        }
//        if (position.getIgnition()==1) {
//            Alert alert = new Alert();
//            alert.setAl_type_name("ignition_on");
//            alert.setAl_type(1);
//            alert.setLatitude(position.getLatitude());
//            alert.setLongitude(position.getLongitude());
//            alert.setUuid(position.getUuid());
//            alert.setDeviceId(position.getDeviceId());
//            alert.setGps_time(position.getDeviceTime());
//            alert.setUuid(position.getUuid());
//            alert.setObservationid((int) position.getId());
//            String jsona= mapper.writeValueAsString(alert);
//            System.out.println("ResultingJSONstring = " + jsona);
//            data.put(KEY_EVENET, alert);
//        }
//        if (position.getIgnition()==2) {
//            Alert alert = new Alert();
//            alert.setAl_type_name("ignition_off");
//            alert.setAl_type(2);
//            alert.setUuid(position.getUuid());
//            alert.setDeviceId(position.getDeviceId());
//            alert.setGps_time(position.getDeviceTime());
//            alert.setUuid(position.getUuid());
//            alert.setObservationid((int) position.getId());
//            String jsona = mapper.writeValueAsString(alert);
//            System.out.println("ResultingJSONstring = " + jsona);
//            data.put(KEY_EVENET, alert);
//
//
//
//        }
//
//        data.put(KEY_POSITION, position);
////             position.setUuid(java.util.UUID.randomUUID().toString());
//
//        System.out.println("&&&&&&&&&&& position &&&&&&&&&&&&&7"+position.getUuid());
//        System.out.println("&&&&&&&&&&& position &&&&&&&&&&&&&7"+position.getAddress());
//
//
//        if (device != null) {
//            data.put(KEY_DEVICE, device);
//
//        }
//        System.out.println("");
//        System.out.println("data"+data);
//        System.out.println("");
//
//
//        return data;
////    }
//     @Override
//    protected Position handlePosition(Position position) throws Exception {
//        Alert event = null;
//        
//        System.out.println("org.traccar.WebDataHandler.handlePosition()");
//        AsyncRequestAndCallback request = new AsyncRequestAndCallback(position, event);
//        request.send();
//
//        return position;    }
//
//    @Override
//    protected Position handlePosition(Position position,Alert event) throws Exception {
//        System.out.println("handlePosition with alert");
//
//        AsyncRequestAndCallback request = new AsyncRequestAndCallback(position,event);
//        request.send();
//
//        return position;
//    }

    private Map<String, Object> prepareJsonPayload(Position position) throws Exception {

        Map<String, Object> data = new HashMap<>();
        Device device = identityManager.getDeviceById(position.getDeviceId());
        ObjectMapper mapper = new ObjectMapper();
//        String addr1="N/A";
        String addr1="";

        JSONObject J= new JSONObject(position);
        //System.out.println("J==============>"+J);
       addr1 = NominatimCall.sendGet(position.getLatitude(), position.getLongitude());

//        System.out.println("getLatitude----------------------------------------"+position.getLatitude());
//        System.out.println("getLongitude----------------------------------------"+position.getLongitude());

//        addr1 = Context.getGeocoder().getAddress(position.getLatitude(), position.getLongitude(),null);

        position.setBusiness_device_id(position.getBusiness_device_id());
        position.setAddress(addr1);
//        System.out.println("ATTRIBUTES_----------------------------------------"+addr1);


       position.setLatitude( position.getLatitude());
       position.setLongitude(position.getLongitude());
      position.setIgnition(position.getIgnition());


//       System.out.println("ATTRIBUTES_----------------------------------------");
//       System.out.println(""+position.getAttributes());
//        System.out.println("ATTRIBUTES_----------------------------------------");



//        System.out.println("set in postion table");
//        System.out.println("ignition"+position.getIgnition());
        if (position.getIgnition()==2) {
            Alert alert = new Alert();
            alert.setAl_type_name("ignition_off");
            alert.setAl_type(AlertType.IGNITION_OFF);
            alert.setLatitude(position.getLatitude());
            alert.setLongitude(position.getLongitude());
            alert.setDeviceId(position.getDeviceId());
            alert.setGps_time(position.getDeviceTime());
            alert.setBusiness_device_id(position.getBusiness_device_id());
            String addr="";
           addr = NominatimCall.sendGet(position.getLatitude(), position.getLongitude());

            alert.setAddress(addr);
//            System.out.println("address"+addr);
           // alert.setUuid(position.get);
          alert.setUuid(position.getUuid());

          alert.setObservationid((int) position.getId());
//            String jsona = mapper.writeValueAsString(alert);
//            System.out.println("ResultingJSONstring = " + jsona);
            data.put(KEY_EVENET, alert);
        }
        if (position.getIgnition()==1) {
            Alert alert = new Alert();
            alert.setAl_type_name("ignition_on");
            alert.setAl_type(AlertType.IGNITION_ON);
            alert.setLatitude(position.getLatitude());
            alert.setLongitude(position.getLongitude());
            alert.setUuid(position.getUuid());
//            System.out.println("uuid"+alert.getUuid());
            alert.setBusiness_device_id(position.getBusiness_device_id());

            alert.setDeviceId(position.getDeviceId());
            alert.setGps_time(position.getDeviceTime());
            alert.setUuid(position.getUuid());
            String addr="";
           addr = NominatimCall.sendGet(position.getLatitude(), position.getLongitude());

            alert.setAddress(addr);
//            System.out.println("address"+addr);
            alert.setObservationid((int) position.getId());
            String jsona= mapper.writeValueAsString(alert);
//            System.out.println("ResultingJSONstring = " + jsona);
            data.put(KEY_EVENET, alert);
        }
        if (position.getAttributes().equals("ignition")) {
            if (position.getAttributes().get("ignition").equals("false")) {
                Alert alert = new Alert();
                alert.setAl_type_name("ignition_off");
                alert.setAl_type(AlertType.IGNITION_OFF);
                alert.setLatitude(position.getLatitude());
                alert.setLongitude(position.getLongitude());
                alert.setDeviceId(position.getDeviceId());
                alert.setGps_time(position.getDeviceTime());
                alert.setBusiness_device_id(position.getBusiness_device_id());
                String addr ="";
                addr = NominatimCall.sendGet(position.getLatitude(), position.getLongitude());

                alert.setAddress(addr);
//                System.out.println("address" + addr);
                // alert.setUuid(position.get);
                alert.setUuid(position.getUuid());

                alert.setObservationid((int) position.getId());
                String jsona = mapper.writeValueAsString(alert);
//                System.out.println("ResultingJSONstring = " + jsona);
                data.put(KEY_EVENET, alert);
            }
        }
        if (position.getAttributes().equals("ignition")) {
            if (position.getAttributes().get("ignition").equals("true")) {
                Alert alert = new Alert();
                alert.setAl_type_name("ignition_on");
                alert.setAl_type(AlertType.IGNITION_ON);
                alert.setLatitude(position.getLatitude());
                alert.setLongitude(position.getLongitude());
                alert.setUuid(position.getUuid());
//                System.out.println("uuid" + alert.getUuid());
                alert.setBusiness_device_id(position.getBusiness_device_id());

                alert.setDeviceId(position.getDeviceId());
                alert.setGps_time(position.getDeviceTime());
                alert.setUuid(position.getUuid());
                String addr  ="";
                addr = NominatimCall.sendGet(position.getLatitude(), position.getLongitude());

                alert.setAddress(addr);
//                System.out.println("address" + addr);
                alert.setObservationid((int) position.getId());
                String jsona = mapper.writeValueAsString(alert);
//                System.out.println("ResultingJSONstring = " + jsona);
                data.put(KEY_EVENET, alert);
            }
        }

//        String power = String.valueOf(position.getAttributes().containsValue("power"));
//        System.out.println("power"+power);
//          boolean powercut = Boolean.parseBoolean(power);
//        System.out.println("powercut"+powercut);
//
//        if (powercut =true) {
//
//           System.out.println("powercut getting=======================>");
//
//            Alert alert = new Alert();
//            alert.setAl_type_name("powercut");
//            alert.setAl_type(AlertType.POWER_CUTT);
//            alert.setLatitude(position.getLatitude());
//            alert.setLongitude(position.getLongitude());
//            alert.setUuid(position.getUuid());
//            System.out.println("uuid"+alert.getUuid());
//            alert.setBusiness_device_id(position.getBusiness_device_id());
//
//            alert.setDeviceId(position.getDeviceId());
//            alert.setGps_time(position.getDeviceTime());
//            alert.setUuid(position.getUuid());
//            String addr=null;
//            addr = NominatimCall.sendGet(position.getLatitude(), position.getLongitude());
//
//            alert.setAddress(addr);
//            System.out.println("address"+addr);
//            alert.setObservationid((int) position.getId());
//            String jsona= mapper.writeValueAsString(alert);
//            System.out.println("ResultingJSONstring = " + jsona);
//            data.put(KEY_EVENET, alert);
//        }
//        System.out.println("erorrrrrrrrrrrrrrrrrrrrrrr");
        if (position.getAttributes().equals("rampoff")) {
            if (position.getAttributes().get("ramp").equals("on")) {
                Alert alert = new Alert();
                alert.setAl_type_name("rampoff");
                alert.setAl_type(AlertType.RAMP_OFF);

                alert.setUuid(position.getUuid());
                alert.setDeviceId(position.getDeviceId());
                alert.setGps_time(position.getDeviceTime());
                alert.setUuid(position.getUuid());
                alert.setBusiness_device_id(position.getBusiness_device_id());

                String addr="";
                addr = NominatimCall.sendGet(position.getLatitude(), position.getLongitude());

                alert.setAddress(addr);
//                System.out.println("address" + addr);
                alert.setObservationid((int) position.getId());
                String jsona = mapper.writeValueAsString(alert);
//                System.out.println("ResultingJSONstring = " + jsona);
                data.put(KEY_EVENET, alert);

            } else if (position.getAttributes().equals("ramp")) {
                Alert alert = new Alert();
                alert.setAl_type_name("rampon");
                alert.setAl_type(AlertType.RAMP_ON);

                alert.setUuid(position.getUuid());
                alert.setDeviceId(position.getDeviceId());
                alert.setGps_time(position.getDeviceTime());
                alert.setUuid(position.getUuid());
                alert.setBusiness_device_id(position.getBusiness_device_id());

                String addr ="";
               addr = NominatimCall.sendGet(position.getLatitude(), position.getLongitude());

                alert.setAddress(addr);
//                System.out.println("address" + addr);
                alert.setObservationid((int) position.getId());
                String jsona = mapper.writeValueAsString(alert);
//                System.out.println("ResultingJSONstring = " + jsona);
                data.put(KEY_EVENET, alert);


            }
        }
        if (position.getAttributes().equals("ac")) {

            Alert alert = new Alert();
            alert.setAl_type_name("ACON");
            alert.setAl_type(AlertType.AC_ON);
            alert.setUuid(position.getUuid());
            alert.setDeviceId(position.getDeviceId());
            alert.setGps_time(position.getDeviceTime());
            alert.setUuid(position.getUuid());
            alert.setBusiness_device_id(position.getBusiness_device_id());

            String addr="";
            addr = NominatimCall.sendGet(position.getLatitude(), position.getLongitude());

            alert.setAddress(addr);
//            System.out.println("address"+addr);
            alert.setObservationid((int) position.getId());
            String jsona = mapper.writeValueAsString(alert);
//            System.out.println("ResultingJSONstring = " + jsona);
            data.put(KEY_EVENET, alert);



        }

    if(position.getAttributes().equals("alarm")){

        if(position.getAttributes().get("alarm").equals("overspeed")){
            Alert alert = new Alert();
            alert.setAl_type_name("overspeed");
            alert.setAl_type(AlertType.OVER_SPEPED);

            alert.setUuid(position.getUuid());
            alert.setDeviceId(position.getDeviceId());
            alert.setGps_time(position.getDeviceTime());
            alert.setUuid(position.getUuid());
            alert.setBusiness_device_id(position.getBusiness_device_id());

            String addr="";
            addr = NominatimCall.sendGet(position.getLatitude(), position.getLongitude());

            alert.setAddress(addr);
//            System.out.println("address"+addr);
            alert.setObservationid((int) position.getId());
            String jsona = mapper.writeValueAsString(alert);
//            System.out.println("ResultingJSONstring = " + jsona);
            data.put(KEY_EVENET, alert);
        }
        else if(position.getAttributes().get("alarm").equals("fallDown"))
        {
            Alert alert = new Alert();
            alert.setAl_type_name("fallDown");
            alert.setAl_type(AlertType.FALLDOWN);

            alert.setUuid(position.getUuid());
            alert.setDeviceId(position.getDeviceId());
            alert.setGps_time(position.getDeviceTime());
            alert.setUuid(position.getUuid());
            alert.setBusiness_device_id(position.getBusiness_device_id());

            String addr="";
            addr = NominatimCall.sendGet(position.getLatitude(), position.getLongitude());

            alert.setAddress(addr);
//            System.out.println("address"+addr);
            alert.setObservationid((int) position.getId());
            String jsona = mapper.writeValueAsString(alert);
//            System.out.println("ResultingJSONstring = " + jsona);
            data.put(KEY_EVENET, alert);
        }
        else if(position.getAttributes().get("alarm").equals("Crash")){
            Alert alert = new Alert();
            alert.setAl_type_name("Crash");
            alert.setAl_type(AlertType.Crash);

            alert.setUuid(position.getUuid());
            alert.setDeviceId(position.getDeviceId());
            alert.setGps_time(position.getDeviceTime());
            alert.setUuid(position.getUuid());
            alert.setBusiness_device_id(position.getBusiness_device_id());

            String addr="";
           addr = NominatimCall.sendGet(position.getLatitude(), position.getLongitude());

            alert.setAddress(addr);
//            System.out.println("address"+addr);
            alert.setObservationid((int) position.getId());
            String jsona = mapper.writeValueAsString(alert);
//            System.out.println("ResultingJSONstring = " + jsona);
            data.put(KEY_EVENET, alert);
        }
        else if(position.getAttributes().get("alarm").equals("HARD_CORNERING")){
            Alert alert = new Alert();
            alert.setAl_type_name("HARD_CORNERING");
            alert.setAl_type(AlertType.HARD_CORNERING);

            alert.setUuid(position.getUuid());
            alert.setDeviceId(position.getDeviceId());
            alert.setGps_time(position.getDeviceTime());
            alert.setUuid(position.getUuid());
            alert.setBusiness_device_id(position.getBusiness_device_id());

            String addr="";
           addr = NominatimCall.sendGet(position.getLatitude(), position.getLongitude());

            alert.setAddress(addr);
//            System.out.println("address"+addr);
            alert.setObservationid((int) position.getId());
            String jsona = mapper.writeValueAsString(alert);
//            System.out.println("ResultingJSONstring = " + jsona);
            data.put(KEY_EVENET, alert);
        }
        else if(position.getAttributes().get("alarm").equals("sos")){
            Alert alert = new Alert();
            alert.setAl_type_name("sos");
            alert.setAl_type(AlertType.SOS);

            alert.setUuid(position.getUuid());
            alert.setDeviceId(position.getDeviceId());
            alert.setGps_time(position.getDeviceTime());
            alert.setUuid(position.getUuid());
            alert.setBusiness_device_id(position.getBusiness_device_id());

            String addr="";
            addr = NominatimCall.sendGet(position.getLatitude(), position.getLongitude());

            alert.setAddress(addr);
//            System.out.println("address"+addr);
            alert.setObservationid((int) position.getId());
            String jsona = mapper.writeValueAsString(alert);
//            System.out.println("ResultingJSONstring = " + jsona);
            data.put(KEY_EVENET, alert);
        }

    }



        if(position.getAttributes().equals("door_close")) {

            if (position.getAttributes().get("door_close").equals("yes")) {
                Alert alert = new Alert();
                alert.setAl_type_name("door_close");
                alert.setAl_type(AlertType.DOOR_CLOSE);

                alert.setUuid(position.getUuid());
                alert.setDeviceId(position.getDeviceId());
                alert.setGps_time(position.getDeviceTime());
                alert.setUuid(position.getUuid());
                alert.setBusiness_device_id(position.getBusiness_device_id());

                String addr ="";
               addr = NominatimCall.sendGet(position.getLatitude(), position.getLongitude());

                alert.setAddress(addr);
//                System.out.println("address" + addr);
                alert.setObservationid((int) position.getId());
                String jsona = mapper.writeValueAsString(alert);
//                System.out.println("ResultingJSONstring = " + jsona);
                data.put(KEY_EVENET, alert);
            }
            else if (position.getAttributes().get("door_open").equals("yes")) {
                Alert alert = new Alert();
                alert.setAl_type(AlertType.DOOR_OPEN);

                alert.setAl_type_name("door_open");
                alert.setUuid(position.getUuid());
                alert.setDeviceId(position.getDeviceId());
                alert.setGps_time(position.getDeviceTime());
                alert.setUuid(position.getUuid());
                alert.setBusiness_device_id(position.getBusiness_device_id());

                String addr="";
              addr = NominatimCall.sendGet(position.getLatitude(), position.getLongitude());

                alert.setAddress(addr);
//                System.out.println("address" + addr);
                alert.setObservationid((int) position.getId());
                String jsona = mapper.writeValueAsString(alert);
//                System.out.println("ResultingJSONstring = " + jsona);
                data.put(KEY_EVENET, alert);
            }
        }
        if(position.getAttributes().equals("engine_idle")) {
            if (position.getAttributes().get("engine_idle").equals("start")) {
                Alert alert = new Alert();
                alert.setAl_type(AlertType.ENGINE_IDLE);

                alert.setAl_type_name("engine_idle");
                alert.setUuid(position.getUuid());
                alert.setDeviceId(position.getDeviceId());
                alert.setGps_time(position.getDeviceTime());
                alert.setUuid(position.getUuid());
                alert.setBusiness_device_id(position.getBusiness_device_id());

                String addr ="";
               addr = NominatimCall.sendGet(position.getLatitude(), position.getLongitude());

                alert.setAddress(addr);
//                System.out.println("address" + addr);
                alert.setObservationid((int) position.getId());
                String jsona = mapper.writeValueAsString(alert);
//                System.out.println("ResultingJSONstring = " + jsona);
                data.put(KEY_EVENET, alert);
            }
        }




        data.put(KEY_POSITION, position);
                System.out.println("data"+position);
//                                System.out.println("data"+position.getLatitude());


//             position.setUuid(java.util.UUID.randomUUID().toString());

//        System.out.println("&&&&&&&&&&& position &&&&&&&&&&&&&7"+position.getUuid());
//        System.out.println("&&&&&&&&&&& position &&&&&&&&&&&&&7"+position.getAddress());


        if (device != null) {
            data.put(KEY_DEVICE, device);

        }
//        System.out.println("");
        JSONObject aJ= new JSONObject(data);

        System.out.println("data=======================>>>"+aJ);
//        System.out.println("");


        return data;
    }

}
