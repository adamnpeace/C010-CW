package com.trafficmon;

public class Example {
    public static void main(String[] args) throws Exception {
        CongestionChargeSystem congestionChargeSystem = new CongestionChargeSystem(new ChargeCalculator(new Checker()));
        congestionChargeSystem.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        delaySeconds(1);
        congestionChargeSystem.vehicleEnteringZone(Vehicle.withRegistration("J091 4PY"));
        delaySeconds(5);
        congestionChargeSystem.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
        delaySeconds(1);
        congestionChargeSystem.vehicleLeavingZone(Vehicle.withRegistration("J091 4PY"));
        congestionChargeSystem.calculateCharges();
    }
    private static void delaySeconds(int secs) throws InterruptedException {
        Thread.sleep(secs * 1000);
    }
}