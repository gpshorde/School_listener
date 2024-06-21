/*
 * Copyright 2015 Anton Tananaev (anton@traccar.org)
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vividsolutions.jts.geom.Geometry;
import jnr.a64asm.SYSREG_CODE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.PrintOut;
import org.traccar.common.CheckForGeoFence;
import org.traccar.common.DateSync;
import org.traccar.model.MiscFormatter;
import org.traccar.model.Permission;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.traccar.PushNotification;
import org.traccar.common.AlertType;
import org.traccar.model.Alert;
import org.traccar.model.CommandHistory;
import org.traccar.model.LiveEvents;
import org.traccar.model.LiveObervation;

public final class QueryBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryBuilder.class);

    private final Map<String, List<Integer>> indexMap = new HashMap<>();
    private Connection connection;
    private PreparedStatement statement;
    private final String query;
    private final boolean returnGeneratedKeys;

//    private QueryBuilder(DataSource dataSource, String query, boolean returnGeneratedKeys) throws SQLException {
//        this.query = query;
//        this.returnGeneratedKeys = returnGeneratedKeys;
//        if (query != null) {
//            connection = dataSource.getConnection();
//            String parsedQuery = parse(query.trim(), indexMap);
//            try {
//                if (returnGeneratedKeys) {
//                    statement = connection.prepareStatement(parsedQuery, Statement.RETURN_GENERATED_KEYS);
//                } else {
//                    statement = connection.prepareStatement(parsedQuery);
//                }
//            } catch (SQLException error) {
//                connection.close();
//                throw error;
//            }
//        }
//    }
private QueryBuilder(DataSource dataSource, String query, boolean returnGeneratedKeys) throws SQLException {
    this.query = query;
    this.returnGeneratedKeys = returnGeneratedKeys;
    if (query != null) {
        connection = dataSource.getConnection();
        String parsedQuery = parse(query.trim(), indexMap);
        try {
            if (returnGeneratedKeys) {
                statement = connection.prepareStatement(parsedQuery, Statement.RETURN_GENERATED_KEYS);
            } else {
                statement = connection.prepareStatement(parsedQuery);
            }
        } catch (SQLException error) {
            connection.close();
            throw error;
        }
    }
}

    private static String parse(String query, Map<String, List<Integer>> paramMap) {

        int length = query.length();
        StringBuilder parsedQuery = new StringBuilder(length);
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int index = 1;

        for (int i = 0; i < length; i++) {

            char c = query.charAt(i);

            // String end
            if (inSingleQuote) {
                if (c == '\'') {
                    inSingleQuote = false;
                }
            } else if (inDoubleQuote) {
                if (c == '"') {
                    inDoubleQuote = false;
                }
            } else {

                // String begin
                if (c == '\'') {
                    inSingleQuote = true;
                } else if (c == '"') {
                    inDoubleQuote = true;
                } else if (c == ':' && i + 1 < length
                        && Character.isJavaIdentifierStart(query.charAt(i + 1))) {

                    // Identifier name
                    int j = i + 2;
                    while (j < length && Character.isJavaIdentifierPart(query.charAt(j))) {
                        j++;
                    }

                    String name = query.substring(i + 1, j);
                    c = '?';
                    i += name.length();
                    name = name.toLowerCase();

                    // Add to list
                    List<Integer> indexList = paramMap.get(name);
                    if (indexList == null) {
                        indexList = new LinkedList<>();
                        paramMap.put(name, indexList);
                    }
                    indexList.add(index);

                    index++;
                }
            }

            parsedQuery.append(c);
        }

        return parsedQuery.toString();
    }

    public static QueryBuilder create(DataSource dataSource, String query) throws SQLException {
        return new QueryBuilder(dataSource, query, false);
    }

    public static QueryBuilder create(
            DataSource dataSource, String query, boolean returnGeneratedKeys) throws SQLException {
        return new QueryBuilder(dataSource, query, returnGeneratedKeys);
    }

    private List<Integer> indexes(String name) {
        name = name.toLowerCase();
        List<Integer> result = indexMap.get(name);
        if (result == null) {
            result = new LinkedList<>();
        }
        return result;
    }

    public QueryBuilder setBoolean(String name, boolean value) throws SQLException {
        for (int i : indexes(name)) {
            try {
                statement.setBoolean(i, value);
            } catch (SQLException error) {
                statement.close();
                connection.close();
                throw error;
            }
        }
        return this;
    }

    public QueryBuilder setInteger(String name, int value) throws SQLException {
        for (int i : indexes(name)) {
            try {
                statement.setInt(i, value);
            } catch (SQLException error) {
                statement.close();
                connection.close();
                throw error;
            }
        }
        return this;
    }
//public long executeUpdateAndReturnAlert() throws SQLException {
//    System.out.println("notification_id-------------------0=0=0-=0-=executeUpdateAndReturnAlert=0=-----------------");
//
//    if (query != null) {
//			try {
//				statement.execute();
//				if (returnGeneratedKeys) {
//					ResultSet resultSet = statement.getGeneratedKeys();
//					if (resultSet.next()) {
//						//System.out.println("notification_id-------------------0=0=0-=0-=0=0=-----------------"+resultSet.getLong("notification_id"));
//						String qry="select business_device_id,altype,key_id from web.notification where notification_id=?";
//						statement=connection.prepareStatement(qry);
//						statement.setLong(1, resultSet.getLong("notification_id"));
//						ResultSet rs=statement.executeQuery();
//						if(rs.next()){
//							final int business_device_id=rs.getInt("business_device_id");
//							final int altype= rs.getInt("altype");
//							final int key_id= rs.getInt("key_id");
//						System.out.println("alert type is------------------from alert table--------"+rs.getInt("altype"));
//							System.out.println("bussiness device_id is------------------from alert table--------"+rs.getInt("business_device_id"));
//							//if(altype!=AlertType.AC_ON && altype!=AlertType.AC_OFF){
//                            System.out.println("push notification=====================");
//
//                            new Thread(new Runnable() {
//								public void run(){
//
//									PushNotification.callPushNotification(business_device_id,altype,key_id);
//                                                                    //    WebSocketSend.CallWebSocket(business_device_id, altype);
//									System.out.println("push notification send to the device"+business_device_id+"---altype-----------------"+altype);
//								}
//							}).start();
//
//						  //}
//						}
//						return resultSet.getLong("notification_id");
//					}
//				}
//			} finally {
//				statement.close();
//				connection.close();
//			}
//		}
//		return 0;
//	}
public long executeUpdateAndReturnAlert() throws SQLException {
//    System.out.println("notification_id-------------------0=0=0-=0-=executeUpdateAndReturnAlert=0=-----------------");

    if (query != null) {
        try {
            statement.execute();
            if (returnGeneratedKeys) {
                ResultSet resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) {
                    //System.out.println("notification_id-------------------0=0=0-=0-=0=0=-----------------"+resultSet.getLong("notification_id"));
                    String qry="select business_device_id,altype,key_id from web.notification where notification_id=?";
                    statement=connection.prepareStatement(qry);
                    statement.setLong(1, resultSet.getLong("notification_id"));
                    ResultSet rs=statement.executeQuery();
                    if(rs.next()){
                        final int business_device_id=rs.getInt("business_device_id");
                        final int altype= rs.getInt("altype");
                        final int key_id= rs.getInt("key_id");
//							System.out.println("alert type is------------------from alert table--------"+rs.getInt("altype"));
//							System.out.println("bussiness device_id is------------------from alert table--------"+rs.getInt("business_device_id"));
                        //if(altype!=AlertType.AC_ON && altype!=AlertType.AC_OFF){
                        new Thread(new Runnable() {
                            public void run(){
                                	System.out.println("push notification send to the device"+business_device_id+"---altype-----------------"+altype);

                                PushNotification.callPushNotification(business_device_id,altype,key_id);
                                //  WebSocketSend.CallWebSocket(business_device_id, altype);
                                //	System.out.println("push notification send to the device"+business_device_id+"---altype-----------------"+altype);
                            }
                        }).start();

                        //}
                    }
                    return resultSet.getLong("notification_id");
                }
            }
        } finally {
            statement.close();
            connection.close();
        }
    }
    return 0;
}
    public QueryBuilder setLong(String name, long value) throws SQLException {
        return setLong(name, value, false);
    }

    public QueryBuilder setLong(String name, long value, boolean nullIfZero) throws SQLException {
        for (int i : indexes(name)) {
            try {
                if (value == 0 && nullIfZero) {
                    statement.setNull(i, Types.INTEGER);
                } else {
                    statement.setLong(i, value);
                }
            } catch (SQLException error) {
                statement.close();
                connection.close();
                throw error;
            }
        }
        return this;
    }

    public QueryBuilder setDouble(String name, double value) throws SQLException {
        for (int i : indexes(name)) {
            try {
                statement.setDouble(i, value);
            } catch (SQLException error) {
                statement.close();
                connection.close();
                throw error;
            }
        }
        return this;
    }

    public QueryBuilder setString(String name, String value) throws SQLException {
        for (int i : indexes(name)) {
            try {
                if (value == null) {
                    statement.setNull(i, Types.VARCHAR);
                } else {
                    statement.setString(i, value);
                }
            } catch (SQLException error) {
                statement.close();
                connection.close();
                throw error;
            }
        }
        return this;
    }

    public QueryBuilder setDate(String name, Date value) throws SQLException {
        for (int i : indexes(name)) {
            try {
                if (value == null) {
                    statement.setNull(i, Types.TIMESTAMP);
                } else {
                    statement.setTimestamp(i, new Timestamp(value.getTime()));
                }
            } catch (SQLException error) {
                statement.close();
                connection.close();
                throw error;
            }
        }
        return this;
    }

    public QueryBuilder setBlob(String name, byte[] value) throws SQLException {
        for (int i : indexes(name)) {
            try {
                if (value == null) {
                    statement.setNull(i, Types.BLOB);
                } else {
                    statement.setBytes(i, value);
                }
            } catch (SQLException error) {
                statement.close();
                connection.close();
                throw error;
            }
        }
        return this;
    }
public long executeUpdateLiveObservation() throws SQLException {
		if (query != null) {
            System.out.println("Its alert dude take care your gadi.it just testing not final implemtation.");

            try {
				statement.execute();
				if (returnGeneratedKeys) {
					ResultSet resultSet = statement.getGeneratedKeys();
					if (resultSet.next()) {
						int ign=resultSet.getInt("ignition");
						int p_mode=resultSet.getInt("p_mode");

						final int b_d_id=resultSet.getInt("business_device_id");
						if(ign==1 && p_mode==1){
							//System.out.println("Its alert dude take care your gadi.it just testing not final implemtation.");
							new Thread(new Runnable() {
								public void run() {
									PushNotification.callPushNotification(b_d_id,AlertType.PARKING,0);
//									System.out.println("push notification send to the device"+b_d_id+"---altype-----------------"+11);
								}
							}).start();
						}
						if(ign==2 && p_mode==2){
							Date gdate=resultSet.getTimestamp("gps_time");
							long diff = DateSync.getUTCTime().getTime() - gdate.getTime();
							long diffMinutes = diff / (60 * 1000) % 60;
							if(diffMinutes>10){
								System.out.println("================Its around 10 minute there is no parking mode set==============");
							}
						}
						return resultSet.getLong(1);
					}
				}
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				statement.close();
				connection.close();
			}
		}
		return 0;
	}
    public QueryBuilder setObject(Object object) throws SQLException {

        Method[] methods = object.getClass().getMethods();

        for (Method method : methods) {
            if (method.getName().startsWith("get") && method.getParameterTypes().length == 0
                    && !method.isAnnotationPresent(QueryIgnore.class)) {
                String name = method.getName().substring(3);
                try {
                    if (method.getReturnType().equals(boolean.class)) {
                        setBoolean(name, (Boolean) method.invoke(object));
                    } else if (method.getReturnType().equals(int.class)) {
                        setInteger(name, (Integer) method.invoke(object));
                    } else if (method.getReturnType().equals(long.class)) {
                        setLong(name, (Long) method.invoke(object), name.endsWith("Id"));
                    } else if (method.getReturnType().equals(double.class)) {
                        setDouble(name, (Double) method.invoke(object));
                    } else if (method.getReturnType().equals(String.class)) {
                        setString(name, (String) method.invoke(object));
                    } else if (method.getReturnType().equals(Date.class)) {
                        setDate(name, (Date) method.invoke(object));
                    } else if (method.getReturnType().equals(byte[].class)) {
                        setBlob(name, (byte[]) method.invoke(object));
                    } else {
                        if (method.getReturnType().equals(Map.class)
                                && Context.getConfig().getBoolean("database.xml")) {
                            setString(name, MiscFormatter.toXmlString((Map) method.invoke(object)));
                        } else {
                            setString(name, Context.getObjectMapper().writeValueAsString(method.invoke(object)));
                        }
                    }
                } catch (IllegalAccessException | InvocationTargetException | JsonProcessingException error) {
                    LOGGER.warn("Get property error", error);
                }
            }
        }

        return this;
    }

    private interface ResultSetProcessor<T> {
        void process(T object, ResultSet resultSet) throws SQLException;
    }

    public <T> T executeQuerySingle(Class<T> clazz) throws SQLException {
        Collection<T> result = executeQuery(clazz);
        if (!result.isEmpty()) {
            return result.iterator().next();
        } else {
            return null;
        }
    }

    private <T> void addProcessors(
            List<ResultSetProcessor<T>> processors,
            final Class<?> parameterType, final Method method, final String name) {

        if (parameterType.equals(boolean.class)) {
            processors.add(new ResultSetProcessor<T>() {
                @Override
                public void process(T object, ResultSet resultSet) throws SQLException {
                    try {
                        method.invoke(object, resultSet.getBoolean(name));
                    } catch (IllegalAccessException | InvocationTargetException error) {
                        LOGGER.warn("Set property error", error);
                    }
                }
            });
        } else if (parameterType.equals(int.class)) {
            processors.add(new ResultSetProcessor<T>() {
                @Override
                public void process(T object, ResultSet resultSet) throws SQLException {
                    try {
                        method.invoke(object, resultSet.getInt(name));
                    } catch (IllegalAccessException | InvocationTargetException error) {
                        LOGGER.warn("Set property error", error);
                    }
                }
            });
        } else if (parameterType.equals(long.class)) {
            processors.add(new ResultSetProcessor<T>() {
                @Override
                public void process(T object, ResultSet resultSet) throws SQLException {
                    try {
                        method.invoke(object, resultSet.getLong(name));
                    } catch (IllegalAccessException | InvocationTargetException error) {
                        LOGGER.warn("Set property error", error);
                    }
                }
            });
        } else if (parameterType.equals(double.class)) {
            processors.add(new ResultSetProcessor<T>() {
                @Override
                public void process(T object, ResultSet resultSet) throws SQLException {
                    try {
                        method.invoke(object, resultSet.getDouble(name));
                    } catch (IllegalAccessException | InvocationTargetException error) {
                        LOGGER.warn("Set property error", error);
                    }
                }
            });
        } else if (parameterType.equals(String.class)) {
            processors.add(new ResultSetProcessor<T>() {
                @Override
                public void process(T object, ResultSet resultSet) throws SQLException {
                    try {
                        method.invoke(object, resultSet.getString(name));
                    } catch (IllegalAccessException | InvocationTargetException error) {
                        LOGGER.warn("Set property error", error);
                    }
                }
            });
        } else if (parameterType.equals(Date.class)) {
            processors.add(new ResultSetProcessor<T>() {
                @Override
                public void process(T object, ResultSet resultSet) throws SQLException {
                    try {
                        Timestamp timestamp = resultSet.getTimestamp(name);
                        if (timestamp != null) {
                            method.invoke(object, new Date(timestamp.getTime()));
                        }
                    } catch (IllegalAccessException | InvocationTargetException error) {
                        LOGGER.warn("Set property error", error);
                    }
                }
            });
        } else if (parameterType.equals(byte[].class)) {
            processors.add(new ResultSetProcessor<T>() {
                @Override
                public void process(T object, ResultSet resultSet) throws SQLException {
                    try {
                        method.invoke(object, resultSet.getBytes(name));
                    } catch (IllegalAccessException | InvocationTargetException error) {
                        LOGGER.warn("Set property error", error);
                    }
                }
            });
        } else {
            processors.add(new ResultSetProcessor<T>() {
                @Override
                public void process(T object, ResultSet resultSet) throws SQLException {
                    String value = resultSet.getString(name);
                    if (value != null && !value.isEmpty()) {
                        try {
                            method.invoke(object, Context.getObjectMapper().readValue(value, parameterType));
                        } catch (InvocationTargetException | IllegalAccessException | IOException error) {
                            LOGGER.warn("Set property error", error);
                        }
                    }
                }
            });
        }
    }
 public <T> Collection<T> executeQuery(Class<T> clazz) throws SQLException {
        List<T> result = new LinkedList<>();
    // System.out.println("query"+query);
        if (query != null) {
//     System.out.println("query===========>"+query);

            try {

                try (ResultSet resultSet = statement.executeQuery()) {

                    ResultSetMetaData resultMetaData = resultSet.getMetaData();

                    List<ResultSetProcessor<T>> processors = new LinkedList<>();

                    Method[] methods = clazz.getMethods();

                    for (final Method method : methods) {
                        if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {

                            final String name = method.getName().substring(3);

                            // Check if column exists
                            boolean column = false;
                            for (int i = 1; i <= resultMetaData.getColumnCount(); i++) {
                                if (name.equalsIgnoreCase(resultMetaData.getColumnLabel(i))) {
                                    column = true;
                                    break;
                                }
                            }
                            if (!column) {
                                continue;
                            }

                            addProcessors(processors, method.getParameterTypes()[0], method, name);
                        }
                    }

                    while (resultSet.next()) {
                        try {
                            T object = clazz.newInstance();
                            for (ResultSetProcessor<T> processor : processors) {
                                processor.process(object, resultSet);
                            }
                            result.add(object);
                        } catch (InstantiationException | IllegalAccessException e) {
                            throw new IllegalArgumentException();
                        }
                    }
                }

            } finally {
                statement.close();
                connection.close();
            }
        }

        return result;
    }
//    public <T> Collection<T> executeQuery(Class<T> clazz) throws SQLException {
//        List<T> result = new LinkedList<>();
//
//        if (query != null) {
//
//            try {
//
//                try (ResultSet resultSet = statement.executeQuery()) {
//
//                    ResultSetMetaData resultMetaData = resultSet.getMetaData();
//
//                    List<ResultSetProcessor<T>> processors = new LinkedList<>();
//
//                    Method[] methods = clazz.getMethods();
//
//                    for (final Method method : methods) {
//                        if (method.getName().startsWith("set") && method.getParameterTypes().length == 1
//                                && !method.isAnnotationPresent(QueryIgnore.class)) {
//
//                            final String name = method.getName().substring(3);
//
//                            // Check if column exists
//                            boolean column = false;
//                            for (int i = 1; i <= resultMetaData.getColumnCount(); i++) {
//                                if (name.equalsIgnoreCase(resultMetaData.getColumnLabel(i))) {
//                                    column = true;
//                                    break;
//                                }
//                            }
//                            if (!column) {
//                                continue;
//                            }
//
//                            addProcessors(processors, method.getParameterTypes()[0], method, name);
//                        }
//                    }
//
//                    while (resultSet.next()) {
//                        try {
//                            T object = clazz.newInstance();
//                            for (ResultSetProcessor<T> processor : processors) {
//                                processor.process(object, resultSet);
//                            }
//                            result.add(object);
//                        } catch (InstantiationException | IllegalAccessException e) {
//                            throw new IllegalArgumentException();
//                        }
//                    }
//                }
//
//            } finally {
//                statement.close();
//                connection.close();
//            }
//        }
//
//        return result;
//    }

    public long executeUpdate() throws SQLException {

        if (query != null) {
            try {
                statement.execute();
                if (returnGeneratedKeys) {
                    ResultSet resultSet = statement.getGeneratedKeys();
                    if (resultSet.next()) {
                        return resultSet.getLong(1);
                    }
                }
            } finally {
                statement.close();
                connection.close();
            }
        }
        return 0;
    }

    public Collection<Permission> executePermissionsQuery() throws SQLException, ClassNotFoundException {
        List<Permission> result = new LinkedList<>();
        if (query != null) {
            try {
                try (ResultSet resultSet = statement.executeQuery()) {
                    ResultSetMetaData resultMetaData = resultSet.getMetaData();
                    while (resultSet.next()) {
                        LinkedHashMap<String, Long> map = new LinkedHashMap<>();
                        for (int i = 1; i <= resultMetaData.getColumnCount(); i++) {
                            String label = resultMetaData.getColumnLabel(i);
                            map.put(label, resultSet.getLong(label));
                        }
                        result.add(new Permission(map));
                    }
                }
            } finally {
                statement.close();
                connection.close();
            }
        }

        return result;
    }

//   public String executeUpdateAndReturn() throws SQLException {
//
//		if (query != null) {
//			try {
//				statement.execute();
//				if (returnGeneratedKeys) {
//					ResultSet resultSet = statement.getGeneratedKeys();
//					if (resultSet.next()) {
//						String id=resultSet.getString("uuid");
//                                               System.out.println("executeUpdateAndReturn----------->>"+id);
//		                        //PrintOut.printOutM(" Observation id--------------------------------- ", id);
//						String qry="select obtype,"
//								+ "ignition,"
//								+ "device_id,"
//								+ "gps_time,"
//								+ "address,"
//								+ "extrainfo,"
//								+ "battery,"
//								+ "angle,"
//								+ "speed,"
//								+ "trip,"
//								+ "mileage,"
//								+ "ST_AsText(gps_location) as geom,"
//								+ "business_device_id,"
//								+ "idle,"
//								+ "fuel,"
//								+ "uuid,"
//                                                                + "ac,"
//                                                                + "distance,"
//                                                                + "stopage_time,"
//                                                                + "protocol,"
//                                                                + "port,"
//                                                                + "temperature,"
//                                                                + "servertime"
//                                                                + " from ob.observation where uuid=?";
//						statement=connection.prepareStatement(qry);
//						statement.setString(1, id);
//						ResultSet rs=statement.executeQuery();
//						if(rs.next()){
//
//
//							/*System.out.println("device_id----------------------------"+rs.getInt("device_id"));
//							System.out.println("bussiness_device_id------------------"+rs.getInt("business_device_id"));
//							System.out.println("gps_time-----------------------------"+rs.getTimestamp("gps_time"));
//							System.out.println("geometry-----------------------------"+rs.getString("geom"));
//							 */
//
////                                                        if(rs.getInt("device_id") == 939)
////                                                        {
////                                                            System.out.println("test");
////                                                           System.out.println("Observation id--------------------------------- " +id);
////                                                        }
//                                                          Integer port = rs.getInt("port");
//
//
//                                                        System.out.println("port no ------------------"+port);
//                            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
//                            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
//
//
////                                                       String uuid =  java.util.UUID.randomUUID().toString();
//							Date date= rs.getTimestamp("gps_time");
//							Alert alert=new Alert();
//                            System.out.println("alert_id"+id);
//							alert.setUuid(id);
//							alert.setGps_time(rs.getTimestamp("gps_time"));
//							String gps_location=rs.getString("geom");
//                            alert.setGps_location(gps_location);
//
//                            System.out.println("gps_location"+gps_location);
//
////                                                         String uuid=rs.getString("uuid");
//							Integer business_device_id=rs.getInt("business_device_id");
//                            System.out.println("alert business_device_id"+business_device_id);
////                                                                  if(port == 9088)
////                                                          {
////                                                        CheckForDriver.checkGeo(gps_location,business_device_id, alert, statement, connection);
////							System.out.println("port------------------"+port);
////                                                          }
//                                                    //   TripGeofence.getGeofence(gps_location, business_device_id,alert,statement,connection);
//						CheckForGeoFence.checkGeo(gps_location, business_device_id,alert,statement,connection);
////                                                       CheckForFranchise.getFranchise(gps_location, business_device_id,alert,statement,connection);;
//						       // newSite.checkSite(gps_location, business_device_id,alert,statement,connection);
//							//4 for exit
//							//5 for enter
//							alert.setBusiness_device_id(business_device_id);
//                            System.out.println("alert get "+ alert.getBusiness_device_id());
//							alert.setUuid(id);
//                            System.out.println("alert uuid "+ alert.getUuid());
//
//                            alert.setGps_time(rs.getTimestamp("gps_time"));
//                                                      //  alert.setGps_location(rs.getString("geom"));
////                                                        alert.setUuid(uuid);
//
//
//                                                        LiveEvents le = new LiveEvents();
//                                                      //  le.setObservation_id(id);
//                            System.out.println("=============================");
//                            System.out.println("liveevnt id "+ id );
//                            System.out.println("=============================");
//
//                            le.setUuid(id);
//                                                       le.setEvent_time(rs.getTimestamp("gps_time"));
//                                                        le.setIgnition(rs.getInt("ignition"));
//                                                        le.setSpeed(rs.getInt("speed"));
//                                                       le.setDeviceId(rs.getInt("device_id"));
//                                                        le.setGps_location(rs.getString("geom"));
//                                                        le.setBusiness_device_id(rs.getInt("business_device_id"));
//							JSONObject obj = new JSONObject(rs.getString("extrainfo").toString());
//                            System.out.println("HELLOO"+rs.getString("extrainfo").toString());
//							if(obj.has("speed")){
//								//alert.setSos(1);
//								alert.setAl_type(AlertType.OVER_SPEPED);
//								//SendSMS.sendSMS();
//								Context.getDataManager().addAlerts(alert);
//							}else if(obj.has("break")){
//								//Hard brake
//								alert.setAl_type(AlertType.HARD_BREAK);
//								Context.getDataManager().addAlerts(alert);
//							}
//							else if(obj.has("hard")){
//								//Hard acceleration
//								alert.setAl_type(8);
//								Context.getDataManager().addAlerts(alert);
//							}
//                                                        if(obj.has("ignition") == true){
//                                                                le.setEvent_type(AlertType.IGNITION_ON);
//                                                                le.setEvent_type_name(AlertType.IGNITIONON);
//                                                               // Context.getDataManager().AddLiveEvents(le);
//								alert.setAl_type(AlertType.IGNITION_ON);
//								Context.getDataManager().addAlerts(alert);
//							}
//							if(obj.has("ignition")==false){
//                                                               le.setEvent_type(AlertType.IGNITION_OFF);
//                                                                le.setEvent_type_name(AlertType.IGNITIONOFF);
////                                                                Context.getDataManager().AddLiveEvents(le);
//								alert.setAl_type(AlertType.IGNITION_OFF);
//								Context.getDataManager().addAlerts(alert);
//							}
//                            if(obj.has("ignition_on")){
//                                le.setEvent_type(AlertType.IGNITION_ON);
//                                le.setEvent_type_name(AlertType.IGNITIONON);
//                                Context.getDataManager().AddLiveEvents(le);
//                                alert.setAl_type(AlertType.IGNITION_ON);
//                                Context.getDataManager().addAlerts(alert);
//                            }
//                            if(obj.has("ignition_off")){
//                                le.setEvent_type(AlertType.IGNITION_OFF);
//                                le.setEvent_type_name(AlertType.IGNITIONOFF);
////                                                                Context.getDataManager().AddLiveEvents(le);
//                                alert.setAl_type(AlertType.IGNITION_OFF);
//                                Context.getDataManager().addAlerts(alert);
//                            }
//                                                        if(obj.has("bonnet_close")){
//                                                                if(obj.getString("bonnet_close").equals("yes")){
//                                                                  le.setEvent_type(AlertType.BONNET_CLOSE);
//                                                                  le.setEvent_type_name(AlertType.BONNETCLOSE);
////                                                                  Context.getDataManager().AddLiveEvents(le);
//                                                                  alert.setAl_type(AlertType.BONNET_CLOSE);
//                                                                 Context.getDataManager().addAlerts(alert);
//                                                                }
//                                                        }
//                                                        if(obj.has("bonnet_open")){
//
////                                                                if(obj.getString("bonnet_open").equals("yes")){
////                                                                  le.setEvent_type(AlertType.BONNET_OPEN);
////                                                                  le.setEvent_type_name(AlertType.BONNETCLOSE);
//////                                                                  Context.getDataManager().AddLiveEvents(le);
////                                                                  alert.setAl_type(AlertType.BONNET_OPEN);
////                                                                  Context.getDataManager().addAlerts(alert);
////                                                                }
//                                                        }
//                                                        if(obj.has("door_close")){
//
//                                                                if(obj.getString("door_close").equals("yes")){
//                                                                  le.setEvent_type(AlertType.DOOR_CLOSE);
//                                                                  le.setEvent_type_name(AlertType.DOORCLOSE);
////                                                                  Context.getDataManager().AddLiveEvents(le);
//                                                                  alert.setAl_type(AlertType.DOOR_CLOSE);
//                                                                  Context.getDataManager().addAlerts(alert);
//                                                                }
//                                                        }
//                                                        if(obj.has("door_open")){
//
//                                                                if(obj.getString("door_open").equals("yes")){
//                                                                  le.setEvent_type(AlertType.DOOR_OPEN);
//                                                                  le.setEvent_type_name(AlertType.DOORCLOSE);
////                                                                  Context.getDataManager().AddLiveEvents(le);
//                                                                  alert.setAl_type(AlertType.DOOR_OPEN);
//                                                                  Context.getDataManager().addAlerts(alert);
//                                                                }
//                                                        }
// 							if(obj.has("engine_idle")){
//								if(obj.getString("engine_idle").equals("start")){
//									alert.setAl_type(AlertType.ENGINE_IDLE);
//									Context.getDataManager().addAlerts(alert);
//								}
//							}
//
//							if(obj.has("ac")){
//								if(obj.getString("ac").equals("on")){
//                                                                        le.setEvent_type(AlertType.AC_ON);
//                                                                        le.setEvent_type_name(AlertType.ACON);
////                                                                        Context.getDataManager().AddLiveEvents(le);
//									alert.setAl_type(AlertType.AC_ON);
//									Context.getDataManager().addAlerts(alert);
//								}
//							}
//							if(obj.has("ac")){
//								if(obj.getString("ac").equals("off")){
//                                                                        le.setEvent_type(AlertType.AC_OFF);
//                                                                        le.setEvent_type_name(AlertType.ACOff);
////                                                                        Context.getDataManager().AddLiveEvents(le);
//                                                                        alert.setAl_type(AlertType.AC_OFF);
//									Context.getDataManager().addAlerts(alert);
//								}
//							}
//                                                        if(obj.has("ramp")){
//								if(obj.getString("ramp").equals("on")){
//                                                                        le.setEvent_type(AlertType.RAMP_ON);
//                                                                        le.setEvent_type_name(AlertType.RAMPON);
////                                                                        Context.getDataManager().AddLiveEvents(le);
//									alert.setAl_type(AlertType.RAMP_ON);
//									Context.getDataManager().addAlerts(alert);
//								}
//							}
//							if(obj.has("ramp")){
//								if(obj.getString("ramp").equals("off")){
//
//                                                                        le.setEvent_type(AlertType.RAMP_OFF);
//                                                                        le.setEvent_type_name(AlertType.RAMPOFF);
////                                                                        Context.getDataManager().AddLiveEvents(le);
//                                                                        alert.setAl_type(AlertType.RAMP_OFF);
//									Context.getDataManager().addAlerts(alert);
//								}
//							}
//							if(obj.has("alarm")){
//								if(obj.getString("alarm").equals("sos")){
//									alert.setAl_type(AlertType.SOS);
//									Context.getDataManager().addAlerts(alert);
//								}
//							}
//                                                        if(obj.has("alarm")) {
//                                                            if(obj.getString("alarm").equals("Vibration")) {
//                                                                    alert.setAl_type(AlertType.VIBRATION);
//                                                                    Context.getDataManager().addAlerts(alert);
//                                                            }
//                                                        }
//							if(obj.has("alarm")){
//								if(obj.getString("alarm").equals("moving")){
//									alert.setAl_type(AlertType.MOVING);
//									Context.getDataManager().addAlerts(alert);
//								}
//							}
//							if(obj.has("alarm")) {
//                                                            if(obj.getString("alarm").equals("displacement")) {
//                                                                alert.setAl_type(AlertType.DISPLACEMENT);
//                                                                Context.getDataManager().addAlerts(alert);
//                                                            }
//                                                        }
//                                                        if(obj.has("alarm")) {
//                                                            if(obj.getString("alarm").equals("illegal_dismantle")) {
//                                                                alert.setAl_type(AlertType.ILLEGAL_DISMANTLE);
//                                                                Context.getDataManager().addAlerts(alert);
//                                                            }
//
//                                                        }
//
//							if(obj.has("alarm")){
//								if(obj.getString("alarm").equals("powerCut")){
//									alert.setAl_type(AlertType.POWER_CUTT);
//									Context.getDataManager().addAlerts(alert);
//								}
//							}
//
//                                                        if(obj.has("alarm")){
//								if(obj.getString("alarm").equals("towing")){
//									alert.setAl_type(AlertType.TOWING);
//									Context.getDataManager().addAlerts(alert);
//								}
//							}
//
//							if(obj.has("alarm")){
//								if(obj.getString("alarm").equals("overspeed")){
//									alert.setAl_type(AlertType.OVER_SPEPED);
//									Context.getDataManager().addAlerts(alert);
//								}
//							}
//
//							if(obj.has("alarm")){
//								if(obj.getString("alarm").equals("fallDown")){
//									alert.setAl_type(AlertType.FALLDOWN);
//									Context.getDataManager().addAlerts(alert);
//								}
//							}
//
//                                                        if(obj.has("alarm")){
//								if(obj.getString("alarm").equals("Crash")){
//									alert.setAl_type(AlertType.Crash);
//									Context.getDataManager().addAlerts(alert);
//								}
//							}
//
//                                                        if(obj.has("alarm")){
//								if(obj.getString("alarm").equals("dangerousDriving")){
//									alert.setAl_type(AlertType.Dangerous_Driving);
//									Context.getDataManager().addAlerts(alert);
//								}
//							}
//
//                                                        if(obj.has("alarm")){
//								if(obj.getString("alarm").equals("HARD_CORNERING")){
//									alert.setAl_type(AlertType.HARD_CORNERING);
//									Context.getDataManager().addAlerts(alert);
//								}
//							}
//
//							if(obj.has("alarm")){
//								if(obj.getString("alarm").equals("lowBattery")){
//									alert.setAl_type(AlertType.LOWBATTERY);
//									Context.getDataManager().addAlerts(alert);
//								}else if(obj.getString("alarm").equals("powerOn")){
//									alert.setAl_type(AlertType.POWER_ON);
//									Context.getDataManager().addAlerts(alert);
//								}
//							}
//
//
//
//							/*if(obj.has("trip")){
//								if(obj.getString("trip").equals("start")){
//									alert.setAl_type(AlertType.MOVING);
//									Context.getDataManager().addAlerts(alert);
//								}
//							}*/
//
//							/*if(rs.getString("obtype")!=null && rs.getString("obtype").equals("lbs")){
//								System.out.println("-------------------------------Its lbs data comes right now--------------------------------------"+rs.getString("extrainfo"));
//								obj=new JSONObject(rs.getString("extrainfo"));
//								CellToLocation.updateLocationForLbs(id,obj);
//							}*/
//
////                                                        insertObservationDetailsRecord(id,date,business_device_id,gps_location,
////									rs.getInt("device_id"),
////									rs.getString("obtype"),
////									rs.getInt("ignition"),
////									rs.getString("address"),
////									rs.getInt("angle"),
////									rs.getDouble("speed"),
////									rs.getDouble("battery"),
////									rs.getInt("trip"),
////									rs.getDouble("mileage"),
////									rs.getString("extrainfo"),
////									rs.getInt("idle"),
////									rs.getInt("fuel"),
////									rs.getInt("ac"),
////                                                                        uuid,
////                                                                        rs.getTimestamp("servertime"),
////                                                                        rs.getDouble("distance"),
////                                                                        rs.getString("protocol"),
////                                                                        rs.getInt("stopage_time"));
//
//
//
//							System.out.println("****************************************************************"+rs.getString("extrainfo"));
//							System.out.println("***************************insert into observation*************************************");
//
//insertOrUpdateLiveRecord(id,date,business_device_id,gps_location,
//									rs.getInt("device_id"),
//									rs.getString("obtype"),
//									rs.getInt("ignition"),
//									rs.getString("address"),
//									rs.getInt("angle"),
//									rs.getDouble("speed"),
//									rs.getDouble("battery"),
//									rs.getInt("trip"),
//									rs.getDouble("mileage"),
//									rs.getString("extrainfo"),
//									rs.getInt("idle"),
//									rs.getInt("fuel"),
//                                                                        rs.getInt("temperature"),
//									rs.getInt("ac"));
////                                                                       uuid);
//
////                                                        	insertOrUpdateLiveRecordNew(id,date,business_device_id,gps_location,
////									rs.getInt("device_id"),
////									rs.getString("obtype"),
////									rs.getInt("ignition"),
////									rs.getString("address"),
////									rs.getInt("angle"),
////									rs.getDouble("speed"),
////									rs.getDouble("battery"),
////									rs.getInt("trip"),
////									rs.getDouble("mileage"),
////									rs.getString("extrainfo"),
////									rs.getInt("idle"),
////									rs.getInt("fuel"),
////									rs.getInt("ac"),
////                                                                       uuid);
//						}
//                                            System.out.println("rs.strig========>"+rs.toString());
//
//						return resultSet.getString(1);
//					}
//				}
//			} finally {
//				statement.close();
//				connection.close();
//			}
//		}
//		return null;
//	}
public String executeUpdateAndReturn() throws SQLException {

    if (query != null) {
        try {
            statement.execute();
            if (returnGeneratedKeys) {
                ResultSet resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) {
                    String id=resultSet.getString("uuid");

                    String qry="select obtype,"
                            + "ignition,"
                            + "device_id,"
                            + "gps_time,"
                            + "address,"
                            + "extrainfo,"
                            + "battery,"
                            + "angle,"
                            + "speed,"
                            + "trip,"
                            + "mileage,"
                            + "ST_AsText(gps_location) as geom,"
                            + "business_device_id,"
                            + "idle,"
                            + "fuel,"
                            + "uuid,"
                            + "ac,"
                            + "distance,"
                            + "stopage_time,"
                            + "protocol,"
                            + "port,"
                            + "temperature,"
                            + "servertime"
                            + " from ob.observation where uuid=?";
                    statement=connection.prepareStatement(qry);
                    statement.setString(1, id);
                    ResultSet rs=statement.executeQuery();
                    if(rs.next()){


							/*System.out.println("device_id----------------------------"+rs.getInt("device_id"));
							System.out.println("bussiness_device_id------------------"+rs.getInt("business_device_id"));
							System.out.println("gps_time-----------------------------"+rs.getTimestamp("gps_time"));
							System.out.println("geometry-----------------------------"+rs.getString("geom"));
							 */

//                                                        if(rs.getInt("device_id") == 939)
//                                                        {
//                                                           System.out.println("Observation id--------------------------------- " +id);
//                                                        }
                        Integer port = rs.getInt("port");

//                                                          System.out.println("port no ------------------"+port);


//                                                       String uuid =  java.util.UUID.randomUUID().toString();
                        Date date= rs.getTimestamp("gps_time");
                       Date servertime= rs.getTimestamp("servertime");

                        Alert alert=new Alert();
                        Integer test = 0;
                        alert.setUuid(id);
                        alert.setGps_time(rs.getTimestamp("gps_time"));
                        String gps_location=rs.getString("geom");
//                                                         String uuid=rs.getString("uuid");
                        Integer business_device_id=rs.getInt("business_device_id");
//                                                                  if(port == 9088)
//                                                          {
//                                                        CheckForDriver.checkGeo(gps_location,business_device_id, alert, statement, connection);
//							System.out.println("port------------------"+port);
//                                                          }
                       // TripGeofence.getGeofence(gps_location, business_device_id,alert,statement,connection);
                        CheckForGeoFence.checkGeo(gps_location, business_device_id,alert,statement,connection);
//                                                       CheckForFranchise.getFranchise(gps_location, business_device_id,alert,statement,connection);;
                       // newSite.checkSite(gps_location, business_device_id,alert,statement,connection);
                        //4 for exit
                        //5 for enter
                        alert.setBusiness_device_id(business_device_id);
                        alert.setUuid(id);
                        alert.setGps_time(rs.getTimestamp("gps_time"));
                        alert.setGps_location(rs.getString("geom"));
//                                                        alert.setUuid(uuid);

//                                                        System.out.println("addon ==========>"+ rs.getInt("temperature"));
//                                                        System.out.println("temperature ==========>"+ rs.getString("address"));
                            CommandHistory commandHistory = new CommandHistory();

                        alert.setTemperature(rs.getInt("temperature"));
                        alert.setAddress(rs.getString("address"));
                        LiveEvents le = new LiveEvents();
                        le.setUuid(id);
                        le.setEvent_time(rs.getTimestamp("gps_time"));
                        le.setIgnition(rs.getInt("ignition"));

                        le.setSpeed(rs.getInt("speed"));
                        le.setDeviceId(rs.getInt("device_id"));
                        le.setGps_location(rs.getString("geom"));
                        le.setBusiness_device_id(rs.getInt("business_device_id"));
                        JSONObject obj = new JSONObject(rs.getString("extrainfo").toString());
//                        System.out.println("HJHD"+rs.getString("extrainfo").toString());

                        if(obj.has("speed")){
                            //alert.setSos(1);
                            alert.setAl_type(AlertType.OVER_SPEPED);
                            //SendSMS.sendSMS();
                            Context.getDataManager().addAlerts(alert);
                        }else if (obj.has("result")) {
                                System.out.println("result================>>");
                                 String result =obj.getString("result");
                                 commandHistory.setBusiness_device_id(rs.getInt("business_device_id"));
                                 commandHistory.setDevice_id(rs.getInt("device_id"));
                                 commandHistory.setData(result);
                                 commandHistory.setProtocol(rs.getString("protocol"));

                               Context.getDataManager().addCommandHistory(commandHistory);
                                System.out.println("result================>>"+result);
                       
                            }
                        else if(obj.has("break")){
                            //Hard brake
                            alert.setAl_type(AlertType.HARD_BREAK);
                            Context.getDataManager().addAlerts(alert);
                        }
                        else if(obj.has("hard")){
                            //Hard acceleration
                            alert.setAl_type(8);
                            Context.getDataManager().addAlerts(alert);
                        }
                        if(obj.has("ignition_on")){
//                            System.out.println("999999999999999999");
                            test = 1;
                            le.setEvent_type(AlertType.IGNITION_ON);
                            le.setEvent_type_name(AlertType.IGNITIONON);
//                                                                Context.getDataManager().AddLiveEvents(le);
                            alert.setAl_type(AlertType.IGNITION_ON);
                            Context.getDataManager().addAlerts(alert);
                        }
                        if(obj.has("ignition")){
//                            System.out.println("obj.has(IGNITION_ON)"+obj.has("ignition"));
                            Boolean aaaaa =obj.getBoolean("ignition");
//                            System.out.println("aaaaa"+aaaaa);
                            if(aaaaa ==true){
                                 test = 1;
                                alert.setAl_type(AlertType.IGNITION_ON);

                                Context.getDataManager().addAlerts(alert);
                            }
                           else{
//                                System.out.println("obj.has(IGNITION_OFF)"+obj.has("ignition"));
                                 test = 2;

                                alert.setAl_type(AlertType.IGNITION_OFF);
                               Context.getDataManager().addAlerts(alert);
                           }
                            }

//                        if(obj.has("ignition")){
//
//
//                                System.out.println("obj.has(IGNITION_OFF)"+obj.has("ignition"));
//
//                                alert.setAl_type(AlertType.IGNITION_OFF);
//                                Context.getDataManager().addAlerts(alert);
//
//                        }

                        if(obj.has("ignition_off")){

//                            System.out.println("888888888888888888888888888888888");
                            test = 2;
                            le.setEvent_type(AlertType.IGNITION_OFF);
                            le.setEvent_type_name(AlertType.IGNITIONOFF);
//                                                                Context.getDataManager().AddLiveEvents(le);
                            alert.setAl_type(AlertType.IGNITION_OFF);
                            Context.getDataManager().addAlerts(alert);
                        }
//                        if(obj.has("ignition")) {
//
//                            System.out.println("true get");
//
//                            if (obj.getString("ignition").equals(true)) {
//                                System.out.println("IGNITIONON get");
//
//                                le.setEvent_type_name(AlertType.IGNITIONON);
////                                                                Context.getDataManager().AddLiveEvents(le);
//                                alert.setAl_type(AlertType.IGNITION_ON);
//                                Context.getDataManager().addAlerts(alert);
//                            } else if (obj.getString("ignition").equals(false)) {
//                                System.out.println("false get");
//                                System.out.println("IGNITION_OFF get");
//
//
//                                le.setEvent_type(AlertType.IGNITION_OFF);
//                                le.setEvent_type_name(AlertType.IGNITIONOFF);
////                                                                Context.getDataManager().AddLiveEvents(le);
//                                alert.setAl_type(AlertType.IGNITION_OFF);
//                                Context.getDataManager().addAlerts(alert);
//                            }
//                        }
                        if(obj.has("bonnet_close")){
                            if(obj.getString("bonnet_close").equals("yes")){
                                le.setEvent_type(AlertType.BONNET_CLOSE);
                                le.setEvent_type_name(AlertType.BONNETCLOSE);
//                                                                  Context.getDataManager().AddLiveEvents(le);
                                alert.setAl_type(AlertType.BONNET_CLOSE);
                                Context.getDataManager().addAlerts(alert);
                            }
                        }
                        if(obj.has("bonnet_open")){

                            if(obj.getString("bonnet_open").equals("yes")){
                                le.setEvent_type(AlertType.BONNET_OPEN);
                                le.setEvent_type_name(AlertType.BONNETCLOSE);
//                                                                  Context.getDataManager().AddLiveEvents(le);
                                alert.setAl_type(AlertType.BONNET_OPEN);
                                Context.getDataManager().addAlerts(alert);
                            }
                        }
                        if(obj.has("alarm")){

                            if(obj.getString("alarm").equals("door_close")){
                                le.setEvent_type(AlertType.DOOR_CLOSE);
                                le.setEvent_type_name(AlertType.DOORCLOSE);
                                // le.setTemperature(rs.getInt("temperature"));

                                alert.setAl_type(AlertType.DOOR_CLOSE);
                                //  alert.setTemperature(rs.getInt("temperature"));

                                Context.getDataManager().addAlerts(alert);
                            }
                        }
                        
                        if(obj.has("alarm")){

                            if(obj.getString("alarm").equals("door_open")){
                                le.setEvent_type(AlertType.DOOR_OPEN);
                                le.setEvent_type_name(AlertType.DOORCLOSE);
                                //  le.setTemperature(rs.getInt("temperature"));
                                alert.setAl_type(AlertType.DOOR_OPEN);
                                // alert.setTemperature(rs.getInt("temperature"));

                                Context.getDataManager().addAlerts(alert);
                            }
                        }
                        if(obj.has("engine_idle")){
                            if(obj.getString("engine_idle").equals("start")){
                                alert.setAl_type(AlertType.ENGINE_IDLE);
                                Context.getDataManager().addAlerts(alert);
                            }
                        }

                        if(obj.has("ac")){
                            if(obj.getString("ac").equals("on")){
                                le.setEvent_type(AlertType.AC_ON);
                                le.setEvent_type_name(AlertType.ACON);
//                                                                        Context.getDataManager().AddLiveEvents(le);
                                alert.setAl_type(AlertType.AC_ON);
                                Context.getDataManager().addAlerts(alert);
                            }
                        }
                        if(obj.has("ac")){
                            if(obj.getString("ac").equals("off")){
                                le.setEvent_type(AlertType.AC_OFF);
                                le.setEvent_type_name(AlertType.ACOff);
//                                                                        Context.getDataManager().AddLiveEvents(le);
                                alert.setAl_type(AlertType.AC_OFF);
                                Context.getDataManager().addAlerts(alert);
                            }
                        }
                        if(obj.has("ramp")){
                            if(obj.getString("ramp").equals("on")){
                                le.setEvent_type(AlertType.RAMP_ON);
                                le.setEvent_type_name(AlertType.RAMPON);
//                                                                        Context.getDataManager().AddLiveEvents(le);
                                alert.setAl_type(AlertType.RAMP_ON);
                                Context.getDataManager().addAlerts(alert);
                            }
                        }
                        if(obj.has("ramp")){
                            if(obj.getString("ramp").equals("off")){

                                le.setEvent_type(AlertType.RAMP_OFF);
                                le.setEvent_type_name(AlertType.RAMPOFF);
//                                                                        Context.getDataManager().AddLiveEvents(le);
                                alert.setAl_type(AlertType.RAMP_OFF);
                                Context.getDataManager().addAlerts(alert);
                            }
                        }
                        if(obj.has("alarm")){
                            if(obj.getString("alarm").equals("sos")){
                                alert.setAl_type(AlertType.SOS);
                                Context.getDataManager().addAlerts(alert);
                            }
                        }
                        if(obj.has("alarm")) {
                            if(obj.getString("alarm").equals("Vibration")) {
                                alert.setAl_type(AlertType.VIBRATION);
                                Context.getDataManager().addAlerts(alert);
                            }
                        }
                        if(obj.has("alarm")){
                            if(obj.getString("alarm").equals("moving")){
                                alert.setAl_type(AlertType.MOVING);
                                Context.getDataManager().addAlerts(alert);
                            }
                        }
                        if(obj.has("alarm")) {
                            if(obj.getString("alarm").equals("displacement")) {
                                alert.setAl_type(AlertType.DISPLACEMENT);
                                Context.getDataManager().addAlerts(alert);
                            }
                        }
                        if(obj.has("alarm")) {
                            if(obj.getString("alarm").equals("illegal_dismantle")) {
                                alert.setAl_type(AlertType.ILLEGAL_DISMANTLE);
                                Context.getDataManager().addAlerts(alert);
                            }

                        }

                        if(obj.has("alarm")){
                            if(obj.getString("alarm").equals("powerCut")){
                                alert.setAl_type(AlertType.POWER_CUTT);
                                Context.getDataManager().addAlerts(alert);
                            }
                        }

                        if(obj.has("alarm")){
                            if(obj.getString("alarm").equals("towing")){
                                alert.setAl_type(AlertType.TOWING);
                                Context.getDataManager().addAlerts(alert);
                            }
                        }

                        if(obj.has("alarm")){
                            if(obj.getString("alarm").equals("overspeed")){
                                alert.setAl_type(AlertType.OVER_SPEPED);
                                Context.getDataManager().addAlerts(alert);
                            }
                        }

                        if(obj.has("alarm")){
                            if(obj.getString("alarm").equals("fallDown")){
                                alert.setAl_type(AlertType.FALLDOWN);
                                Context.getDataManager().addAlerts(alert);
                            }
                        }

                        if(obj.has("alarm")){
                            if(obj.getString("alarm").equals("Crash")){
                                alert.setAl_type(AlertType.Crash);
                                Context.getDataManager().addAlerts(alert);
                            }
                        }

                        if(obj.has("alarm")){
                            if(obj.getString("alarm").equals("dangerousDriving")){
                                alert.setAl_type(AlertType.Dangerous_Driving);
                                Context.getDataManager().addAlerts(alert);
                            }
                        }

                        if(obj.has("alarm")){
                            if(obj.getString("alarm").equals("HARD_CORNERING")){
                                alert.setAl_type(AlertType.HARD_CORNERING);
                                Context.getDataManager().addAlerts(alert);
                            }
                        }

                        if(obj.has("alarm")){
                            if(obj.getString("alarm").equals("lowBattery")){
                                alert.setAl_type(AlertType.LOWBATTERY);
                                Context.getDataManager().addAlerts(alert);
                            }else if(obj.getString("alarm").equals("powerOn")){
                                alert.setAl_type(AlertType.POWER_ON);
                                Context.getDataManager().addAlerts(alert);
                            }
                        }



							/*if(obj.has("trip")){
								if(obj.getString("trip").equals("start")){
									alert.setAl_type(AlertType.MOVING);
									Context.getDataManager().addAlerts(alert);
								}
							}*/

							/*if(rs.getString("obtype")!=null && rs.getString("obtype").equals("lbs")){
								System.out.println("-------------------------------Its lbs data comes right now--------------------------------------"+rs.getString("extrainfo"));
								obj=new JSONObject(rs.getString("extrainfo"));
								CellToLocation.updateLocationForLbs(id,obj);
							}*/

//                                                        insertObservationDetailsRecord(id,date,business_device_id,gps_location,
//									rs.getInt("device_id"),
//									rs.getString("obtype"),
//									rs.getInt("ignition"),
//									rs.getString("address"),
//									rs.getInt("angle"),
//									rs.getDouble("speed"),
//									rs.getDouble("battery"),
//									rs.getInt("trip"),
//									rs.getDouble("mileage"),
//									rs.getString("extrainfo"),
//									rs.getInt("idle"),
//									rs.getInt("fuel"),
//									rs.getInt("ac"),
//                                                                        uuid,
//                                                                        rs.getTimestamp("servertime"),
//                                                                        rs.getDouble("distance"),
//                                                                        rs.getString("protocol"),
//                                                                        rs.getInt("stopage_time"));



                        //	System.out.println("****************************************************************"+rs.getString("extrainfo"));
                        insertOrUpdateLiveRecord(id,date,servertime,business_device_id,gps_location,
                                rs.getInt("device_id"),
                                rs.getString("obtype"),
                                test,
                                rs.getString("address"),
                                rs.getInt("angle"),
                                rs.getDouble("speed"),
                                rs.getDouble("battery"),
                                rs.getInt("trip"),
                                rs.getDouble("mileage"),
                                rs.getString("extrainfo"),
                                rs.getInt("idle"),
                                rs.getInt("fuel"),
                                   rs.getString("protocol"),
                                                                        rs.getInt("port"),
                                rs.getInt("temperature"),
                                rs.getInt("ac"));
//                                                                       uuid);

//                                                        	insertOrUpdateLiveRecordNew(id,date,business_device_id,gps_location,
//									rs.getInt("device_id"),
//									rs.getString("obtype"),
//									rs.getInt("ignition"),
//									rs.getString("address"),
//									rs.getInt("angle"),
//									rs.getDouble("speed"),
//									rs.getDouble("battery"),
//									rs.getInt("trip"),
//									rs.getDouble("mileage"),
//									rs.getString("extrainfo"),
//									rs.getInt("idle"),
//									rs.getInt("fuel"),
//									rs.getInt("ac"),
//                                                                       uuid);
                    }
                    return resultSet.getString(1);
                }
            }
        } finally {
            statement.close();
            connection.close();
        }
    }
    return null;
}

