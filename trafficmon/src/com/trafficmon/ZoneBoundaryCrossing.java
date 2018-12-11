package com.trafficmon;
import java.time.Duration;
import java.time.LocalTime;

public abstract class ZoneBoundaryCrossing {

    private final Vehicle vehicle;
    private LocalTime time;

    public ZoneBoundaryCrossing(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.time = LocalTime.now();
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public LocalTime timestamp() {
        return time;
    }

    public void setNewTimestamp(LocalTime time) {
        this.time = time;
    }
}
