package sample;


public class Scheduletable1 {
    private int runway;
    private String fuelconsumption;
    private String minfuelconsumption;
    private String fuelsaved;



    public Scheduletable1 (int runway, String fuelconsumption, String minfuelconsumption, String fuelsaved){
        this.runway = runway;
        this.fuelconsumption = fuelconsumption;
        this.minfuelconsumption = minfuelconsumption;
        this.fuelsaved = fuelsaved;

    }

    public int getRunway(){
        return runway;
    }

    public void setRunway(int runway){
        this.runway = runway;
    }

    public String getFuelconsumption(){
        return fuelconsumption;
    }

    public void setFuelconsumption(String fuelconsumtion){
        this.fuelconsumption = fuelconsumption;
    }

    public String getMinfuelconsumption(){
        return minfuelconsumption;
    }

    public void setMinfuelconsumption(String minfuelconsumtion){
        this.minfuelconsumption = minfuelconsumtion;
    }

    public String getFuelsaved(){
        return fuelsaved;
    }

    public void setFuelsaved(String fuelsaved){
        this.fuelsaved = fuelsaved;
    }


}

