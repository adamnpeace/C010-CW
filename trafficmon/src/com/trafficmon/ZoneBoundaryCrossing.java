package com.trafficmon;

public abstract class ZoneBoundaryCrossing {

    private final Vehicle vehicle;
    private long time; // NEW

    public ZoneBoundaryCrossing(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.time = System.currentTimeMillis(); // NEW
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public long timestamp() {
        return time;
    }

    /*
        CUSTOM CODE BELOW
     */
    public void setNewTimestamp(long time) {
        this.time = time;
    }
}
