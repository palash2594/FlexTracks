import java.sql.Time;

public class CompleteFlightInfo {
	
	public String flightNo;
	public String airline;
	public String arrivalDeparture;
	public String sourceDestination;
	public String aircraftName;
	public String terminalName;
	public String timeStamp;
	public java.sql.Time time;
	public double sourceDestinationDegree;
	public String category;
	public double latitude;
	public double longitude;
	public int runway;
	

	public CompleteFlightInfo(String flightNo, String airline, String arrivalDeparture, String sourceDestination,
			String aircraftName, String terminalName, String timeStamp, Time time, double sourceDestinationDegree, String category, int runway){
		
		this.flightNo = flightNo;
		this.airline = airline;
		this.arrivalDeparture = arrivalDeparture;
		this.sourceDestination = sourceDestination;
		this.aircraftName = aircraftName;
		this.terminalName = terminalName;
		this.timeStamp = timeStamp;
		this.sourceDestinationDegree = sourceDestinationDegree;
		this.category = category;
		this.time = time;
		this.runway = runway;
		
	}
	
	public void setTime(java.sql.Time t)
	{
		time=t;
	}
	
	public String toString(){
		return flightNo + " " + arrivalDeparture + " " + sourceDestination + " " + time + " " + runway;
	}
	
}