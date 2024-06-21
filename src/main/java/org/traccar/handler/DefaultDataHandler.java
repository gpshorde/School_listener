/*
 * Copyright 2015 - 2019 Anton Tananaev (anton@traccar.org)
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
package org.traccar.handler;

import io.netty.channel.ChannelHandler;
import java.sql.SQLException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.BaseDataHandler;
import org.traccar.Context;
import org.traccar.database.DataManager;
import org.traccar.model.Alert;
import org.traccar.model.Event;
import org.traccar.model.MiscFormatter;
import org.traccar.model.Position;

@ChannelHandler.Sharable
public class DefaultDataHandler extends BaseDataHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDataHandler.class);

    private final DataManager dataManager;

    public DefaultDataHandler(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    protected Position handlePosition(Position position) {

        try {
//            dataManager.addObject(position);
//            System.out.println("addPosition=======================================================");
//          dataManager.addPosition(position);
            Context.getDataManager().addPosition(position);

            Position lastPosition = Context.getConnectionManager().getLastPosition(position.getDeviceId());
//               System.out.println("last position" + position.getBusiness_device_id());
             JSONObject ext_info= new JSONObject(MiscFormatter.toJson(position.getAttributes()));
//             System.out.println("org.traccar.handler.DefaultDataHandler.handlePosition()"+ext_info);
//            System.out.println("position.getLatitude()"+position.getLatitude());
//            System.out.println("position.getLongitude()"+position.getLongitude());

             if(position.getLatitude()!=0.0 && position.getLongitude()!=0.0){
				
				final Position pos=position;
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
//                                                   System.out.println("........ INSIDE ...........  Run Call Stoopage ");
							Context.getDataManager().vehicleShiftExecution(pos.getBusiness_device_id(),pos);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
						//	e.printStackTrace();
						}
						
					}
				}).start();
				
			}
             
                        if(ext_info.has("a")){
				
			}
        } catch (Exception error) {
            LOGGER.warn("Failed to store position", error);
        }

        return position;
    }

    @Override
    protected Position handlePosition(Position position, Alert event) {
        try {
//            dataManager.addObject(position);
//            System.out.println("addPosition=======================================================");
//          dataManager.addPosition(position);
            Context.getDataManager().addPosition(position);

            Position lastPosition = Context.getConnectionManager().getLastPosition(position.getDeviceId());
//            System.out.println("last position" + position.getBusiness_device_id());
            JSONObject ext_info= new JSONObject(MiscFormatter.toJson(position.getAttributes()));
//            System.out.println("org.traccar.handler.DefaultDataHandler.handlePosition()"+ext_info);
//            System.out.println("position.getLatitude()"+position.getLatitude());
//            System.out.println("position.getLongitude()"+position.getLongitude());

            if(position.getLatitude()!=0.0 && position.getLongitude()!=0.0){

                final Position pos=position;
                new Thread(new Runnable() {

                    @Override
                    public void run() {
						try {
//                                                   System.out.println("........ INSIDE ...........  Run Call Stoopage ");
							Context.getDataManager().vehicleShiftExecution(pos.getBusiness_device_id(),pos);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
						//	e.printStackTrace();
						}

                    }
                }).start();

            }

            if(ext_info.has("a")){

            }
        } catch (Exception error) {
            LOGGER.warn("Failed to store position", error);
        }

        return position;
    }


}
