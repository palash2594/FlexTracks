import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConflictResolver2 {

	static Map<Integer, CompleteFlightInfo> unscheduled = new HashMap<Integer, CompleteFlightInfo>(); // store the unresolved flights schedule
	static Map<Integer, CompleteFlightInfo> scheduled = new HashMap<Integer, CompleteFlightInfo>();  // store the resolved schedule
	public static int runwayId[]=new int[4];	
	public static String arrCat[]=new String[4];	// store last arrival category on the runway i
	public static java.sql.Time arrTime[]=new java.sql.Time[4];	// store last arrival time on the runway i
	public static String depCat[]=new String[4];	// store last departure category on the runway i
	public static java.sql.Time depTime[]=new java.sql.Time[4];	// store last departure time on the runway i
	static Integer schCounter = 0;	// counter for schedule hashmap
	static long currentTime = 0;	// stores the current time, it increments by 60 after every scheduled flights
	static java.sql.Time startTime;	// start time of the current schedule
	static List<Long> allotedTimeInSec = new ArrayList<Long>();	// stores the time in sec after a flight has been scheduled
	public static List<java.sql.Time> allotedTime = new ArrayList<java.sql.Time>();
	public static int noOfFlights;
	static String url = "jdbc:mysql://localhost:3306/";
	static String user = "root";
	static String password = "";
	
	public static void main(String[] args){
		
	}
	
	public static Map<Integer, CompleteFlightInfo> resolver(CompleteFlightInfo[] cfi, int n){
		// filling the unscheduled hashmap
		scheduled.clear();
		unscheduled.clear();
		allotedTimeInSec.clear();
		allotedTime.clear();
		noOfFlights = n;
		currentTime = 0;
		for(int i = 0; i < noOfFlights; i++){
			prepareMap(i, cfi[i]);
		}
		
		int scopeEnd = 3;	// indicates the range of flights to be considered at particular time(only for arrivals)
		int timeElapsed = 0; // increment scopeEnd when timeElapsed = 60
		int counter = 0; // flight in focus
		boolean isSchedule = false;	// check whether flight in focus is scheduled or not
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(url, user, password);
			Statement stt = con.createStatement();
			stt.execute("USE flextracks");
			
			ResultSet res1 = stt.executeQuery("SELECT * FROM `flightlogtable` ORDER BY id DESC LIMIT 1");
			while(res1.next()){
				java.sql.Time lastTime = res1.getTime("actualTime");
				long t = lastTime.getTime();
				startTime = new java.sql.Time(t+ 60000);
				//System.out.println(startTime);
			}
			
			int i = 0;
			ResultSet res = stt.executeQuery("SELECT * FROM runwaylastflights");
			
			while(res.next()){
				runwayId[i] = res.getInt("runwayId");
				arrTime[i] = res.getTime("arrTime");
				arrCat[i] = res.getString("arrCat");
				depTime[i] = res.getTime("depTime");
				depCat[i] = res.getString("depCat");
				i++;
			}
			while(!unscheduled.isEmpty()){
				//System.out.println("hey");
				
				isSchedule=false;
				//System.out.println("2");
				while(isSchedule != true){
					
					if(counter <= scopeEnd || unscheduled.get(counter).arrivalDeparture.equals("D")){
						boolean moveToSchedule = checkFlight(unscheduled.get(counter));
						if(moveToSchedule){
							updateLastEntryTable(counter);
							scheduled.put(schCounter++, unscheduled.get(counter));
							unscheduled.remove(counter);
							isSchedule = true;
							allotedTimeInSec.add(currentTime);
							currentTime += 60;
							scopeEnd++;
							timeElapsed = 0;
							//System.out.println("1");
							counter = getNextCounter3(counter); // fdytruy
							break;
						}
						else if(counter == 9){
							currentTime += 60;
							counter = getNextCounter3(counter);
							scopeEnd++;
							continue;
						}
						else if(getNextCounter(counter) != -1){
							counter = getNextCounter(counter);
						}
					}
					else{
						if(getNextCounter(counter) == -1){
							counter = getNextCounter3(counter);
							currentTime += 60;
						}
						else{
							counter = getNextCounter2(counter);
						}
					}
				}
			}
			
			
			findTime(); // to convert sec into time format (java.sql.Time)
			//System.out.println(allotedTimeInSec);
			
			stt.execute("UPDATE`timereqforlastslot` SET `time` = '"+allotedTimeInSec.get(allotedTimeInSec.size() - 1)+"' WHERE `id`='1'");
			//System.out.println(allotedTimeInSec.get(allotedTimeInSec.size() - 1));
			
			fillFLightLogTable();
			//DisplaySchedule.display(scheduled);
			//System.out.println("c2");
			schCounter = 0;
			return scheduled;
			
	      }catch(Exception e){
	    	  
	      }
		return scheduled;   
	}
	
	public static void fillFLightLogTable() {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		Date date = new Date();
		String d = dateFormat.format(date);
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(url, user, password);
			Statement stt = con.createStatement();
			stt.execute("USE flextracks");
			
			for(int i = 0; i < noOfFlights; i++){
				stt.execute("INSERT INTO `flightlogtable`(`flightNo`, `aircraftName`, `arrivalDeparture`, `sourceDestination`, `airline`, `terminalName`, `date`,"
						+ " `actualTime`)"
						+ " VALUES ('"+scheduled.get(i).flightNo+"', '"+scheduled.get(i).aircraftName+"', '"+scheduled.get(i).arrivalDeparture+"',"
						+ " '"+scheduled.get(i).sourceDestination+"', '"+scheduled.get(i).airline+"', '"+scheduled.get(i).terminalName+"', '"+d+"',"
						+ "'"+allotedTime.get(i)+"')");
			}
			
		}catch(Exception e){
			
		}
	}

	public static void findTime(){
		for(int i = 0; i < noOfFlights; i++){
			allotedTime.add(new java.sql.Time(startTime.getTime() + allotedTimeInSec.get(i) * 1000));
		}
	}
	
	public static void prepareMap(Integer place, CompleteFlightInfo cfi){
			unscheduled.put(place, cfi);
	}
	
	public static int getNextCounter(int c){
		for(int i = 0; i < noOfFlights; i++){
			if(unscheduled.containsKey(i) && i > c){
				return i;
			}
		}
		return -1;
	}
	
	public static int getNextCounter2(int counter){
		for(int i = 0; i < noOfFlights; i++){
			if(unscheduled.containsKey(i) && i > counter){
				return i;
			}
		}
		
		for(int i = 0; i < noOfFlights; i++){
			if(unscheduled.containsKey(i)){
				return i;
			}
		}
		return -1;
	}
	
	public static int getNextCounter3(int counter){
		for(int i = 0; i < noOfFlights; i++){
			if(unscheduled.containsKey(i)){
				return i;
			}
		}
		return -1;
	}
	
	public static boolean checkFlight(CompleteFlightInfo cfi){
		
		java.sql.Time preTime;
		
		if(cfi.arrivalDeparture.equals("A")){
			preTime = arrTime[cfi.runway - 1];
		}else{
			preTime = depTime[cfi.runway - 1];
		}
		
		double t1 = preTime.getTime();
		double t2 = startTime.getTime() + currentTime * 1000;
		
		double timeDiff = (t2-  t1) / 1000;	
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(url, user, password);
			Statement stt = con.createStatement();
			stt.execute("USE flextracks");
			ResultSet res;
			int timegap = 0;
			if(cfi.arrivalDeparture.equals("A")){
			res = stt.executeQuery("SELECT * FROM separationdistance where preceeding = '"+arrCat[cfi.runway - 1]+"' AND "
    			+ "succeeding = '"+cfi.category+"' AND arrDep = '"+cfi.arrivalDeparture+"'");
			}else{
			res = stt.executeQuery("SELECT * FROM separationdistance where preceeding = '"+depCat[cfi.runway - 1]+"' AND "
		    			+ "succeeding = '"+cfi.category+"' AND arrDep = '"+cfi.arrivalDeparture+"'");
			}
			while(res.next()){
    		  timegap = res.getInt("timegap");
			}
			
			if(timegap <= timeDiff){
				return true;
			}
		}catch(Exception e){
			
		}
		return false;
	}
	
	public static void updateLastEntryTable(int counter){
		
		double time = startTime.getTime();
		time = time + currentTime * 1000;
		java.sql.Time newTime = new java.sql.Time((long) time);
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(url, user, password);
			Statement stt = con.createStatement();
			stt.execute("USE flextracks");
			
			if(unscheduled.get(counter).arrivalDeparture.equals("A")){
				
				arrCat[unscheduled.get(counter).runway - 1] = unscheduled.get(counter).category;
				arrTime[unscheduled.get(counter).runway - 1] = newTime;
				
				stt.execute("UPDATE `runwaylastflights` SET `arrTime`= '"+ newTime+"',`arrCat`= '"+unscheduled.get(counter).category+"' "
						+ "WHERE `runwayId`='"+(unscheduled.get(counter).runway) +"'");
				
			}else{
				
				depCat[unscheduled.get(counter).runway - 1] = unscheduled.get(counter).category;
				depTime[unscheduled.get(counter).runway - 1] = newTime;
				
				stt.execute("UPDATE `runwaylastflights` SET `depTIme`= '"+ newTime+"',`depCat`= '"+unscheduled.get(counter).category+"' "
						+ "WHERE `runwayId`='"+(unscheduled.get(counter).runway)+"'");
				
			}
			
			}catch(Exception e){
				
			}
	}
}