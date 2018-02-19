import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;

public class CalculateFuel {
	
	public static String[] flightNo = new String[10];
	public static String[] airline = new String[10];
	public static String[] arrivalDeparture = new String[10];
	public static String[] aircraftName = new String[10];
	public static String[] terminalName = new String[10];
	public static String[] timeStamp = new String[10];
	public static java.sql.Time[] time = new java.sql.Time[10];
	public static double sourceDestinationDegree[] = new double[10];
	public static String sourceDestination[] = new String[10];
	public static String[] cat = new String[10];
	public static double[][] xyCood = new double[10][2];  	// every flight x and y cood
	public static double[][] runway3D = new double[10][4];	//stores distance of every plane from the runway, always assuming mumbai as source
	public static int[][] taxiTimeTakeoff = new int[10][4]; 	// taxi time before take off
	public static double windSpeed;
	public static String windDirection;
	public static double windDirectionDegree;
	public static double[] runwayWind= new double[4];
	public static double[][] runwayLenReq = new double[10][4]; // stores required runway length of each flight
	public static int[] runwayLength = new int[4];	// change 4 if no. of runways is changed
	public static boolean[] combinationValidity = new boolean[4]; // C1 C2 C3 C4
	public static boolean runwayvalidity[][] = new boolean[10][4];
	public static double[][] minLandingTaxiTime = new double[10][4]; 
	public static String[][] minLandingTaxiExit = new String[10][4];
	public static double[][] fuelConsumption = new double[10][4];
	public static double[][] fuelConsumptionReplica = new double[10][4];
	public static int[][] runwaySchedule = new int[10][4];	// stores runway name
	public static double[] cumulativeFuelConsumption = new double[4];
	public static double fixedRunwayFuelConsumption;
	public static int fixedRunwayNo = 2; 	// 0=9 1=27 2=14 3=32
	public static int fixedRunwayName = 32 ;
	public static double minFuelConsumption = 1000000;
	public static int minpos = 0;
	public static Map<Integer, CompleteFlightInfo> finalSchedule= new HashMap<Integer, CompleteFlightInfo>();
	public static int noOfFlights;
	public static int flag = 0;
	public static int prenoOfFlights = 0;
	public static int prePrenoOfFlights = 0;
	
	public static void runCode(int n){

		for(int a = 0; a < 5; a++){
			for(int b = 0; b < 4; b++){
				fuelConsumption[a][b] = 0.0;
			}
		}
		
		for(int a = 0; a < 4; a++){
			cumulativeFuelConsumption[a] = 0.0;
		}
		
		noOfFlights = n;
		findCoordinates();
		calculateDistInAir();
		FetchWind();
		calReqRunwayLen();
		calculateTaxiTimeTakeoff();
		calculateTaxiTimeLanding();
		checkValidity();
		//calculateTaxiTimeLanding();
		fuelCalculate();
		findTime();
		fuelOptimizer();
		display();
		deleteFromFlightInfoTable();
	}
	
	
		private static void deleteFromFlightInfoTable() {
		
			String url = "jdbc:mysql://localhost:3306/";
			String user = "root";
			String password = "";
			
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(url, user, password);
			Statement stt = con.createStatement();
			stt.execute("USE flextracks");
			
			stt.execute("DELETE FROM flightinfoatc LIMIT "+noOfFlights+"");
			
		}catch(Exception e){
			
		}
		
		try {
		    Thread.sleep(20 * 1000);
		    //1000 milliseconds is one second.
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
		
		
		for(int i = 0; i < noOfFlights; i++){
			sourceDestinationDegree[i] = 0.0;
			xyCood[i][0] = 0.0;
			xyCood[i][1] = 0.0;
		}
		
		
	}
	
	
	public static void main(String[] args){
	
		findCoordinates();
		calculateDistInAir();
		FetchWind();
		calReqRunwayLen();
		calculateTaxiTimeTakeoff();
		calculateTaxiTimeLanding();
		checkValidity();
		//calculateTaxiTimeLanding();
		fuelCalculate();
		findTime();
		fuelOptimizer();
		display();
	}
	
