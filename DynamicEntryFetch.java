import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DynamicEntryFetch{
	
	//public static String[] flightNo = new String[10];
	public static List<String> flightNo = new ArrayList<String>();
	//public static String[] airline = new String[10];
	public static List<String> airline = new ArrayList<String>();
	//public static String[] arrivalDeparture = new String[10];
	public static List<String> arrivalDeparture = new ArrayList<String>();
	//public static String[] sourceDestination = new String[10];
	public static List<String> sourceDestination = new ArrayList<String>();
	//public static String[] aircraftName = new String[10];
	public static List<String> aircraftName = new ArrayList<String>();
	//public static String[] terminalName = new String[10];
	public static List<String> terminalName = new ArrayList<String>();
	//public static String[] timeStamp = new String[10];
	public static List<String> timeStamp = new ArrayList<String>();
	//public static String[] time = new String[10];
	public static List<String> time = new ArrayList<String>();
	//public static double sourceDestinationDegree[] = new double[10];
	public static List<Double> sourceDestinationDegree = new ArrayList<Double>();
	//public static String[] cat = new String[10];
	public static List<String> cat = new ArrayList<String>();
	//public static double[][] latlon = new double[10][2]; // latitude longitude
	public static List<Double> lat = new ArrayList<Double>();
	public static List<Double> lon = new ArrayList<Double>();
	public static double mumlat = 19.088686;
	public static double mumlon = 72.867919;
	public static java.sql.Time startTime;
	public static java.sql.Time endTime;
	public static int noOfFlights;
	
	public static double bearing(double lat1, double lon1, double lat2, double lon2){
		double x = Math.cos(lat2 * 3.1412 / 180) * Math.sin(lon2 * 3.1412 / 180 - lon1 * 3.1412 / 180);
		double y = Math.cos(lat1 * 3.1412 / 180) * Math.sin(lat2 * 3.1412 / 180) - 
				Math.sin(lat1 * 3.1412 / 180) * Math.cos(lat2 * 3.1412 / 180) *
				Math.cos(lon2 * 3.1412 / 180- lon1 * 3.1412 / 180);
		
		double beta = Math.atan2(x, y);
		return beta * 180 / 3.1412;
	}
	
	public static void runCode(){
		Date dNow = new Date( );
	      SimpleDateFormat ft =  new SimpleDateFormat ("hh:mm:ss");

	      //System.out.println("Current Date: " + ft.format(dNow));
	      
	    CompleteFlightInfo[] cfi = new CompleteFlightInfo[10];  
	      
	  	String url = "jdbc:mysql://localhost:3306/";
		String user = "root";
		String password = "";
		
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
					t = startTime.getTime();
					endTime = new java.sql.Time(t + 15 * 60000);
				}
				
				
				int i = 0;
				noOfFlights = 0;
				
				ResultSet res = stt.executeQuery("SELECT * FROM flightinfoatc WHERE `time` <= '"+endTime+"' LIMIT 10");
				while(res.next()){
					
					flightNo.add(res.getString("flightNo"));
					airline.add(res.getString("airline"));
					arrivalDeparture.add(res.getString("arrivalDeparture"));
					sourceDestination.add(res.getString("sourceDestination"));
					aircraftName.add(res.getString("aircraftName"));
					terminalName.add(res.getString("terminalName"));
					timeStamp.add(res.getString("timeStamp"));
					time.add(res.getString("time"));
					noOfFlights++;
					i++;
				}
				
				/*for(int j = 0; j <10; j++){
					res = stt.executeQuery("SELECT * FROM flightinfoatc INNER JOIN aircraft ON flightinfoatc.aircraftName = aircraft.aircraftName");
					while(res.next()){
						cat[j] = res.getString("cat");
					}
					System.out.println(cat[j]);
				}*/
				
				stt.execute("TRUNCATE TABLE  flightcurrenttable");
				
				for(int j = 0; j < noOfFlights; j++){
				res = stt.executeQuery("SELECT * FROM aircraft where aircraftName = '"+ aircraftName.get(j) +"'"); // '"+ +"' write variable in the blank
				while(res.next()){
					cat.add(res.getString("cat"));
				}
				
				res = stt.executeQuery("SELECT * FROM airportsinfo where iataCode = '"+ sourceDestination.get(j) +"'");
				while(res.next()){
					lat.add(res.getDouble("lattitude"));
					lon.add(res.getDouble("longitude"));
				}
				}
				
				for(i = 0; i < noOfFlights; i++){			
						sourceDestinationDegree.add(bearing( mumlat, mumlon, lat.get(i), lon.get(i)) + 180);
							
					//System.out.println(sourceDestinationDegree.get(i));
				}
				
				//Fill up flgihtCureentTable
				for(i = 0; i < noOfFlights; i++)
				{
					stt.execute("INSERT INTO `flightcurrenttable`(`flightNo`, `airline`, `cat`, `arrivalDeparture`, `sourceDestinationDegree`, `terminalName`, "
							+ "`timeStamp`, `aircraftName`) VALUES ('"+flightNo.get(i)+"','"+airline.get(i)+"','"+cat.get(i)+"','"+arrivalDeparture.get(i)+"',"
							+ "'"+sourceDestinationDegree.get(i)+"','"+terminalName.get(i)+"','"+timeStamp.get(i)+"','"+aircraftName.get(i)+"')");
				}
				flightNo.clear();
				aircraftName.clear();
				cat.clear();
				sourceDestination.clear();
				terminalName.clear();
				timeStamp.clear();
				aircraftName.clear();
				arrivalDeparture.clear();
				time.clear();
				sourceDestinationDegree.clear();
				lat.clear();
				lon.clear();
			}catch(Exception e){
				e.printStackTrace();
			}   
	}
	
	public static void run(){
		runCode();
		CalculateFuel cf = new CalculateFuel();
		cf.runCode(noOfFlights);
	}
	
	public static void main(String[] args){
		runCode();
		CalculateFuel cf = new CalculateFuel();
		cf.runCode(noOfFlights);
	}

}