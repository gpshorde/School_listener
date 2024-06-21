/*
 * Copyright 2015 - 2018 Anton Tananaev (anton@traccar.org)
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
package org.traccar.database;

import io.netty.channel.Channel;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.GlobalTimer;
import org.traccar.Main;
import org.traccar.Protocol;
import org.traccar.handler.events.MotionEventHandler;
import org.traccar.handler.events.OverspeedEventHandler;
import org.traccar.model.Device;
import org.traccar.model.DeviceState;
import org.traccar.model.Event;
import org.traccar.model.Position;

import java.net.SocketAddress;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

    private static final long DEFAULT_TIMEOUT = 600;
    private final Map<Long, Position> positions = new HashMap<>();
    private final long deviceTimeout;
//    private final boolean enableStatusEvents;
//    private final boolean updateDeviceState;

    private final Map<Long, ActiveDevice> activeDevices = new ConcurrentHashMap<>();
    private final Map<Long, Set<UpdateListener>> listeners = new ConcurrentHashMap<>();
    private final Map<Long, Timeout> timeouts = new ConcurrentHashMap<>();

//    public ConnectionManager() {
//        deviceTimeout = Context.getConfig().getLong("status.timeout", DEFAULT_TIMEOUT) * 1000;
//        enableStatusEvents = Context.getConfig().getBoolean("event.enable");
//        updateDeviceState = Context.getConfig().getBoolean("status.updateDeviceState");
//    }

    public ConnectionManager(DataManager dataManager) {
        deviceTimeout = Context.getConfig().getLong("status.timeout", DEFAULT_TIMEOUT) * 1000;
//        enableStatusEvents = Context.getConfig().getBoolean("event.enable");
//        updateDeviceState = Context.getConfig().getBoolean("status.updateDeviceState");
        System.out.println("===================");

        System.out.println("deviceTimeout"+deviceTimeout);
        System.out.println("===================");

        if (dataManager != null) {
            try {
                for (Position position : dataManager.getLatestPositions()) {
                    positions.put(position.getDeviceId(), position);


                }


            } catch (SQLException error) {
                System.out.println("===================");

                System.out.println("error============="+error);
                System.out.println("===================");

                LOGGER.warn("error",error);
            }
        }
    }
  public void addActiveDevice(long deviceId, Protocol protocol, Channel channel, SocketAddress remoteAddress,String uniqueId) {
        activeDevices.put(deviceId, new ActiveDevice(deviceId, protocol, channel, remoteAddress,uniqueId));
    }
  

//    public void addActiveDevice(long deviceId, Protocol protocol, Channel channel, SocketAddress remoteAddress) {
//        activeDevices.put(deviceId, new ActiveDevice(deviceId, protocol, channel, remoteAddress));
//    }
   public Position getLastPosition(long deviceId) {
       System.out.println("deviceId wewewew"+deviceId);
        return positions.get(deviceId);
    }
//    public void removeActiveDevice(Channel channel) {
//        for (ActiveDevice activeDevice : activeDevices.values()) {
//            if (activeDevice.getChannel() == channel) {
//                updateDevice(activeDevice.getDeviceId(), Device.STATUS_OFFLINE, null);
//                activeDevices.remove(activeDevice.getDeviceId());
//                break;
//            }
//        }
//    }


    public ActiveDevice getActiveDevice(long deviceId) {
        return activeDevices.get(deviceId);
    }

//    public void updateDevice(final long deviceId, String status, Date time) {
//        Device device = Context.getIdentityManager().getById(deviceId);
//        if (device == null) {
//            return;
//        }
//
//        String oldStatus = device.getStatus();
//        device.setStatus(status);
//
//        if (enableStatusEvents && !status.equals(oldStatus)) {
//            String eventType;
//            Map<Event, Position> events = new HashMap<>();
//            switch (status) {
//                case Device.STATUS_ONLINE:
//                    eventType = Event.TYPE_DEVICE_ONLINE;
//                    break;
//                case Device.STATUS_UNKNOWN:
//                    eventType = Event.TYPE_DEVICE_UNKNOWN;
//                    if (updateDeviceState) {
//                        events.putAll(updateDeviceState(deviceId));
//                    }
//                    break;
//                default:
//                    eventType = Event.TYPE_DEVICE_OFFLINE;
//                    if (updateDeviceState) {
//                        events.putAll(updateDeviceState(deviceId));
//                    }
//                    break;
//            }
//            events.put(new Event(eventType, deviceId), null);
//            Context.getNotificationManager().updateEvents(events);
//        }
//
//        Timeout timeout = timeouts.remove(deviceId);
//        if (timeout != null) {
//            timeout.cancel();
//        }
//
//        if (time != null) {
//            device.setLastUpdate(time);
//        }
//
//        if (status.equals(Device.STATUS_ONLINE)) {
//            timeouts.put(deviceId, GlobalTimer.getTimer().newTimeout(new TimerTask() {
//                @Override
//                public void run(Timeout timeout) {
//                    if (!timeout.isCancelled()) {
//                        updateDevice(deviceId, Device.STATUS_UNKNOWN, null);
//                    }
//                }
//            }, deviceTimeout, TimeUnit.MILLISECONDS));
//        }
//
//        try {
//            Context.getDeviceManager().updateDeviceStatus(device);
//        } catch (SQLException error) {
//            LOGGER.warn("Update device status error", error);
//        }
//
//        updateDevice(device);
//    }

    public Map<Event, Position> updateDeviceState(long deviceId) {
        DeviceState deviceState = Context.getDeviceManager().getDeviceState(deviceId);
        Map<Event, Position> result = new HashMap<>();

        Map<Event, Position> event = Main.getInjector()
                .getInstance(MotionEventHandler.class).updateMotionState(deviceState);
        if (event != null) {
            result.putAll(event);
        }

        event = Main.getInjector().getInstance(OverspeedEventHandler.class)
                .updateOverspeedState(deviceState, Context.getDeviceManager().
                        lookupAttributeDouble(deviceId, OverspeedEventHandler.ATTRIBUTE_SPEED_LIMIT, 0, true, false));
        if (event != null) {
            result.putAll(event);
        }

        return result;
    }

//    public synchronized void updateDevice(Device device) {
//        for (long userId : Context.getPermissionsManager().getDeviceUsers(device.getId())) {
//            if (listeners.containsKey(userId)) {
//                for (UpdateListener listener : listeners.get(userId)) {
//                    listener.onUpdateDevice(device);
//                }
//            }
//        }
//    }


    public synchronized void updateDevice(final long deviceId, String status, Date time) {


        Device device = Context.getIdentityManager().getDeviceById(deviceId);
        if (device == null) {
            return;
        }

//        String oldStatus = device.getStatus();
        device.setStatus(status);

//        if (enableStatusEvents && !status.equals(oldStatus)) {
//            String eventType;
//            Map<Event, Position> events = new HashMap<>();
//            switch (status) {
//                case Device.STATUS_ONLINE:
//                    eventType = Event.TYPE_DEVICE_ONLINE;
//                    break;
//                case Device.STATUS_UNKNOWN:
//                    eventType = Event.TYPE_DEVICE_UNKNOWN;
//                    if (updateDeviceState) {
//                        events.putAll(updateDeviceState(deviceId));
//                    }
//                    break;
//                default:
//                    eventType = Event.TYPE_DEVICE_OFFLINE;
//                    if (updateDeviceState) {
//                        events.putAll(updateDeviceState(deviceId));
//                    }
//                    break;
//            }
//            events.put(new Event(eventType, deviceId), null);
//            Context.getNotificationManager().updateEvents(events);
//        }

        Timeout timeout = timeouts.remove(deviceId);

        System.out.println("========timeout");
        if (timeout != null) {
            timeout.cancel();
        }

        if (time != null) {
            device.setLastUpdate(time);
        }

        if (status.equals(Device.STATUS_ONLINE)) {
            timeouts.put(deviceId, GlobalTimer.getTimer().newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) {
                    if (!timeout.isCancelled()) {
                        updateDevice(deviceId, Device.STATUS_UNKNOWN, null);
                    }
                }
            }, deviceTimeout, TimeUnit.MILLISECONDS));
        }

        try {
            Context.getDataManager().updateDeviceStatus(device);
        } catch (SQLException error) {
            LOGGER.warn("Update device status error", error);
        }

//        updateDevice(device);

        if (listeners.containsKey(deviceId)) {
            for (UpdateListener listener : listeners.get(deviceId)) {
                listener.onUpdateDevice(device);
            }
        }
    }
//    public synchronized void updatePosition(Position position) {
//        long deviceId = position.getDeviceId();
//
//        for (long userId : Context.getPermissionsManager().getDeviceUsers(deviceId)) {
//            if (listeners.containsKey(userId)) {
//                for (UpdateListener listener : listeners.get(userId)) {
//                    listener.onUpdatePosition(position);
//                }
//            }
//        }
//    }
    public synchronized void updatePosition(Position position) {
        long deviceId = position.getDeviceId();

        positions.put(deviceId, position);


        if (listeners.containsKey(deviceId)) {
            for (UpdateListener listener : listeners.get(deviceId)) {
                listener.onUpdatePosition(position);

            }
        }
    }
    public synchronized void updateEvent(long userId, Event event) {
        if (listeners.containsKey(userId)) {
            for (UpdateListener listener : listeners.get(userId)) {
                listener.onUpdateEvent(event);
            }
        }
    }

    public interface UpdateListener {
        void onUpdateDevice(Device device);
        void onUpdatePosition(Position position);
        void onUpdateEvent(Event event);
    }

    public synchronized void addListener(long userId, UpdateListener listener) {
        if (!listeners.containsKey(userId)) {
            listeners.put(userId, new HashSet<UpdateListener>());
        }
        listeners.get(userId).add(listener);
    }

    public synchronized void removeListener(long userId, UpdateListener listener) {
        if (!listeners.containsKey(userId)) {
            listeners.put(userId, new HashSet<UpdateListener>());
        }
        listeners.get(userId).remove(listener);
    }

}