	public static void checkValidity(){
		
		String url = "jdbc:mysql://localhost:3306/";
		String user = "root";
		String password = "";
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(url, user, password);
			Statement stt = con.createStatement();
			stt.execute("USE flextracks");
			ResultSet res = stt.executeQuery("SELECT length from runway");
			int k = 0;
			while(res.next()){
				runwayLength[k++] = res.getInt("length");
			}
			
			for(int i=0;i<4;i++)
				combinationValidity[i]= true;
			
			for(int i = 0; i < noOfFlights; i++){
				for(int j = 0; j < 4; j++){
					if(runwayLenReq[i][j] + 500 <= runwayLength[j]){ // ask rathod for buffer length (here it is 500)
						runwayvalidity[i][j] = true;
					}
					else{
						runwayvalidity[i][j] = false;
					}
			}
		}
			for(int j=0; j<4; j++){		// column wise traversal to find invalid runways for departures
				for(int i = 0; i < noOfFlights; i++){
					if(runwayvalidity[i][j]== false && arrivalDeparture[i].equals("D")){
						combinationValidity[j] = false;
					}
						
				}
			}
			
			// if flag==1, then plane has only 1 runway to land, hence the opposite cannot be used for departure.
			int flag = 0, flagPos = 0;
			for(int i = 0; i < noOfFlights; i++){
				for(int j = 0; j < 4; j++){
					if(runwayvalidity[i][j]== true && arrivalDeparture[i].equals("A")){
						flag++;
						flagPos = j;//indicates which runway to be eliminated later.
					}
				}
				if(flag == 1){
					if(flagPos == 0){
						combinationValidity[1] = false;
					}
					else if(flagPos == 1){
						combinationValidity[0] = false;
					}
					else if(flagPos == 2){
						combinationValidity[3] = false;
					}
					else if(flagPos == 3){
						combinationValidity[2] = false;
					}
				}
				flag = 0;
					
			}
			
		}catch(Exception e){
			
		}
	}
	
	public static void FetchWind(){
		
		String url = "jdbc:mysql://localhost:3306/";
		String user = "root";
		String password = "";
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(url, user, password);
			Statement stt = con.createStatement();
			stt.execute("USE flextracks");
			
			ResultSet res = stt.executeQuery("SELECT * FROM wind");
			while(res.next()){
				windSpeed = res.getDouble("speed");
				windDirection = res.getString("direction");
			}
			
			switch(windDirection){
			
			case "S" : windDirectionDegree = 0;
					   break;
			
			case "SSW" : windDirectionDegree = 22.5;
					   break;

			case "SW" : windDirectionDegree = 45;
					   break;

			case "SWW" : windDirectionDegree =67.5;
					   break;

			case "W" : windDirectionDegree = 90;
					   break;

			case "NWW" : windDirectionDegree = 112.5;
					   break;

			case "NW" : windDirectionDegree = 135;
					   break;

			case "NNW" : windDirectionDegree = 157.5;
					   break;

			case "N" : windDirectionDegree = 180;
					   break;

			case "NNE" : windDirectionDegree = 202.5;
					   break;

			case "NE" : windDirectionDegree = 225;
					   break;

			case "NEE" : windDirectionDegree = 247.5;
					   break;

			case "E" : windDirectionDegree = 270;
					   break;

			case "SEE" : windDirectionDegree = 292.5;
					   break;

			case "SE" : windDirectionDegree = 315;
					   break;

			case "SSE" : windDirectionDegree = 337.5;
					   break;
					   
			}
			
			runwayWind[0] = windSpeed * Math.cos((windDirectionDegree - 90) * 3.14 / 180);
			runwayWind[1] = windSpeed * Math.cos((windDirectionDegree - 270) * 3.14 / 180);
			runwayWind[2] = windSpeed * Math.cos((windDirectionDegree - 140) * 3.14 / 180);
			runwayWind[3] = windSpeed * Math.cos((windDirectionDegree - 320) * 3.14 / 180);
			
		}catch(Exception e){
			
		}
	}
	
	public static void calReqRunwayLen(){
		
		String url = "jdbc:mysql://localhost:3306/";
		String user = "root";
		String password = "";
		double length = 0;
		try{
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection con = DriverManager.getConnection(url, user, password);
		Statement stt = con.createStatement();
		stt.execute("USE flextracks");
		
		for(int i = 0 ; i < noOfFlights; i++){
			ResultSet res = stt.executeQuery("SELECT mll,mtol  FROM aircraft WHERE aircraftName = '"+ aircraftName[i]+"'");
			while(res.next()){
			if(arrivalDeparture[i].equals("D")){
				length = res.getDouble("mtol");
				
				if(runwayWind[0] <= 0){
					runwayLenReq[i][0] = length * (1 + runwayWind[0] * 0.015); 
				}else{
					runwayLenReq[i][0] = length * (1 + runwayWind[0] * 0.05); 
				}
				
				if(runwayWind[1] <= 0){
					runwayLenReq[i][1] = length * (1 + runwayWind[1] * 0.015); 
				}else{
					runwayLenReq[i][1] = length * (1 + runwayWind[1] * 0.05); 
				}
				
				if(runwayWind[2] <= 0){
					runwayLenReq[i][2] = length * (1 + runwayWind[2] * 0.015); 
				}else{
					runwayLenReq[i][2] = length * (1 + runwayWind[2] * 0.05); 
				}
				
				if(runwayWind[3] <= 0){
					runwayLenReq[i][3] = length * (1 + runwayWind[3] * 0.015); 
				}else{
					runwayLenReq[i][3] = length * (1 + runwayWind[3] * 0.05); 
				}
			}
			
			else{
				length = res.getDouble("mll");
				if(runwayWind[0] <= 0){
					runwayLenReq[i][0] = length * (1 + runwayWind[0] * 0.015); 
				}else{
					runwayLenReq[i][0] = length * (1 + runwayWind[0] * 0.05); 
				}
				
				if(runwayWind[1] <= 0){
					runwayLenReq[i][1] = length * (1 + runwayWind[1] * 0.015); 
				}else{
					runwayLenReq[i][1] = length * (1 + runwayWind[1] * 0.05); 
				}
				
				if(runwayWind[2] <= 0){
					runwayLenReq[i][2] = length * (1 + runwayWind[2] * 0.015); 
				}else{
					runwayLenReq[i][2] = length * (1 + runwayWind[2] * 0.05); 
				}
				
				if(runwayWind[3] <= 0){
					runwayLenReq[i][3] = length * (1 + runwayWind[3] * 0.015); 
				}else{
					runwayLenReq[i][3] = length * (1 + runwayWind[3] * 0.05); 
				}
			}
			}
		}
		
		}
		catch(Exception e){
			
		}
			
	}
	
	public static void calculateDistInAir(){
		
		double runway9x = -10; // runway9
		double runway9y = 0; 	// 9
		double runway27x = 10; // 27 
		double runway27y = 0; // 27
		double runway14x = -6.4278; // 14
		double runway14y = 7.66; // 14
		double runway32x = 6.4278; 	// 32
		double runway32y= -7.66;  	//32
		
		for(int i = 0; i < noOfFlights; i++){
				runway3D[i][0] = Math.sqrt(Math.pow((xyCood[i][0] - runway9x), 2) + Math.pow((xyCood[i][1] - runway9y), 2)) + 9;
				runway3D[i][1] = Math.sqrt(Math.pow((xyCood[i][0] - runway27x), 2) + Math.pow((xyCood[i][1] - runway27y), 2)) + 9;
				runway3D[i][2] = Math.sqrt(Math.pow((xyCood[i][0] - runway14x), 2) + Math.pow((xyCood[i][1] - runway14y), 2)) + 9;
				runway3D[i][3] = Math.sqrt(Math.pow((xyCood[i][0] - runway32x), 2) + Math.pow((xyCood[i][1] - runway32y), 2)) + 9;
		}
		
//		for(int i = 0; i < 10; i++){
//			System.out.println(""+xyCood[i][0]+"     "+xyCood[i][1]);
//		}
	}
	
	public static void calculateTaxiTimeTakeoff(){  // terminal to runway start
		
		String url = "jdbc:mysql://localhost:3306/";
		String user = "root";
		String password = "";
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(url, user, password);
			Statement stt = con.createStatement();
			stt.execute("USE flextracks");
			
		for(int i = 0; i < noOfFlights; i++){
			if(arrivalDeparture[i].equals("D")){
				int j = 0;
				ResultSet res = stt.executeQuery("SELECT timeInMinutes FROM mumbaiterminalrunwaymap WHERE terminalName = '"+ terminalName[i]+"'");
				while(res.next()){
					taxiTimeTakeoff[i][j++] = res.getInt("timeInMinutes");
				}
			}
		}
		
		}catch(Exception e){
			
		}
	}
	
	public static void calculateTaxiTimeLanding(){
		
		String url = "jdbc:mysql://localhost:3306/";
		String user = "root";
		String password = "";
		try{
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection con = DriverManager.getConnection(url, user, password);
		Statement stt = con.createStatement();
		stt.execute("USE flextracks");
			
		for(int i = 0; i < noOfFlights; i++){
			for(int j = 0; j < 4; j++){
				if(arrivalDeparture[i].equals("A")){
					ResultSet res = stt.executeQuery("(SELECT MIN(timeInMinutes),exitName FROM mumbaiterminalexitmap WHERE (terminalName='"+ terminalName[i] +"') AND (exitName IN (SELECT exitName FROM mumbaiexit WHERE (distance > '"+ runwayLenReq[i][j] +"')AND (runwayId = '"+ (j + 1)  +"'))))");
					while(res.next()){
						minLandingTaxiTime[i][j] = res.getDouble("MIN(timeInMinutes)");
						minLandingTaxiExit[i][j] = res.getString("exitName");
					}
				}
			}
		}
			
		}catch(Exception e){
			
		}
	
	}

	public static void fuelCalculate()
	{
		
		String url = "jdbc:mysql://localhost:3306/";
		String user = "root";
		String password = "";
		try{
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection con = DriverManager.getConnection(url, user, password);
		Statement stt = con.createStatement();
		stt.execute("USE flextracks");
		
		double t1=0,t2=0;
		for(int i = 0; i < noOfFlights; i++)
			for(int j = 0; j < 4; j++){
				
				if(arrivalDeparture[i].equals("A"))
				{
					ResultSet res=stt.executeQuery("SELECT airfcpnm,taxifcpm FROM aircraft WHERE aircraftName='"+aircraftName[i]+"'");
					while(res.next()){
						
						t1=res.getDouble("airfcpnm");///air time fuel per nm
						t2=res.getDouble("taxifcpm");//taxi time fuel per minute
						fuelConsumption[i][j] = runway3D[i][j] * t1 * 0.35+ minLandingTaxiTime[i][j] * t2;
					}			
				}
				else
				{
					ResultSet res=stt.executeQuery("SELECT airfcpnm,taxifcpm FROM aircraft WHERE aircraftName='"+aircraftName[i]+"'");
					while(res.next()){
						
						t1=res.getDouble("airfcpnm");///air time fuel per nm
						t2=res.getDouble("taxifcpm");//taxi time fuel per minute
						fuelConsumption[i][j] = runway3D[i][j] * t1 + taxiTimeTakeoff[i][j] * t2;
					}
				}
			}	
		
		for(int i = 0; i < noOfFlights; i++){
			for(int j = 0; j < 4; j++){
				fuelConsumptionReplica[i][j] = fuelConsumption[i][j];
				if(!runwayvalidity[i][j]){
					fuelConsumption[i][j] = -1;
				}
			}
		}
		
//		System.out.println();
//		System.out.println();
//		System.out.println("runway3D");
//		
//		for(int a = 0; a < 10; a++){
//			for(int b = 0; b < 4; b++){
//				System.out.print( "        " + fuelConsumption[a][b]);
//			}
//			System.out.println();
//		}
		
		}catch(Exception e){
			
		}
		
	}
	
	public static void fuelOptimizer(){
		boolean[][] combinationRunwayMap = {{true, false, true, true}, {false, true, true, true}, {true, true, true, false}, {true, true, false, true}};
		// this map can be manipulated according to runways dependency
		
		
		for(int i = 0; i < 4; i++){ // c1, c2, c2, c2
			if(combinationValidity[i]){
				for(int j = 0; j < noOfFlights; j++){
					if(arrivalDeparture[j].equals("A")){
						
					double minval = 1000000;
					for(int k = 0; k < 4; k++){ // k = runways
						if(fuelConsumption[j][k] < minval && combinationRunwayMap[i][k] == true && fuelConsumption[j][k] != -1){
							minval = fuelConsumption[j][k];
							runwaySchedule[j][i] = k + 1;
						}
						
					}
					cumulativeFuelConsumption[i] += minval;
					minval = 1000000;
				}
					else{
						cumulativeFuelConsumption[i] += fuelConsumption[j][i];
						runwaySchedule[j][i] = i + 1;
					}
				}
			}
		}
				
		for(int i = 0; i < 4; i++){
			if(cumulativeFuelConsumption[i] < minFuelConsumption && cumulativeFuelConsumption[i] != 0){
				minFuelConsumption = cumulativeFuelConsumption[i];
				minpos = i;
			}
		}
		////////////////////////////////////////////////////////////////////////////////////////////////////////
//		System.out.println();
//		System.out.println();
//		for(int i = 0; i < 5; i++){
//				//System.out.print(xyCood[i][0] + " " + xyCood[i][1]);
//			System.out.println(sourceDestinationDegree[i]);
//			System.out.println();
//		}
		
		// array of flight objects
		
		CompleteFlightInfo[] cfi = new CompleteFlightInfo[noOfFlights];
		
		for(int i = 0; i < noOfFlights; i++){
			cfi[i] = new CompleteFlightInfo(flightNo[i], airline[i], arrivalDeparture[i], sourceDestination[i],
					aircraftName[i], terminalName[i], timeStamp[i], time[i], sourceDestinationDegree[i], cat[i], runwaySchedule[i][minpos]);
		}
		finalSchedule.clear();
		finalSchedule = ConflictResolver2.resolver(cfi, noOfFlights);
		System.out.println(finalSchedule);
		//Print.pr(finalSchedule);
		//A.xyz(cfi);
		// fuel consumption if one runway is fixed
		fixedRunwayFuelConsumption = 0;
		for(int i = 0; i < noOfFlights; i++){
			fixedRunwayFuelConsumption += fuelConsumptionReplica[i][fixedRunwayNo];
		}
	}
	
	public static void findCoordinates(){
			
		      try{
		    	  
		    	String url = "jdbc:mysql://localhost:3306/";
				String user = "root";
				String password = "";
				
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				Connection con = DriverManager.getConnection(url, user, password);
					
					
					Statement stt = con.createStatement();
					
					stt.execute("USE flextracks");
					
					int i = 0;
					// Get people surname Bloggs
					ResultSet res = stt.executeQuery("SELECT * FROM flightcurrenttable");
					while(res.next()){
						flightNo[i] = res.getString("flightNo");
						airline[i] = res.getString("airline");
						arrivalDeparture[i] = res.getString("arrivalDeparture");
						aircraftName[i] = res.getString("aircraftName");
						terminalName[i] = res.getString("terminalName");
						timeStamp[i] = res.getString("timeStamp");
						//time[i] = res.getString("time");
						sourceDestinationDegree[i] = res.getDouble("sourceDestinationDegree");
						cat[i] = res.getString("cat");
						i++;
					}
					
					int ch = 0;
					
					// on which quadrant the plane lies
					for(i = 0; i < noOfFlights; i++){
						if(sourceDestinationDegree[i] >= 0 && sourceDestinationDegree[i] < 90){
							ch = 3;
						}
						else if(sourceDestinationDegree[i] >= 90 && sourceDestinationDegree[i] < 180){
							ch = 2;
						}
						else if(sourceDestinationDegree[i] >= 180 && sourceDestinationDegree[i] < 270){
							ch = 1;
						}
						else if(sourceDestinationDegree[i] >= 270 && sourceDestinationDegree[i] <= 360){
							ch = 4;
						}
						switch(ch){
						
						case 1: xyCood[i][0] = 20 * Math.cos((270 - sourceDestinationDegree[i]) / 180.0 * 3.14);
						xyCood[i][1] = 20 * Math.sin((270 - sourceDestinationDegree[i]) / 180.0 * 3.14);
						break;

						case 2: xyCood[i][0] = -20 * Math.cos((sourceDestinationDegree[i] - 90) / 180.0 * 3.14);
						xyCood[i][1] = 20 * Math.sin((sourceDestinationDegree[i] - 90) / 180.0 * 3.14);
						break;	

						case 3: xyCood[i][0] = -20 * Math.cos((90 - sourceDestinationDegree[i]) / 180.0 * 3.14);
						xyCood[i][1] = -20 * Math.sin((90 - sourceDestinationDegree[i]) / 180.0 * 3.14);
						break;

						case 4: xyCood[i][0] = 20 * Math.cos((sourceDestinationDegree[i] - 270) / 180.0 * 3.14);
						xyCood[i][1] = -20 * Math.sin((sourceDestinationDegree[i] - 270) / 180.0 * 3.14);
						break;				
							
						default: System.out.println("error");		
						}
					}
					
				}catch(Exception e){
					e.printStackTrace();
				}
	}

	public static void findTime(){
		String url = "jdbc:mysql://localhost:3306/";
		String user = "root";
		String password = "";
		try{
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection con = DriverManager.getConnection(url, user, password);
		Statement stt = con.createStatement();
		stt.execute("USE flextracks");
		
		int i = 0;
		ResultSet res = stt.executeQuery("SELECT time,sourceDestination FROM flightinfoatc");
		while(res.next()){
			time[i] = res.getTime("time");
			sourceDestination[i] = res.getString("sourceDestination");
			i++;
		}
			
		}catch(Exception e){
			
		}
	}
	
	public static void display(){
		
		System.out.println("*****************************FLEX-TRACKS SIMULATION************************************");
		System.out.println();
		System.out.println("Fuel consumption of Runway " + fixedRunwayName + " = "+fixedRunwayFuelConsumption+ " litres.");
		System.out.println();
		System.out.println("When Mumbai Airport is operated with Flex-Tracks.");
		System.out.println();
		System.out.println("Fuel consumption = "+ minFuelConsumption + " Litres.");
		System.out.println();
		System.out.println("Fuel saved = " + (fixedRunwayFuelConsumption - minFuelConsumption) + " Litres.");
		System.out.println();
		double fuelsaved = fixedRunwayFuelConsumption - minFuelConsumption;
		System.out.println();
		
		System.out.println("Sr No.  |\t Flight No.   |      Arrival/Departure     |\t City   |\t Time         |\t   Runway Alloted");
		
		for(int i = 0; i < noOfFlights; i++){
			System.out.print(i+1 + "   \t|\t    " + finalSchedule.get(i).flightNo + "    |\t\t \t" + finalSchedule.get(i).arrivalDeparture +"\t   |     " + finalSchedule.get(i).sourceDestination + "\t|      " + ConflictResolver2.allotedTime.get(i) +"\t      |          "+ finalSchedule.get(i).runway);
			System.out.println();
		}
		System.out.println();
		System.out.println("THIS IS JUST 15 MINUTES IN MUMBAI, THINK OF THE SAVINGS IF FLEX-TRACKS IS OPERATING AT EVERY AIRPORT ALL THE TIME!!!!!");
		
		String url = "jdbc:mysql://localhost:3306/";
		String user = "root";
		String password = "";
		
	try{
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection con = DriverManager.getConnection(url, user, password);
		Statement stt = con.createStatement();
		stt.execute("USE flextracks");

		stt.execute("UPDATE displayfuelconsumption set runway = '"+fixedRunwayName+"' WHERE id = '1'");
		stt.execute("UPDATE displayfuelconsumption set fixedRunwayFuelConsumption = '"+fixedRunwayFuelConsumption+"' WHERE id = '1'");
		stt.execute("UPDATE displayfuelconsumption set minFuelConsumption = '"+minFuelConsumption+"' WHERE id = '1'");
		stt.execute("UPDATE displayfuelconsumption set fuelSaved = '"+fuelsaved+"' WHERE id = '1'");


		if(flag == 0){
			stt.execute("TRUNCATE TABLE  schedule");
			flag++;
			prePrenoOfFlights = prenoOfFlights;
			prenoOfFlights = noOfFlights;
		}else if(flag == 1){
			flag++;
			prePrenoOfFlights = prenoOfFlights;
			prenoOfFlights = noOfFlights;
		}else{
			prePrenoOfFlights = prenoOfFlights;
			prenoOfFlights = noOfFlights;
			stt.execute("DELETE FROM schedule LIMIT "+prePrenoOfFlights+"");
		}
		String[] city = new String[noOfFlights];
		for(int i = 0; i < noOfFlights; i++){
			ResultSet res = stt.executeQuery("SELECT `city` FROM `airportsinfo` WHERE `iataCode` = '"+  finalSchedule.get(i).sourceDestination+"'");
			while(res.next()){
				city[i] = res.getString("city");
			}
		}
		
		for(int i = 0; i < noOfFlights; i++)
		{
			stt.execute("INSERT INTO `schedule`(`flightNo`, `arrivalDeparture`, `city`, `sourceDestination`,`airline`, `time`, `runwayAllocated`, "
					+ "`terminalName`) VALUES ('"+finalSchedule.get(i).flightNo+"','"+finalSchedule.get(i).arrivalDeparture+"','"+ city[i] +"','"+finalSchedule.get(i).sourceDestination+"',"
					+ "'"+finalSchedule.get(i).airline+"','"+ConflictResolver2.allotedTime.get(i)+"','"+finalSchedule.get(i).runway+"','"+finalSchedule.get(i).terminalName+"')");
		}
	}catch(Exception e){
		
	}
		
	}
}