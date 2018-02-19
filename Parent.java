import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Parent {
	

	static String url = "jdbc:mysql://localhost:3306/";
	static String user = "root";
	static String password = "";
	
	public static void main(String[] args){
		
		while(true){
			
			DynamicEntryFetch.run();
			
			int time = 0;
			try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(url, user, password);
			Statement stt = con.createStatement();
			stt.execute("USE flextracks");
			
			try {
			    Thread.sleep(10000);
			    //1000 milliseconds is one second.
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			
			ResultSet res = stt.executeQuery("SELECT time FROM timereqforlastslot");
			while(res.next()){
				time = res.getInt("time");
			}
				
			}catch(Exception e){
				
			}
			
			try {
			    Thread.sleep(10 * 1000 - 10000);
			    //1000 milliseconds is one second.
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			
		//deleteFromFlightInfoTable();
			
		}
	}

}
