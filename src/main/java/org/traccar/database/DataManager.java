/*
 * Copyright 2012 - 2017 Anton Tananaev (anton@traccar.org)
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

import com.vividsolutions.jts.geom.Geometry;
import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.SQLException;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.config.Config;
import org.traccar.Context;
import org.traccar.helper.DateUtil;
import org.traccar.model.Attribute;
import org.traccar.model.Device;
import org.traccar.model.Driver;
import org.traccar.model.Event;
import org.traccar.model.Geofence;
import org.traccar.model.Group;
import org.traccar.model.Maintenance;
import org.traccar.model.ManagedUser;
import org.traccar.model.Notification;
import org.traccar.model.Permission;
import org.traccar.model.BaseModel;
import org.traccar.model.Calendar;
import org.traccar.model.Command;
import org.traccar.model.Position;
import org.traccar.model.Server;
import org.traccar.model.Statistics;
import org.traccar.model.User;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.json.JsonObject;
import org.traccar.NominatimCall;
import org.traccar.PrintOut;
import org.traccar.PushNotification;
import org.traccar.common.CheckForGeoFence;
import org.traccar.model.Alert;
import org.traccar.model.CommandHistory;
import org.traccar.model.DSCHisory;
import org.traccar.model.LiveEvents;
import org.traccar.model.LiveObervation;
import org.traccar.model.MiscFormatter;
import org.traccar.model.VideoData;

public class DataManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataManager.class);
  private Map<String, Device> devicesByPhone;
    private AtomicLong devicesLastUpdate = new AtomicLong();
private final Map<Long, Device> devicesById = new HashMap<>();
// private final Map<String, Long> deviceIdMap = new HashMap<>();
    private Map<String, Device> devicesByUniqueId = new HashMap<>();
      private AtomicLong devicesUpdate = new AtomicLong();
    public static final String ACTION_SELECT_ALL = "selectAll";
    public static final String ACTION_SELECT = "select";
    public static final String ACTION_INSERT = "insert";
    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_DELETE = "delete";



    private final Config config;

    private DataSource dataSource;
    private boolean generateQueries;

    private boolean forceLdap;
 public int getBusinessDeviceId(int device_id) throws ClassNotFoundException, SQLException {
        int id = 0;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            //Class.forName("org.postgresql.Driver");
            //connection = DriverManager.getConnection("jdbc:postgresql://192.168.1.34:5432/trackio","track", "trackio");
            connection = dataSource.getConnection();
            String selectSQL = "SELECT business_device_id from web.business_device WHERE device_device_id=?  AND is_active=true AND is_deleted=false order by business_device_id desc LIMIT 1;";
            preparedStatement = connection.prepareStatement(selectSQL);
            preparedStatement.setInt(1, device_id);

            // execute select SQL stetement
            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                id = rs.getInt("business_device_id");
//                System.out.println("org.traccar.database.DataManager.getBusinessDeviceId()"+id);
//                PrintOut.PrintOutString("business device id--------->"+id);
            }
        } catch (SQLException e) {
//            System.out.println(e.getMessage());
        } finally {
            if (connection != null) {
                connection.close();
                preparedStatement.close();
                rs.close();
      //          PrintOut.PrintOutString("connection was closed now.");
            }
        }
        return id;
    }

public void AddLiveEvents(LiveEvents le) throws SQLException {
        le.setId(QueryBuilder.create(dataSource, getQuery("database.addLiveEvents"), true)
                .setObject(le)
                .setLong("device_id", le.getDeviceId())
                .executeUpdate());

    }


      public void addAlerts(Alert alert) throws SQLException {

   int check =0;
    int ramp =0;
//          System.out.println("add alerts test");
          if(alert.getAl_type() == 1 || alert.getAl_type() == 2 )
        {
        check = checkIgnitionOnOrOFF(alert.getBusiness_device_id());
//            System.out.println("1 or 2 ");
//            System.out.println("1 or 2 "+check);


        }
          
          
          if(alert.getAl_type() == 32 || alert.getAl_type() == 33 )
        {
          ramp = checkRampOnOrOFF(alert.getBusiness_device_id()); 
//            System.out.println("32 or 33 ");


        }
         
          
        
         
       if(alert.getAl_type() == 32 || alert.getAl_type() == 33)
       {
        if ((ramp == 0 || alert.getAl_type() != ramp)) {
//            System.out.println("ramp" );
            alert.setId(QueryBuilder.create(dataSource, getQuery("database.addalert"), true)
                    .setObject(alert)
                    .setLong("device_id", alert.getDeviceId())
                    .executeUpdateAndReturnAlert());
//            System.out.println("32 or 33");
        }
       }else{
            if ((check == 0 || alert.getAl_type() != check)) {
//                System.out.println("executeUpdateAndReturnAlert");
            alert.setId(QueryBuilder.create(dataSource, getQuery("database.addalert"), true)
                    .setObject(alert)
                    .setLong("device_id", alert.getDeviceId())
                    .executeUpdateAndReturnAlert());
        }
       }
    }


// public int checkIgnitionOnOrOFF(Integer business_device_id) throws SQLException {
//        int response = 0;
//        Connection connection = null;
//        PreparedStatement preparedStatement = null;
//        ResultSet rs = null;
//        try {
//            connection = dataSource.getConnection();
//            String selectSQL = "SELECT altype from web.notification where (altype=1 OR altype=2) AND business_device_id=? ORDER BY notification_id DESC LIMIT 1";
//            System.out.println("selectSQL"+selectSQL);
//            preparedStatement = connection.prepareStatement(selectSQL);
//
//            preparedStatement.setInt(1, business_device_id);
//            rs = preparedStatement.executeQuery();
//            if (rs.next()) {
//                response = rs.getInt("altype");
//            }
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        } finally {
//            if (connection != null) {
//                connection.close();
//                preparedStatement.close();
//                rs.close();
//                PrintOut.PrintOutString("connection was closed now.");
//            }
//        }
//        return response;
//    }
//
public int checkIgnitionOnOrOFF(Integer business_device_id) throws SQLException {
    int response = 0;
    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet rs = null;
    try {
        connection = dataSource.getConnection();
        String selectSQL = "SELECT altype from web.notification where (altype=1 OR altype=2) AND business_device_id=? ORDER BY notification_id DESC LIMIT 1";
        preparedStatement = connection.prepareStatement(selectSQL);

        preparedStatement.setInt(1, business_device_id);
        rs = preparedStatement.executeQuery();
        if (rs.next()) {
            response = rs.getInt("altype");
        }
    } catch (SQLException e) {
//        System.out.println(e.getMessage());
    } finally {
        if (connection != null) {
            connection.close();
            preparedStatement.close();
            rs.close();
            PrintOut.PrintOutString("connection was closed now.");
        }
    }
    return response;
}
     
     public int checkRampOnOrOFF(Integer business_device_id) throws SQLException {
        int response = 0;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            String selectSQL = "SELECT altype from web.notification where (altype=32 OR altype=33) AND business_device_id=? ORDER BY notification_id DESC LIMIT 1";
            preparedStatement = connection.prepareStatement(selectSQL);

            preparedStatement.setInt(1, business_device_id);
            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                response = rs.getInt("altype");
            }
        } catch (SQLException e) {
//            System.out.println(e.getMessage());
        } finally {
            if (connection != null) {
                connection.close();
                preparedStatement.close();
                rs.close();
                PrintOut.PrintOutString("connection was closed now.");
            }
        }
        return response;
    }

 public void addLiveObservation(LiveObervation liveObervation) throws SQLException {
//     System.out.println("###############################");
        liveObervation.setId(QueryBuilder.create(dataSource, getQuery("database.add_live_obs"), true)
                .setObject(liveObervation)
                .executeUpdateLiveObservation());
    }
 public void updateLiveObservationMileage(LiveObervation liveObervation) throws SQLException {
        liveObervation.setId(QueryBuilder.create(dataSource, getQuery("database.update_live_ob_cmileage"), true)
                .setInteger("business_device_id", liveObervation.getBusiness_device_id())
                .setObject(liveObervation)
                .executeUpdate());
    }

    public void updateLiveObservation(LiveObervation liveObervation) throws SQLException {
        liveObervation.setId(QueryBuilder.create(dataSource, getQuery("database.update_live_obs"), true)
                .setInteger("business_device_id", liveObervation.getBusiness_device_id())
                .setObject(liveObervation)
                .executeUpdateLiveObservation());
    }

    public Device getDeviceById(long id) {
        try {
//            System.out.println("trupti"+id);
            updateDeviceCache(!devicesById.containsKey(id));
        } catch (SQLException e) {
            LOGGER.warn("updateDeviceCache----->",e);
        }
        return devicesById.get(id);
    }
    public void updateDeviceCache(boolean force) throws SQLException {
        long lastUpdate = devicesUpdate.get();
//        if ((force || System.currentTimeMillis() - lastUpdate > devicesRefreshDelay)
//                && devicesUpdate.compareAndSet(lastUpdate, System.currentTimeMillis())) {
////            refreshItems();
//        }

//           if (System.currentTimeMillis() - devicesLastUpdate > devicesRefreshDelay || force) {
//            devicesById.clear();
//            devicesByUniqueId.clear();
//           
//            
//            for (Device device : getAllDevices()) {
//                
//                devicesById.put(device.getId(), device);
//                devicesByUniqueId.put(device.getUniqueId(), device);
////                continue;
////                System.out.println("deviceById----------"+devicesById.toString());
////                 System.out.println("devicesByUniqueId-----------"+devicesByUniqueId.toString());
//
//            }
////            System.out.println("deviceById----------"+devicesById);
////             System.out.println("devicesByUniqueId-----------"+devicesByUniqueId.values());
//            devicesLastUpdate = System.currentTimeMillis();
//
////            refreshItems();
//
//        }
    }
    public Device getDeviceByUniqueId(String uniqueId) throws SQLException {
//     boolean forceUpdate =  !devicesByUniqueId.containsKey(uniqueId) && !config.getBoolean("database.ignoreUnknown");
     
     if(!devicesByUniqueId.containsKey(uniqueId)){
         devicesByUniqueId.clear();
        
            Collection<Device> deviceList= getAllDevicesa();
            PrintOut.PrintOutString("deviceid size--------->"+deviceList.size());
//              System.out.println("uniqueId()"+uniqueId);
//              System.out.println("deviceList"+deviceList.size());

            for (Device device : deviceList) {
                             // System.out.println("device.getUniqueId()"+device.getUniqueId());

                    devicesByUniqueId.put(device.getUniqueId(), device);
                   devicesById.put(device.getId(), device);
                                              //    System.out.println("device.getId()"+device.getId());

                    
            } 
     } 
//        updateDeviceCache(forceUpdate);
//        System.out.println("ignoreUnknown------------->"+!config.getBoolean("database.ignoreUnknown"));
//        PrintOut.PrintOutString("device------------->"+!devicesByUniqueId.containsKey(uniqueId));
//        return devicesByUniqueId.get(uniqueId);
   return devicesByUniqueId.get(uniqueId);
    }
public Collection<Device> getAllDevicesa() throws SQLException {
        return QueryBuilder.create(dataSource, getQuery("database.selectDevicesAll"))
                .executeQuery(Device.class);
    }
    public DataManager(Config config) throws Exception {
        this.config = config;

        forceLdap = config.getBoolean("ldap.force");

        initDatabase();
        initDatabaseSchema();
    }
 public void AddDscHistoryVersion(DSCHisory dsc1) throws SQLException {
     System.out.println("add jar details in that");
        dsc1.setWeb_jar_id(QueryBuilder.create(dataSource, getQuery("database.addDSCHistory"), true)
                .setObject(dsc1)
                .setDate("created_time", new Date())
                .executeUpdate());

    }

public void AddVideoData(VideoData vd) throws SQLException {
        vd.setVideo_id(QueryBuilder.create(dataSource, getQuery("database.addvideoData"), true)
                .setObject(vd)
                .setDate("created_time", new Date())
                .executeUpdate());

    }  
    private void initDatabase() throws Exception {

        String jndiName = config.getString("database.jndi");

        if (jndiName != null) {

            dataSource = (DataSource) new InitialContext().lookup(jndiName);

        } else {

            String driverFile = config.getString("database.driverFile");
            if (driverFile != null) {
                ClassLoader classLoader = ClassLoader.getSystemClassLoader();
                try {
                    Method method = classLoader.getClass().getDeclaredMethod("addURL", URL.class);
                    method.setAccessible(true);
                    method.invoke(classLoader, new File(driverFile).toURI().toURL());
                } catch (NoSuchMethodException e) {
                    Method method = classLoader.getClass()
                            .getDeclaredMethod("appendToClassPathForInstrumentation", String.class);
                    method.setAccessible(true);
                    method.invoke(classLoader, driverFile);
                }
            }

            String driver = config.getString("database.driver");
            if (driver != null) {
                Class.forName(driver);
            }

            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setDriverClassName(config.getString("database.driver"));
            hikariConfig.setJdbcUrl(config.getString("database.url"));
            hikariConfig.setUsername(config.getString("database.user"));
            hikariConfig.setPassword(config.getString("database.password"));
            hikariConfig.setConnectionInitSql(config.getString("database.checkConnection", "SELECT 1"));
            hikariConfig.setIdleTimeout(600000);

            int maxPoolSize = config.getInteger("database.maxPoolSize");

            if (maxPoolSize != 0) {
                hikariConfig.setMaximumPoolSize(maxPoolSize);
            }

            generateQueries = config.getBoolean("database.generateQueries");

            dataSource = new HikariDataSource(hikariConfig);

        }
    }

    public static String constructObjectQuery(String action, Class<?> clazz, boolean extended) {
        switch (action) {
            case ACTION_INSERT:
            case ACTION_UPDATE:
                StringBuilder result = new StringBuilder();
                StringBuilder fields = new StringBuilder();
                StringBuilder values = new StringBuilder();

                Set<Method> methods = new HashSet<>(Arrays.asList(clazz.getMethods()));
                methods.removeAll(Arrays.asList(Object.class.getMethods()));
                methods.removeAll(Arrays.asList(BaseModel.class.getMethods()));
                for (Method method : methods) {
                    boolean skip;
                    if (extended) {
                        skip = !method.isAnnotationPresent(QueryExtended.class);
                    } else {
                        skip = method.isAnnotationPresent(QueryIgnore.class)
                                || method.isAnnotationPresent(QueryExtended.class) && !action.equals(ACTION_INSERT);
                    }
                    if (!skip && method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
                        String name = Introspector.decapitalize(method.getName().substring(3));
                        if (action.equals(ACTION_INSERT)) {
                            fields.append(name).append(", ");
                            values.append(":").append(name).append(", ");
                        } else {
                            fields.append(name).append(" = :").append(name).append(", ");
                        }
                    }
                }
                fields.setLength(fields.length() - 2);
                if (action.equals(ACTION_INSERT)) {
                    values.setLength(values.length() - 2);
                    result.append("INSERT INTO ").append(getObjectsTableName(clazz)).append(" (");
                    result.append(fields).append(") ");
                    result.append("VALUES (").append(values).append(")");
                } else {
                    result.append("UPDATE ").append(getObjectsTableName(clazz)).append(" SET ");
                    result.append(fields);
                    result.append(" WHERE id = :id");
                }
                return result.toString();
            case ACTION_SELECT_ALL:
                return "SELECT * FROM " + getObjectsTableName(clazz);
            case ACTION_SELECT:
                return "SELECT * FROM " + getObjectsTableName(clazz) + " WHERE id = :id";
            case ACTION_DELETE:
                return "DELETE FROM " + getObjectsTableName(clazz) + " WHERE id = :id";
            default:
                throw new IllegalArgumentException("Unknown action");
        }
    }

    public static String constructPermissionQuery(String action, Class<?> owner, Class<?> property) {
        switch (action) {
            case ACTION_SELECT_ALL:
                return "SELECT " + makeNameId(owner) + ", " + makeNameId(property) + " FROM "
                        + getPermissionsTableName(owner, property);
            case ACTION_INSERT:
                return "INSERT INTO " + getPermissionsTableName(owner, property)
                        + " (" + makeNameId(owner) + ", " + makeNameId(property) + ") VALUES (:"
                        + makeNameId(owner) + ", :" + makeNameId(property) + ")";
            case ACTION_DELETE:
                return "DELETE FROM " + getPermissionsTableName(owner, property)
                        + " WHERE " + makeNameId(owner) + " = :" + makeNameId(owner)
                        + " AND " + makeNameId(property) + " = :" + makeNameId(property);
            default:
                throw new IllegalArgumentException("Unknown action");
        }
    }

    private String getQuery(String key) {
        String query = config.getString(key);
        if (query == null) {
            LOGGER.info("Query not provided: " + key);
        }
        return query;
    }

    public String getQuery(String action, Class<?> clazz) {
        return getQuery(action, clazz, false);
    }

    public String getQuery(String action, Class<?> clazz, boolean extended) {
        String queryName;
        if (action.equals(ACTION_SELECT_ALL)) {
            queryName = "database.select" + clazz.getSimpleName() + "s";
        } else {
            queryName = "database." + action.toLowerCase() + clazz.getSimpleName();
            if (extended) {
                queryName += "Extended";
            }
        }
        String query = config.getString(queryName);
        if (query == null) {
            if (generateQueries) {
                query = constructObjectQuery(action, clazz, extended);
                config.setString(queryName, query);
            } else {
                LOGGER.info("Query not provided: " + queryName);
            }
        }

        return query;
    }

    public String getQuery(String action, Class<?> owner, Class<?> property) {
        String queryName;
        switch (action) {
            case ACTION_SELECT_ALL:
                queryName = "database.select" + owner.getSimpleName() + property.getSimpleName() + "s";
                break;
            case ACTION_INSERT:
                queryName = "database.link" + owner.getSimpleName() + property.getSimpleName();
                break;
            default:
                queryName = "database.unlink" + owner.getSimpleName() + property.getSimpleName();
                break;
        }
        String query = config.getString(queryName);
        if (query == null) {
            if (generateQueries) {
                query = constructPermissionQuery(action, owner,
                        property.equals(User.class) ? ManagedUser.class : property);
                config.setString(queryName, query);
            } else {
                LOGGER.info("Query not provided: " + queryName);
            }
        }

        return query;
    }

    private static String getPermissionsTableName(Class<?> owner, Class<?> property) {
        String propertyName = property.getSimpleName();
        if (propertyName.equals("ManagedUser")) {
            propertyName = "User";
        }
        return "tc_" + Introspector.decapitalize(owner.getSimpleName())
                + "_" + Introspector.decapitalize(propertyName);
    }

    private static String getObjectsTableName(Class<?> clazz) {
        String result = "tc_" + Introspector.decapitalize(clazz.getSimpleName());
        // Add "s" ending if object name is not plural already
        if (!result.endsWith("s")) {
            result += "s";
        }
        return result;
    }

    private void initDatabaseSchema() throws SQLException, LiquibaseException {

       // if (config.hasKey("database.changelog")) {

            ResourceAccessor resourceAccessor = new FileSystemResourceAccessor();

            Database database = DatabaseFactory.getInstance().openDatabase(
                    config.getString("database.url"),
                    config.getString("database.user"),
                    config.getString("database.password"),
                    config.getString("database.driver"),
                    null, null, null, resourceAccessor);

//            Liquibase liquibase = new Liquibase(
//                    config.getString("database.changelog"), resourceAccessor, database);

//            liquibase.clearCheckSums();
//
//            liquibase.update(new Contexts());
   //     }
    }

    public User login(String email, String password) throws SQLException {

//     System.out.println("......    email  .............  " + email);
//        System.out.println("......    password  .............  " + password);

        User user = QueryBuilder.create(dataSource, getQuery("database.loginUser"))
                .setString("email", email.trim())
                .executeQuerySingle(User.class);
        LdapProvider ldapProvider = Context.getLdapProvider();
        if (user != null) {

//            System.out.println("......    user  .............  " + password);

            if (ldapProvider != null && user.getLogin() != null && ldapProvider.login(user.getLogin(), password)
                    || !forceLdap && user.isPasswordValid(password)) {
                return user;
            }
        } else {
            if (ldapProvider != null && ldapProvider.login(email, password)) {
                user = ldapProvider.getUser(email);
                Context.getUsersManager().addItem(user);
                return user;
            }
        }
        return null;
    }

//    public void updateDeviceStatus(Device device) throws SQLException {
//        QueryBuilder.create(dataSource, getQuery(ACTION_UPDATE, Device.class, true))
//                .setObject(device)
//                .executeUpdate();
//    }

    public void updateDeviceStatus(Device device) throws SQLException {
        QueryBuilder.create(dataSource, getQuery("database.updateDeviceStatus"))
                .setObject(device)
                .executeUpdate();
    }
    public Collection<Position> getPositions(long deviceId, Date from, Date to) throws SQLException {
        return QueryBuilder.create(dataSource, getQuery("database.selectPositions"))
                .setLong("deviceId", deviceId)
                .setDate("from", from)
                .setDate("to", to)
                .executeQuery(Position.class);
    }

    public void updateLatestPosition(Position position) throws SQLException {
        QueryBuilder.create(dataSource, getQuery("database.updateLatestPosition"))
                .setDate("now", new Date())
                .setObject(position)
                .executeUpdate();
    }

    public Collection<Position> getLatestPositions() throws SQLException {
        return QueryBuilder.create(dataSource, getQuery("database.selectLatestPositions"))
                .executeQuery(Position.class);
    }

    public void clearHistory() throws SQLException {
        long historyDays = config.getInteger("database.historyDays");
        if (historyDays != 0) {
            Date timeLimit = new Date(System.currentTimeMillis() - historyDays * 24 * 3600 * 1000);
            LOGGER.info("Clearing history earlier than " + DateUtil.formatDate(timeLimit, false));
            QueryBuilder.create(dataSource, getQuery("database.deletePositions"))
                    .setDate("serverTime", timeLimit)
                    .executeUpdate();
            QueryBuilder.create(dataSource, getQuery("database.deleteEvents"))
                    .setDate("serverTime", timeLimit)
                    .executeUpdate();
        }
    }

    public Server getServer() throws SQLException {
        return QueryBuilder.create(dataSource, getQuery(ACTION_SELECT_ALL, Server.class))
                .executeQuerySingle(Server.class);
    }

    public Collection<Event> getEvents(long deviceId, Date from, Date to) throws SQLException {
        return QueryBuilder.create(dataSource, getQuery("database.selectEvents"))
                .setLong("deviceId", deviceId)
                .setDate("from", from)
                .setDate("to", to)
                .executeQuery(Event.class);
    }

    public Collection<Statistics> getStatistics(Date from, Date to) throws SQLException {
        return QueryBuilder.create(dataSource, getQuery("database.selectStatistics"))
                .setDate("from", from)
                .setDate("to", to)
                .executeQuery(Statistics.class);
    }

    public static Class<?> getClassByName(String name) throws ClassNotFoundException {
        switch (name.toLowerCase().replace("id", "")) {
            case "device":
                return Device.class;
            case "group":
                return Group.class;
            case "user":
                return User.class;
            case "manageduser":
                return ManagedUser.class;
            case "geofence":
                return Geofence.class;
            case "driver":
                return Driver.class;
            case "attribute":
                return Attribute.class;
            case "calendar":
                return Calendar.class;
            case "command":
                return Command.class;
            case "maintenance":
                return Maintenance.class;
            case "notification":
                return Notification.class;
            default:
                throw new ClassNotFoundException();
        }
    }

    private static String makeNameId(Class<?> clazz) {
        String name = clazz.getSimpleName();
        return Introspector.decapitalize(name) + (!name.contains("Id") ? "Id" : "");
    }

    public Collection<Permission> getPermissions(Class<? extends BaseModel> owner, Class<? extends BaseModel> property)
            throws SQLException, ClassNotFoundException {
        return QueryBuilder.create(dataSource, getQuery(ACTION_SELECT_ALL, owner, property))
                .executePermissionsQuery();
    }

    public void linkObject(Class<?> owner, long ownerId, Class<?> property, long propertyId, boolean link)
            throws SQLException {
        QueryBuilder.create(dataSource, getQuery(link ? ACTION_INSERT : ACTION_DELETE, owner, property))
                .setLong(makeNameId(owner), ownerId)
                .setLong(makeNameId(property), propertyId)
                .executeUpdate();
    }

    public <T extends BaseModel> T getObject(Class<T> clazz, long entityId) throws SQLException {
        return QueryBuilder.create(dataSource, getQuery(ACTION_SELECT, clazz))
                .setLong("id", entityId)
                .executeQuerySingle(clazz);
    }

    public <T extends BaseModel> Collection<T> getObjects(Class<T> clazz) throws SQLException {
        return QueryBuilder.create(dataSource, getQuery(ACTION_SELECT_ALL, clazz))
                .executeQuery(clazz);
    }

    public void addObject(BaseModel entity) throws SQLException {
        entity.setId(QueryBuilder.create(dataSource, getQuery(ACTION_INSERT, entity.getClass()), true)
                .setObject(entity)
                .executeUpdate());
    }

    public void updateObject(BaseModel entity) throws SQLException {
        QueryBuilder.create(dataSource, getQuery(ACTION_UPDATE, entity.getClass()))
                .setObject(entity)
                .executeUpdate();
        if (entity instanceof User && ((User) entity).getHashedPassword() != null) {
            QueryBuilder.create(dataSource, getQuery(ACTION_UPDATE, User.class, true))
                    .setObject(entity)
                    .executeUpdate();
        }
    }

    public void removeObject(Class<? extends BaseModel> clazz, long entityId) throws SQLException {
        QueryBuilder.create(dataSource, getQuery(ACTION_DELETE, clazz))
                .setLong("id", entityId)
                .executeUpdate();
    }
    public void vehicleShiftExecution(int business_device_id, Position position) throws SQLException {
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Timestamp shift_start_time = null;
        Timestamp shift_end_time = null;
        Integer a = business_device_id;
        Integer start_trip_minute = null;
        Integer end_trip_minute = null;
        try {
            connection = dataSource.getConnection();
            String query = "SELECT r.route_id,r.up_down,s.shift_id,s.shift_start_time,s.shift_end_time,s.start_trip_minute,s.end_trip_minute FROM web.vehicle v "
                    + "INNER JOIN web.route_vehicle rv on v.vehicle_id=rv.vehicle_id "
                    + "INNER JOIN web.route r on r.route_id=rv.route_id "
                    + "INNER JOIN web.shift s on s.shift_id=r.shift_id and v.business_device_business_device_id=? and rv.is_deleted=false";//73

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, business_device_id);
            rs = preparedStatement.executeQuery();
            Timestamp compute_date = new Timestamp(position.getFixTime().getTime());
            Timestamp new_shift_start_time = null;
            Timestamp new_shift_end_time = null;
            
            int countertest=0;


            
            while (rs.next()) {
                countertest+=1;
                System.out.println("counter==="+countertest+ "route_id ===="+rs.getInt("route_id"));
                shift_start_time = rs.getTimestamp("shift_start_time");
                shift_end_time = rs.getTimestamp("shift_end_time");
                start_trip_minute = rs.getInt("start_trip_minute");
                end_trip_minute = rs.getInt("end_trip_minute");
//                System.out.println("......      Shift Start Time  .............  " + shift_start_time);
//                System.out.println("......      shift_end_time  .............  " + shift_end_time);
//                System.out.println("......      start_trip_minute .............  " + start_trip_minute);
//                System.out.println("......      end_trip_minute  .............  " + end_trip_minute);

//                if (rs.getInt("up_down") == 1) {
////                      System.out.println("..........   compute_date    ................... " + compute_date);
////                    System.out.println("........        inside up_down          ................  " );
////                    compute_date.setHours(shift_start_time.getHours());
////                    compute_date.setMinutes(shift_start_time.getMinutes());
////
////                    new_shift_start_time = Timestamp.from(new Timestamp(compute_date.getTime()).toInstant().minusSeconds(start_trip_minute * 60));
//////                    System.out.println("..........    NEW Shift Start Time      ................... " + new_shift_start_time);
////                    new_shift_end_time = compute_date;
////                       System.out.println("..........    NEW Shift Start Time      ................... " + new_shift_end_time);
//
//                } else {
////                    compute_date.setHours(shift_end_time.getHours());
////                    compute_date.setMinutes(shift_end_time.getMinutes());
////                    new_shift_start_time = compute_date;
////                    new_shift_end_time = Timestamp.from(new Timestamp(compute_date.getTime()).toInstant().plusSeconds(end_trip_minute * 60));
//                }
//                    System.out.println("..........   Position . getFix time  1 .....  "+ position.getFixTime());
//                    System.out.println("..........   Position . getFix time  2 .....  "+ new_shift_start_time);
//                    System.out.println("....................  Poaition  time 4  "  + position.getFixTime());
//                    System.out.println("...............   endtime       " +new_shift_end_time );
//                    if(position.getFixTime().getTime() >= shift_start_time.getTime()){
//                        System.out.println(" true");
//                    } else {
//                        System.out.println(" false");
//                    }
//
//                    if(position.getFixTime().getTime() <= shift_end_time.getTime()){
//                        System.out.println("...... 2 true");
//                    }else  {
//                        System.out.println("............  2 false");
//                    }
//                    System.out.println(".........     TRip Start Time   ...........     " + shift_start_time);
//                    System.out.println(".........    TRip END Time   ...........     " + shift_end_time);
//                    System.out.println(".........   Current  TRip Start Time   ...........     " + position.getFixTime());

//                System.out.println(".shift_start_time.getTime()..................   "+shift_start_time.getTime());




                SimpleDateFormat simpleformat = new SimpleDateFormat("HH.mm");
                String Start_time = simpleformat.format(shift_start_time);


//                System.out.println("Hour in HH format = "+Start_time);
                SimpleDateFormat UtcTime = new SimpleDateFormat("HH.mm");
//
                String strHourutc = UtcTime.format(position.getFixTime());
//                System.out.println("utc in HH format = "+strHourutc);

                SimpleDateFormat simpleformat1 = new SimpleDateFormat("HH.mm");
                String endtime = simpleformat1.format(shift_end_time);
//                System.out.println("Hour in HH format = "+endtime);

                    System.out.println(".........out if  business_device_id    ..........   "+business_device_id+"===="+"date compute"+ compute_date);

                    
                double strHourstart =Double.parseDouble(Start_time);
                double strHourend =Double.parseDouble(endtime);
                double strHourg =Double.parseDouble(strHourutc);

                    System.out.println("uppper get strHourstart==="+strHourstart+ "strHourend=== "+strHourend+"strHourg======"+strHourg);

//                if ((strHourg <= strHourstart) && (strHourg <= strHourend)) {
//                                if ((shift_start_time.getTime() >= position.getFixTime().getTime()) && (position.getFixTime().getTime() <= shift_end_time.getTime())) {

                    System.out.println("main====before time......... route_id    ..........   "+rs.getInt("route_id"));
                     System.out.println("main===before time......... up_down    ..........   "+rs.getInt("up_down"));
                if ((strHourstart <=  strHourg )&& (strHourend >=  strHourg )) {

                    System.out.println("......... we get the shift time    ..........   "+"strHourstart  "+strHourstart+ "strHourend==="+strHourend+"strHourg======"+strHourg);

                    int route_id = rs.getInt("route_id");

                    int up_down = rs.getInt("up_down");

//                    System.out.println("......... route_id    ..........   "+route_id);
//                    System.out.println("......... up_down    ..........   "+up_down);

                     System.out.println("if........ route_id    ..........   "+rs.getInt("route_id"));
                     System.out.println("if........ up_down    ..........   "+rs.getInt("up_down"));
                    System.out.println("......... business_device_id    ..........   "+business_device_id);

                    checkAndGetForRoute(business_device_id, position, route_id, up_down);
                     System.out.println("back for next");

                } else {
                    System.out.println("else=time==not get shift time");
                    System.out.println("else=time== route_id===" +rs.getInt("route_id"));
                     System.out.println("=else=time===up_down===" +rs.getInt("up_down"));

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.close();
                preparedStatement.close();
                rs.close();
                PrintOut.PrintOutString("Shift Execution process database connection closed now.");
            }
        }
    }

// public void checkAndGetForRoute(int business_device_id, Position position, int route_id, int up_down_key) throws SQLException {
//
//        Connection connection = null;
//        PreparedStatement preparedStatement = null;
//        ResultSet rs = null;
//        try {
//            System.out.println("");
//            System.out.println("............  inside CHECK AND GET FOR ROUTE ...................... ");
//            System.out.println("");
//            connection = dataSource.getConnection();
//            String selectSQL = "SELECT index,route_stoppage_id,st_astext(stoppage_location_point) as gps_location,route_id,stoppage_name FROM web.route_stoppage where route_id=?";
//
//            /*String selectSQL = "SELECT rs.route_stoppage_id,st_astext(rs.stoppage_location_point) as gps_location,rs.route_id,rs.stoppage_name FROM web.vehicle v "
//					+ "INNER JOIN web.route_vehicle rv on v.vehicle_id=rv.vehicle_id "
//					+ "INNER JOIN web.route_stoppage rs on rs.route_id=rv.route_id  "
//					+ "and v.business_device_id=? and rv.is_deleted=false";*/ //old one
//            preparedStatement = connection.prepareStatement(selectSQL);
//            preparedStatement.setInt(1, route_id);
//            rs = preparedStatement.executeQuery();
//            Geometry geometry = null;
//         //   ParentNotification p_notification = null;
//            String in_query = null;
//            String check_query = "select notification_type,up_down_key from web.parent_notification where route_id=? ORDER BY parent_notification_id DESC limit 1";
//            System.out.println("==============================");
//
//            System.out.println("check_query"+check_query);
//            System.out.println("==============================");
//
//            boolean check = false;
//            while (rs.next()) {
//                int index_from_route_stopapge = rs.getInt("index");
//               // geometry = CheckForGeoFence.wktToGeometry(rs.getString("gps_location"));
//                String stp_name = rs.getString("stoppage_name");
//                long route_stoppage_id = rs.getLong("route_stoppage_id");
//
////                System.out.println("lat----------->"+position.getLatitude());
////                 System.out.println("lon----------->"+position.getLongitude());
////                  System.out.println("x----------->"+geometry.getCoordinate().x);
////                   System.out.println("y----------->"+geometry.getCoordinate().y);
//
//                if (CheckForGeoFence.checkForCircle(geometry.getCoordinate().x, geometry.getCoordinate().y, position.getLatitude(), position.getLongitude(), 350)) { //379  //350
////                    System.out.println("");
////                    System.out.println("inside  Geofence ");
////                    System.out.println("");
//                    //p_notification.setRoute_id(rs.getInt("route_id"));
//                    //p_notification.setNotification_type("stoppage_name");
//                    preparedStatement = connection.prepareStatement(check_query);
//                    preparedStatement.setInt(1, rs.getInt("route_id"));
//                    ResultSet rs1 = preparedStatement.executeQuery();
//                    if (rs1.next()) {
//                        if (rs1.getString("notification_type") != null && !rs1.getString("notification_type").equals(stp_name)
//                                && rs1.getInt("up_down_key") == up_down_key) {
//                              System.out.println(".........        inside if      ..............   ");
//                            in_query = "insert into web.parent_notification (route_id, notification_type, is_active,up_down_key)values (?, ?, ?, ?)";
//                            preparedStatement = connection.prepareStatement(in_query, Statement.RETURN_GENERATED_KEYS);
//                            preparedStatement.setInt(1, rs.getInt("route_id"));
//                            preparedStatement.setString(2, rs.getString("stoppage_name"));
//                            preparedStatement.setBoolean(3, true);
//                            preparedStatement.setInt(4, up_down_key);
//                            preparedStatement.executeUpdate();
//                            rs = preparedStatement.getGeneratedKeys();
//                            if (rs.next()) {
//                                String stoppage_name = rs.getString("notification_type");
//                                PushNotification.sendRoutePushNotification(position.getBusiness_device_id(), 77, stoppage_name, route_stoppage_id);
//                            } else {
//
//                            }
//                            check = true;
//                       }
//                    } else {
//                            System.out.println(".........        inside second   else     ..............   ");
//                        in_query = "insert into web.parent_notification (route_id, notification_type, is_active,up_down_key)values (?, ?, ?, ?)";
//                        preparedStatement = connection.prepareStatement(in_query, Statement.RETURN_GENERATED_KEYS);
//                        preparedStatement.setInt(1, rs.getInt("route_id"));
//                        preparedStatement.setString(2, rs.getString("stoppage_name"));
//                        preparedStatement.setBoolean(3, true);
//                        preparedStatement.setInt(4, up_down_key);
//                        preparedStatement.executeUpdate();
//                        rs1 = preparedStatement.getGeneratedKeys();
//                        if (rs1.next()) {
//                            String stoppage_name = rs1.getString("notification_type");
//                            PushNotification.sendRoutePushNotification(position.getBusiness_device_id(), 77, stoppage_name, route_stoppage_id);
//                        } else {
//
////                            PrintOut.PrintOutString("===================stoppage======record not inserted dude==============");
//                            System.out.println("===================stoppage======record not inserted dude==============");
//                        }
//                        check = true;
//                   }
//                } else {
//
//                    PrintOut.PrintOutString("It's not an comes near route=======================*-*-*-*-*-*-*--*");
//                }
//                if (check) {
//                    break;
//                }
//
//            }
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        } finally {
//            if (connection != null) {
//                connection.close();
//                preparedStatement.close();
//                rs.close();
////
//            }
//        }
//    }
public void checkAndGetForRoute(int business_device_id, Position position, int route_id, int up_down_key) throws SQLException {

    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet rs = null;
    try {
        System.out.println("checkAndGetForRoute=======>");
        System.out.println("............  inside CHECK AND GET FOR ROUTE ...................... ");
        System.out.println("");
        connection = dataSource.getConnection();
        String selectSQL = "SELECT index,route_stoppage_id,st_astext(stoppage_location_point) as gps_location,route_id,stoppage_name FROM web.route_stoppage where route_id=?";
        preparedStatement = connection.prepareStatement(selectSQL);

        preparedStatement.setInt(1, route_id);
        rs = preparedStatement.executeQuery();
        Geometry geometry = null;
        String in_query = null;
        String check_query = "select notification_type,up_down_key,servertime from web.parent_notification where route_id=? ORDER BY parent_notification_id DESC limit 1";
        while (rs.next()) {
            int index_from_route_stopapge = rs.getInt("index");
            geometry = CheckForGeoFence.wktToGeometry(rs.getString("gps_location"));
            String stp_name = rs.getString("stoppage_name");
            long route_stoppage_id = rs.getLong("route_stoppage_id");
                    System.out.println("......... stp_name    ..........   "+stp_name);
                    System.out.println("......... route_stoppage_id    ..........   "+route_stoppage_id);
                    System.out.println("......... index_from_route_stopapge    ..........   "+index_from_route_stopapge);
                    System.out.println("......... up_down_key    ..........   "+up_down_key);


            if (CheckForGeoFence.checkForCircle(geometry.getCoordinate().x, geometry.getCoordinate().y, position.getLatitude(), position.getLongitude(), 350)) { //379  //350
                    System.out.println("inside  Geofence ");
                   PushNotification.sendRoutePushNotification(position.getBusiness_device_id(), 77, stp_name, route_stoppage_id);

//                preparedStatement = connection.prepareStatement(check_query);
//                preparedStatement.setInt(1, rs.getInt("route_id"));
//                ResultSet rs1 = preparedStatement.executeQuery();
//                if (rs1.next()) {
//                            System.out.println(".........        inside second    rs1    ..............   ");
//                    if (rs1.getString("notification_type") != null && !rs1.getString("notification_type").equals(stp_name)
//                            && rs1.getInt("up_down_key") == up_down_key) {
//                              System.out.println(".........        inside second  rs1 else      ..............   ");
//                        in_query = "insert into web.parent_notification (route_id, notification_type, is_active,up_down_key,servertime)values (?, ?, ?, ?, ?)";
//                        preparedStatement = connection.prepareStatement(in_query, Statement.RETURN_GENERATED_KEYS);
//                        preparedStatement.setInt(1, rs.getInt("route_id"));
//                        preparedStatement.setString(2, rs.getString("stoppage_name"));
//                        preparedStatement.setBoolean(3, true);
//                        preparedStatement.setInt(4, up_down_key);
//                        preparedStatement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
//                        preparedStatement.executeUpdate();
//                        rs = preparedStatement.getGeneratedKeys();
//                        if (rs.next()) {
//                            String stoppage_name = rs.getString("notification_type");
//
//                            System.out.println("sending ---- 1 called");
//                            PushNotification.sendRoutePushNotification(position.getBusiness_device_id(), 77, stoppage_name, route_stoppage_id);
//                        } else {
//
//                        }
//                    }
//                } else {
//                    System.out.println(".........        inside second   else     ..............   ");
//                    in_query = "insert into web.parent_notification (route_id, notification_type, is_active,up_down_key,servertime)values (?, ?, ?, ?, ?)";
//                    preparedStatement = connection.prepareStatement(in_query, Statement.RETURN_GENERATED_KEYS);
//                    preparedStatement.setInt(1, rs.getInt("route_id"));
//                    preparedStatement.setString(2, rs.getString("stoppage_name"));
//                    preparedStatement.setBoolean(3, true);
//                    preparedStatement.setInt(4, up_down_key);
//                    preparedStatement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
//
//                    preparedStatement.executeUpdate();
//                    rs1 = preparedStatement.getGeneratedKeys();
//                    if (rs1.next()) {
//                        String stoppage_name = rs1.getString("notification_type");
//                        System.out.println("sending  2----");
//
//                        PushNotification.sendRoutePushNotification(position.getBusiness_device_id(), 77, stoppage_name, route_stoppage_id);
//                    } else {
//
//                        System.out.println("===================stoppage======record not inserted dude==============");
//                    }
//                }
//            }\
            } else {
                            System.out.println(".........It's not an comes near route   ..............   ");

                PrintOut.PrintOutString("It's not an comes near route=======================*-*-*-*-*-*-*--*");
            }


        }
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    } finally {
        if (connection != null) {
            connection.close();
            preparedStatement.close();
            rs.close();
//
        }
    }
}

    
    

//public void addPosition(Position position) throws SQLException {
//
//		int ignition=0;
//		int trip=0;
//		int idle=0;
//		int ac =0;
//		int fuel = 0;
//                int temp = 0;
//		long stopage_time=0;
//		String vin_number = "0000";
//                String addr=null;
//             int device_id = (int) position.getDeviceId();
////             if(device_id == 939){
////                System.out.println("enter add potion data--------------->");
////             }
//           	System.out.println("device_id add position function changess"+device_id);
//        if (position.getLatitude() != 0 && position.getLongitude() != 0) {
////                position.setAddress("N/A");
//            System.out.println("testttttttttttttttttttttttttttttttttttt");
//            if (position.getAddress() == null) {
//                try {
//                    System.out.println("TRUIRUIUTIRUTIUI");
//
//                     addr = NominatimCall.sendGet(position.getLatitude(), position.getLongitude());
////                    addr = Context.getGeocoder().getAddress(position.getLatitude(), position.getLongitude(), null);
//                    System.out.println("testttttttttttttttttttttttttttttttttttt"+addr);
//
//                    if (addr != null ) {
//                        position.setAddress(addr);
//                         System.out.println("setAddress"+addr);
//                    } else {
//                        position.setAddress("N/A");
//                    }
//
//                } catch (Exception e) {
//                    // TODO Auto-generated catch block
//
//                    e.printStackTrace();
//                }
//            }
//            Connection connection = null;
//            java.sql.PreparedStatement preparedStatement = null;
//            ResultSet rs = null;
//            try {
//
//                //Class.forName("org.postgresql.Driver");
//                //connection = DriverManager.getConnection("jdbc:postgresql://192.168.1.34:5432/trackio","track", "trackio");
//                connection = dataSource.getConnection();
//
//   String selectSQL = "SELECT fuel,idle,ignition,trip,gps_time,ac,temperature from web.live_ob WHERE business_device_id=? order by live_ob_id desc LIMIT 1;";
//                System.out.println("selectSQL"+selectSQL);
//                preparedStatement = connection.prepareStatement(selectSQL);
////                PrintOut.PrintOutString("add position deviceId--------->"+position.getDeviceId());
//                preparedStatement.setInt(1,(int) position.getBusiness_device_id());
//System.out.println(" position.getBusiness_device_id()" + position.getBusiness_device_id());
//
//                // execute select SQL stetement
//                rs = preparedStatement.executeQuery();
//
////                    if(device_id == 939){
////                         System.out.println("vishal3");
////                System.out.println("result set statement deviceId--------->");
////             }
//
//
//
//
//                if (rs.next()) {
//                    ignition = rs.getInt("ignition");
//                    trip = rs.getInt("trip");
//                    idle = rs.getInt("idle");
//                    ac = rs.getInt("ac");
//                    //                 vin_number = rs.getString("vin_number");
//
////                                        if(!position.getVin_number().equalsIgnoreCase("0000") && !vin_number.equalsIgnoreCase(position.getVin_number())){
////                                            vin_number = position.getVin_number();
////                                        }  else {
////                                            vin_number = vin_number;
////                                        }
//                    if (position.getIgnition() != 0 && ignition != position.getIgnition()) {
//                        ignition = position.getIgnition();
//                        /*Thread t = new Thread(new Runnable() {
//							public void run()
//							{
//								PushNotification.callPushNotification(position.getBusiness_device_id(),position.getIgnition());
//
//							}
//						});
//						t.start();*/
//                    } else if (ignition == 1) {
//                        ignition = 1;
//                    } else if (ignition == 2) {
//                        ignition = 2;
//                    } else {
//                        ignition = position.getIgnition();
//                    }
//                    if (position.getTrip() != 0 && trip != position.getTrip()) {
//                        trip = position.getTrip();
//                        if (position.getTrip() == 1) {
//                            Timestamp gps_time = rs.getTimestamp("gps_time");
//                            PreparedStatement preparedStatement1 = null;
//                            ResultSet rs1 = null;
//                            String query = "SELECT * FROM ob.observation WHERE business_device_id=? AND trip=1 ORDER BY gps_time desc LIMIT 1";
//                            System.out.println("query"+query);
//  preparedStatement1 = connection.prepareStatement(query);
//                            preparedStatement1.setInt(1, (int) position.getBusiness_device_id());
//                            rs1 = preparedStatement1.executeQuery();
//                            Timestamp gps_time1 = null;
//                            if (rs1.next()) {
//                                gps_time1 = rs1.getTimestamp("gps_time");
//                            }
//                            if (gps_time1 != null) {
////
//                                stopage_time = ((gps_time.getTime() - gps_time1.getTime()) / 1000);
//
//                            }
//                        }
//                    } else if (trip == 1) {
//                        trip = 1;
//                    } else if (trip == 2) {
//                        trip = 2;
//                    } else {
//                        trip = position.getTrip();
//                    }
//
//
////            if(device_id == 939){
////                 System.out.println("vishal2");
////                System.out.println("data manager file-------decode");
////             }
//            if (position.getIdle() != 0 && idle != position.getIdle()) {
//                        idle = position.getIdle();
//                    } else if (idle == 1) {
//                        idle = 1;
//                    } else if (idle == 2) {
//                        idle = 2;
//                    } else {
//                        idle = position.getIdle();
//                    }
//
//                    if (position.getAc() != 0 && ac != position.getAc()) {
//                        ac = position.getAc();
//                    } else if (ac == 1) {
//                        ac = 1;
//                    } else if (ac == 2) {
//                        ac = 2;
//                    } else {
//                        ac = position.getAc();
//                    }
//
//                    if (position.getFuel() == 0) {
//                        fuel = rs.getInt("fuel");
//                    } else {
//                        fuel = position.getFuel();
//                    }
//
//                    if(position.getTemperature() == 0)
//                        {
//                         temp = rs.getInt("temperature");
//                         }else{
//                          temp = position.getTemperature();
//                            }
//
//                } else {
//                    trip = 1;
//                }
//
//            } catch (SQLException e) {
//
//                System.out.println("inside catch ....... querry execution");
//
////				System.out.println(e.getMessage());
//            } finally {
//                if (connection != null) {
//                    connection.close();
//                    preparedStatement.close();
//                    rs.close();
//                }
//            }
//
//            if (position.getFixTime() == null) {
//                position.setFixTime(new Date());
//            }
//
//            position.setIgnition(ignition);
//            position.setTrip(trip);
//            position.setIdle(idle);
//            position.setAc(ac);
//            position.setFuel(fuel);
//            position.setTemperature(temp);
//            position.setStopage_time(stopage_time);
//             position.setUuid(java.util.UUID.randomUUID().toString());
//                          System.out.println("getUuid------------------->"+ position.getUuid());
//            //position.setVin_number(vin_number);
//            position.setUuid(QueryBuilder.create(dataSource, getQuery("database.insertPosition"), true)
//                    .setDate("now", new Date())
//                    .setObject(position)
//                    .setDate("time", position.getFixTime()) // tmp
//                    .setLong("device_id", position.getDeviceId())
//                    .setLong("power", 0) // tmp
//                    .setString("extended_info", MiscFormatter.toXmlString(position.getAttributes())) // tmp
//                    .setString("other", MiscFormatter.toXmlString(position.getAttributes())) // tmp
//                   .executeUpdateAndReturn());
//            System.out.println("position.getFixTime()"+position.getFixTime());
//            JsonObject m = MiscFormatter.toJson(position.getAttributes());
//        } else {
//            LOGGER.info("Gps is to low, thats y record was not insterting into database.");
//
//        }
//
//    }
    DataManager dataManager;
public void addPosition(Position position) throws SQLException {

    int ignition=0;
    int trip=0;
    int idle=0;
    int ac =0;
    int fuel = 0;
    int temp = 0;
    long stopage_time=0;
    String vin_number = "0000";
    String addr=null;
    int device_id = (int) position.getDeviceId();
    System.out.println("enter add potion data--------------->"+device_id);

//             if(device_id == 939){
//                System.out.println("enter add potion data--------------->");
//             }



   if (position.getLatitude() != 0 && position.getLongitude() != 0) {
     //           position.setAddress("N/A");
       System.out.println("addrin uper if--------------->"+position.getAddress());


       if (position.getAddress() == null) {
            try {
                System.out.println("addr getAddress--------------->");
//                    addr = "----";
//                    position.setAddress(addr);

                addr = NominatimCall.sendGet(position.getLatitude(), position.getLongitude());

                if (addr != null ) {
                    position.setAddress(addr);
                } else {
                    position.setAddress("N/A");
                }

            } catch (Exception e) {
                System.out.println("catch--------------->");
                // TODO Auto-generated catch block
//                    addr = "----";
//                    position.setAddress(addr);

                e.printStackTrace();
            }
        }
        Connection connection = null;
        java.sql.PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {

            //Class.forName("org.postgresql.Driver");
            //connection = DriverManager.getConnection("jdbc:postgresql://192.168.1.34:5432/trackio","track", "trackio");
            connection = dataSource.getConnection();

            String selectSQL = "SELECT fuel,idle,ignition,trip,gps_time,ac,temperature from web.live_ob WHERE business_device_id=? order by live_ob_id desc LIMIT 1;";
            preparedStatement = connection.prepareStatement(selectSQL);
//                PrintOut.PrintOutString("add position deviceId--------->"+position.getDeviceId());
            preparedStatement.setInt(1,(int) position.getBusiness_device_id());
            System.out.println("device_id"+position.getBusiness_device_id());
            // execute select SQL stetement
            rs = preparedStatement.executeQuery();

//                    if(device_id == 939){
//                         System.out.println("vishal3");
//                System.out.println("result set statement deviceId--------->");
//             }

//            if (rs.next()){
//                Geometry  geometry = CheckForGeoFence.wktToGeometry(rs.getString("gps_location"));
//
//                if (CheckForGeoFence.checkForCircle(geometry.getCoordinate().x, geometry.getCoordinate().y, position.getLatitude(), position.getLongitude(), 350)) { //379  //350
//
//                }
//            }

            if (rs.next()) {
                ignition = rs.getInt("ignition");
                System.out.println("ignition addPOistion"+ignition);
                trip = rs.getInt("trip");
                idle = rs.getInt("idle");
                ac = rs.getInt("ac");
                //                 vin_number = rs.getString("vin_number");

//                                        if(!position.getVin_number().equalsIgnoreCase("0000") && !vin_number.equalsIgnoreCase(position.getVin_number())){
//                                            vin_number = position.getVin_number();
//                                        }  else {
//                                            vin_number = vin_number;
//                                        }
                if (position.getIgnition() != 0 && ignition != position.getIgnition()) {


                    ignition = position.getIgnition();
                        /*Thread t = new Thread(new Runnable() {
							public void run()
							{
								PushNotification.callPushNotification(position.getBusiness_device_id(),position.getIgnition());

							}
						});
						t.start();*/
                } else if (ignition == 1) {
                    ignition = 1;
                } else if (ignition == 2) {
                    ignition = 2;
                } else {
                    ignition = position.getIgnition();
                }
                if (position.getTrip() != 0 && trip != position.getTrip()) {
                    trip = position.getTrip();
                    if (position.getTrip() == 1) {
                        Timestamp gps_time = rs.getTimestamp("gps_time");
                        PreparedStatement preparedStatement1 = null;
                        ResultSet rs1 = null;
                        String query = "SELECT * FROM ob.observation WHERE business_device_id=? AND trip=1 ORDER BY gps_time desc LIMIT 1";
                        preparedStatement1 = connection.prepareStatement(query);
                        preparedStatement1.setInt(1, (int) position.getBusiness_device_id());
                        rs1 = preparedStatement1.executeQuery();
                        Timestamp gps_time1 = null;
                        if (rs1.next()) {
                            gps_time1 = rs1.getTimestamp("gps_time");
                        }
                        if (gps_time1 != null) {
//
                            stopage_time = ((gps_time.getTime() - gps_time1.getTime()) / 1000);

                        }
                    }
                } else if (trip == 1) {
                    trip = 1;
                } else if (trip == 2) {
                    trip = 2;
                } else {
                    trip = position.getTrip();
                }


//            if(device_id == 939){
//                 System.out.println("vishal2");
//                System.out.println("data manager file-------decode");
//             }
                if (position.getIdle() != 0 && idle != position.getIdle()) {
                    idle = position.getIdle();
                } else if (idle == 1) {
                    idle = 1;
                } else if (idle == 2) {
                    idle = 2;
                } else {
                    idle = position.getIdle();
                }

                if (position.getAc() != 0 && ac != position.getAc()) {
                    ac = position.getAc();
                } else if (ac == 1) {
                    ac = 1;
                } else if (ac == 2) {
                    ac = 2;
                } else {
                    ac = position.getAc();
                }

                if (position.getFuel() == 0) {
                    fuel = rs.getInt("fuel");
                } else {
                    fuel = position.getFuel();
                }

                if(position.getTemperature() == 0)
                {
                    temp = rs.getInt("temperature");
                }else{
                    temp = position.getTemperature();
                }

            } else {
                trip = 1;
            }

        } catch (SQLException e) {

//                System.out.println("inside catch ....... querry execution");

//				System.out.println(e.getMessage());
        } finally {
            if (connection != null) {
                connection.close();
                preparedStatement.close();
                rs.close();
            }
        }

        if (position.getFixTime() == null) {
            position.setFixTime(new Date());
        }

        position.setIgnition(ignition);
        position.setTrip(trip);
        position.setIdle(idle);
        position.setAc(ac);
        position.setFuel(fuel);
        position.setTemperature(temp);
        position.setStopage_time(stopage_time);
        position.setUuid(java.util.UUID.randomUUID().toString());
        //position.setVin_number(vin_number);
        position.setUuid(QueryBuilder.create(dataSource, getQuery("database.insertPosition"), true)
                .setDate("now", new Date())
                .setObject(position)
                .setDate("time", position.getFixTime()) // tmp
                .setLong("device_id", position.getDeviceId())
                .setLong("power", 0) // tmp
                .setString("extended_info", MiscFormatter.toXmlString(position.getAttributes())) // tmp
                .setString("other", MiscFormatter.toXmlString(position.getAttributes())) // tmp
                .executeUpdateAndReturn());

        JsonObject m = MiscFormatter.toJson(position.getAttributes());
    } else {
       LOGGER.info("Gps is to low, thats y record was not insterting into database.");

    }

}


    public int checkAuthorizationForP2p(String value, Integer p2p_user_id) throws SQLException {

        int response = 1;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            String selectSQL = "SELECT * from web.token where token=? AND p2p_user_id_p2p_user_id=? ORDER BY token_id DESC LIMIT 1";
            preparedStatement = connection.prepareStatement(selectSQL);
            preparedStatement.setString(1, value);
            preparedStatement.setInt(2, p2p_user_id);
//            System.out.println("value user _id----------->"+value);
//            System.out.println("p2p user _id----------->"+p2p_user_id);
            rs = preparedStatement.executeQuery();
            if (!rs.next()) {
                response = 0;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            if (connection != null) {
                connection.close();
                preparedStatement.close();
                rs.close();
//				System.out.println("connection was closed now.");
            }
        }
        return response;
    }

    public void addCommandHistory(CommandHistory le) throws SQLException {
        le.setId(QueryBuilder.create(dataSource, getQuery("database.addCommandHistory"), true)
                .setObject(le)
                .setDate("created_time", new Date())
                .executeUpdate());

    }
}
