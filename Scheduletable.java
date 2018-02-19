package sample;


public class Scheduletable {
    private int srno;
    private String flightno;
    private String arrivaldeparture;
    private String city;
    private String iatacode;
    private String airline;
    private java.sql.Time time;
    private int runwayallocated;
    private String terminalname;


    public Scheduletable (int srno, String flightno, String arrivaldeparture, String city, String iatacode, String airline, java.sql.Time time, int runwayallocated, String terminalname){
        this.srno = srno;
        this.flightno = flightno;
        this.arrivaldeparture = arrivaldeparture;
        this.city = city;
        this.iatacode = iatacode;
        this.airline = airline;
        this.time = time;
        this.runwayallocated = runwayallocated;
        this.terminalname = terminalname;
    }

    public int getSrno(){
        return srno;
    }

    public void setSrno(int srno){
        this.srno = srno;
    }

    public String getFlightno(){
        return flightno;
    }

    public void setFlightno(String flightno){
        this.flightno = flightno;
    }

    public String getArrivaldeparture(){
        return arrivaldeparture;
    }

    public void setArrivaldeparture(String arrivaldeparture){
        this.arrivaldeparture = arrivaldeparture;
    }

    public String getCity(){
        return city;
    }

    public void setCity(String city){
        this.city = city;
    }

    public String getIatacode(){
        return iatacode;
    }

    public void setIatacode(String iatacode){
        this.iatacode = iatacode;
    }

    public String getAirline(){
        return airline;
    }

    public void setAirline(String airline){
        this.airline = airline;
    }

    public java.sql.Time getTime(){
        return time;
    }

    public void setTime(java.sql.Time time){
        this.time = time;
    }

    public int getRunwayallocated(){
        return runwayallocated;
    }

    public void setRunwayallocated(int runwayallocated){
        this.runwayallocated = runwayallocated;
    }

    public String getTerminalname(){
        return terminalname;
    }

    public void setTerminalname(String terminalname){
        this.terminalname = terminalname;
    }




}

