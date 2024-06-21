package org.traccar.common;

public class AlertType {

	public static final Integer IGNITION_ON=1;
	public static final Integer IGNITION_OFF=2;
	public static final Integer MOVING=3;
	public static final Integer GEOFENCE_ENTER=4;
	public static final Integer GEOFENCE_EXIT=5;
	public static final Integer OVER_SPEPED=6;
	public static final Integer HARD_BREAK=7;
	public static final Integer HARD_ACCELERATION=8;
	public static final Integer ENGINE_IDLE=9;
	public static final Integer PARKING=11;
	public static final Integer AC_ON=12;
	public static final Integer AC_OFF=13;

	public static final Integer POWER_CUTT=15;
	public static final Integer SOS=16;
        
        //Ramp
        public static final Integer RAMP_ON=32;
	public static final Integer RAMP_OFF=33;
	
        
	
	// for personal ev07 device
	public static final Integer FALLDOWN=17;
	public static final int LOWBATTERY = 18;
	public static final int POWER_ON = 19;
	
        
        // new obd device alert 
        
        public static final Integer HARD_CORNERING = 20 ;       // sharp turn
        public static final Integer Dangerous_Driving = 21 ;    // dangerousDriving
        public static final Integer Crash = 22 ;                // crash alert 
        public static final Integer DOOR_OPEN = 23  ;           //door_open
        public static final Integer DOOR_CLOSE = 24  ;          //Door_close          
        public static final Integer BONNET_OPEN = 25  ;         //BONNET_OPEN
        public static final Integer BONNET_CLOSE = 26  ;        //BONNET_CLOSE    
        public static final Integer DISPLACEMENT = 27   ;       //displacement for ttassets
        public static final Integer ILLEGAL_DISMANTLE = 28;     //illegal_dismantle for ttasstes
        public static final Integer VIBRATION = 29 ;            // 29 vibration 
        
        // for TripBooking geofence Enter and out  add by krishna 23/05/2019
        public static final Integer Trip_Geofence_Enter = 30 ;
        public static final Integer Trip_Geofence_Exit = 31 ;
        public static final Integer TOWING = 38;
        
        // for Event Type
        
        public static final String RAMPON = "Ramp On";
        public static final String RAMPOFF = "Ramp Off";
         public static final String ACON = "Ac On";
        public static final String ACOff = "Ac Off";
        public static final String DOOROPEN = "Door Open";
        public static final String DOORCLOSE = "Door Close";
        public static final String BONNETOPEN = "Bonnet Open";
        public static final String BONNETCLOSE = "Bonnet Close";
        public static final String IGNITIONON = "Ignition on";
        public static final String IGNITIONOFF = "Igniton Off";
         
     
}