//private void insertOrUpdateLiveRecord(String uuid, Date date, Integer business_device_id, String gps_location, int device_id, String obtype, int ignition, String address, int angle, double speed,double battery,int trip,double mileage,String extrainfo,int idle,int fuel,int temp, int ac) throws SQLException {
//
//		Geometry geom= CheckForGeoFence.wktToGeometry(gps_location);
//	//	System.out.println(observation_id+"--business_device_id--"+business_device_id+"--gps_location---"+geom.getCoordinate().x+"--"+geom.getCoordinate().y+"-----------extrainfo------"+extrainfo);
//
//
//
//		String check_live_obs="select * from web.live_ob where business_device_id=?";
//		long diff = 0;
//		int diffminute=0;
//    try {
//        if(date!=null){
//            diff= DateSync.getUTCTime().getTime()-date.getTime();
//            diffminute = (int)diff / (60 * 1000) % 60;
//        }
//    } catch (ParseException e1) {
//        // TODO Auto-generated catch block
//        e1.printStackTrace();
//    }
//
//
//		try{
//			statement=connection.prepareStatement(check_live_obs);
//			statement.setInt(1, business_device_id);
//                    System.out.println("business_device_id ============>"+business_device_id);
//			ResultSet rs=statement.executeQuery();
//
//		//	System.out.println("Difference of date-----------------------"+diffminute+"--------------"+diff);
//			if(business_device_id!=0){
//				if(rs.next()){
//     PreparedStatement preparedStatement1 = null;
//      ResultSet rs1 = null;
//   String notification = "select * from web.notification where business_device_id = ? order by notification_id desc limit 1";
//
//    preparedStatement1 = connection.prepareStatement(notification);
//    preparedStatement1.setInt(1, business_device_id);
//
//                        rs1 = preparedStatement1.executeQuery();
//                     String ob_id = null;
//                     Integer altype = 0;
//                        if(rs1.next())
//                    {
//
//                         ob_id = rs1.getString("uuid");
//                        System.out.println("============================="+rs1.getString("uuid"));
//
//                        System.out.println("============================="+ob_id);
//                        System.out.println("=============================");
//
//                        System.out.println("=============================");
//
//                        System.out.println("ob_id"+ob_id);
//                        altype = rs1.getInt("altype");
//                     }
//
//                                     double cmileage = rs.getDouble("cmileage");
//
//
//                                        LiveObervation liveObervation=new LiveObervation();
//                                        if(cmileage == 0.0)
//                                        {
//                                           liveObervation.setCmileage(mileage);
//                                           liveObervation.setBusiness_device_id(business_device_id);
//                                           Context.getDataManager().updateLiveObservationMileage(liveObervation);
//                                        }
//
//                                        if(ob_id == uuid)
//                                        {
//                                               liveObervation.setAl_type(altype);
//                                        }else{
//                                                liveObervation.setAl_type(0);
//                                        }
//
//					liveObervation.setDevice_id(device_id);
//					liveObervation.setGps_time(date);
//					liveObervation.setLatitude(geom.getCoordinate().y);
//					liveObervation.setLongitude(geom.getCoordinate().x);
//	//				liveObervation.setObservation_id(observation_id);
//					liveObervation.setAddress(address);
//					liveObervation.setBusiness_device_id(business_device_id);
//					liveObervation.setCourse(angle);
//					liveObervation.setIgnition(ignition);
//					liveObervation.setTrip(trip);
//					liveObervation.setObtype(obtype);
//					liveObervation.setBattery(battery);
//					liveObervation.setSpeed(speed);
//					liveObervation.setMileage(mileage);
//					liveObervation.setExtra(extrainfo);
//					liveObervation.setDate_diff(diffminute);
//					liveObervation.setIdle(idle);
//					liveObervation.setFuel(fuel);
//                    liveObervation.setTemperature(temp);
//					liveObervation.setAc(ac);
//                                       liveObervation.setUuid(uuid);
//
//					Context.getDataManager().updateLiveObservation(liveObervation);
//
//				}else{
//
//   PreparedStatement preparedStatement2 = null;
//   ResultSet rs2 = null;
//   String notification = "select * from web.notification where business_device_id= ? order by notification_id desc limit 1";
//                                    System.out.println("notification"+notification);
//                preparedStatement2 = connection.prepareStatement(notification);
//                preparedStatement2.setInt(1, business_device_id);
//                rs2 = preparedStatement2.executeQuery();
//                String ob_id = null;
//                Integer altype = 0;
//                       if(rs2.next())
//                   {
//                       System.out.println("============================="+rs2.getString("uuid"));
//
//                       System.out.println("============================="+ob_id);
//                       System.out.println("=============================");
//
//                       System.out.println("=============================");
//                        ob_id = rs2.getString("uuid");
//                       altype = rs2.getInt("altype");
//
//                   }
//                                                           //System.out.println("Comes to first time insert........");
//
//					LiveObervation liveObervation=new LiveObervation();
//
//                                        if(ob_id == uuid)
//                                        {
//                                               liveObervation.setAl_type(altype);
//                                        }else{
//                                                liveObervation.setAl_type(0);
//                                        }
//
//					liveObervation.setDevice_id(device_id);
//		//			liveObervation.setObservation_id(observation_id);
//					liveObervation.setBusiness_device_id(business_device_id);
//					liveObervation.setLatitude(geom.getCoordinate().y);
//					liveObervation.setLongitude(geom.getCoordinate().x);
//					liveObervation.setGps_time(date);
//					liveObervation.setObtype(obtype);
//					liveObervation.setIgnition(ignition);
//					liveObervation.setTrip(trip);
//					liveObervation.setSpeed(speed);
//					liveObervation.setCourse(angle);
//					liveObervation.setAddress(address);
//					liveObervation.setBattery(battery);
//					liveObervation.setMileage(mileage);
//					liveObervation.setExtra(extrainfo);
//					liveObervation.setDate_diff(diffminute);
//					liveObervation.setIdle(idle);
//					liveObervation.setFuel(fuel);
//                                        liveObervation.setTemperature(temp);
//					liveObervation.setAc(ac);
//                                        liveObervation.setUuid(uuid);
//
//					Context.getDataManager().addLiveObservation(liveObervation);
//				}
//			}
//		}catch(Exception e){
//			e.printStackTrace();
//		}finally {
//			//statement.close();
//			//connection.close();
//		}
//
//
//
//	}
private void insertOrUpdateLiveRecord(String uuid, Date date,Date servertime, Integer business_device_id, String gps_location, int device_id, String obtype, int ignition, String address, int angle, double speed,double battery,int trip,double mileage,String extrainfo,int idle,int fuel,String protocol,int port,int temp, int ac) throws SQLException {

    Geometry geom=CheckForGeoFence.wktToGeometry(gps_location);
//		System.out.println(observation_id+"--business_device_id--"+business_device_id+"--gps_location---"+geom.getCoordinate().x+"--"+geom.getCoordinate().y+"-----------extrainfo------"+extrainfo);





    String check_live_obs="select * from web.live_ob where business_device_id=?";
    long diff = 0;
    int diffminute=0;
    try {
        if(date!=null){
            diff=DateSync.getUTCTime().getTime()-date.getTime();
            diffminute = (int)diff / (60 * 1000) % 60;
        }
    } catch (ParseException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
    }


    try{
        statement=connection.prepareStatement(check_live_obs);
        statement.setInt(1, business_device_id);
        ResultSet rs=statement.executeQuery();

        //	System.out.println("Difference of date-----------------------"+diffminute+"--------------"+diff);
        if(business_device_id!=0){
            if(rs.next()){
                PreparedStatement preparedStatement1 = null;
                ResultSet rs1 = null;
                String notification = "select * from web.notification where business_device_id = ? order by notification_id desc limit 1";

                preparedStatement1 = connection.prepareStatement(notification);
                preparedStatement1.setInt(1, business_device_id);

                rs1 = preparedStatement1.executeQuery();
                String ob_id = null;
                Integer altype = 0;
                if(rs1.next())
                {

                    ob_id = rs1.getString("uuid");
                    altype = rs1.getInt("altype");
                }

                double cmileage = rs.getDouble("cmileage");


                LiveObervation liveObervation=new LiveObervation();
                if(cmileage == 0.0)
                {
                    liveObervation.setCmileage(mileage);
                    liveObervation.setBusiness_device_id(business_device_id);
                    Context.getDataManager().updateLiveObservationMileage(liveObervation);
                }

                if(ob_id == uuid)
                {
                    liveObervation.setAl_type(altype);
                }else{
                    liveObervation.setAl_type(0);
                }

                liveObervation.setDevice_id(device_id);
                liveObervation.setGps_time(date);
                liveObervation.setLatitude(geom.getCoordinate().y);

//                System.out.println("Latitude===============>"+geom.getCoordinate().y);
//
//                System.out.println("sLongitude=============>"+geom.getCoordinate().x);

                liveObervation.setLongitude(geom.getCoordinate().x);
                liveObervation.setUuid(uuid);
                                                        liveObervation.setServerTime(servertime);

                liveObervation.setAddress(address);
                liveObervation.setBusiness_device_id(business_device_id);
                liveObervation.setCourse(angle);
                liveObervation.setIgnition(ignition);
                liveObervation.setTrip(trip);
                liveObervation.setObtype(obtype);
                liveObervation.setBattery(battery);
                liveObervation.setSpeed(speed);
                liveObervation.setMileage(mileage);
                liveObervation.setExtra(extrainfo);
                liveObervation.setDate_diff(diffminute);
                liveObervation.setIdle(idle);
                liveObervation.setFuel(fuel);
                liveObervation.setTemperature(temp);
                liveObervation.setAc(ac);
                  liveObervation.setProtocol(protocol);
                                        liveObervation.setPort(port);



                Context.getDataManager().updateLiveObservation(liveObervation);

            }else{

                PreparedStatement preparedStatement2 = null;
                ResultSet rs2 = null;
                String notification = "select * from web.notification where business_device_id= ? order by notification_id desc limit 1";

                preparedStatement2 = connection.prepareStatement(notification);
                preparedStatement2.setInt(1, business_device_id);
                rs2 = preparedStatement2.executeQuery();
                String ob_id = null;
                Integer altype = 0;
                if(rs2.next())
                {

                    ob_id = rs2.getString("uuid");
                    altype = rs2.getInt("altype");

                }
                //System.out.println("Comes to first time insert........");

                LiveObervation liveObervation=new LiveObervation();

                if(ob_id == uuid)
                {
                    liveObervation.setAl_type(altype);
                }else{
                    liveObervation.setAl_type(0);
                }

                liveObervation.setDevice_id(device_id);
                liveObervation.setUuid(uuid);
                liveObervation.setBusiness_device_id(business_device_id);
                liveObervation.setLatitude(geom.getCoordinate().y);
                liveObervation.setLongitude(geom.getCoordinate().x);
                liveObervation.setGps_time(date);
                                                        liveObervation.setServerTime(servertime);

                liveObervation.setObtype(obtype);
                liveObervation.setIgnition(ignition);
                liveObervation.setTrip(trip);
                liveObervation.setSpeed(speed);
                liveObervation.setCourse(angle);
                liveObervation.setAddress(address);
                liveObervation.setBattery(battery);
                liveObervation.setMileage(mileage);
                liveObervation.setExtra(extrainfo);
                liveObervation.setDate_diff(diffminute);
                liveObervation.setIdle(idle);
                liveObervation.setFuel(fuel);
                liveObervation.setTemperature(temp);
                liveObervation.setAc(ac);
                liveObervation.setUuid(uuid);

 liveObervation.setProtocol(protocol);
                                        liveObervation.setPort(port);
                Context.getDataManager().addLiveObservation(liveObervation);
            }
        }
    }catch(Exception e){
        e.printStackTrace();
    }finally {
        //statement.close();
        //connection.close();
    }



}

}
