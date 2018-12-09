package com.trafficmon;

import java.math.BigDecimal;
import java.util.*;
/*
Changes made to constructor, calculateCharges, checkOrderingOf, typeOfOrdering, getTypeOfOrdering, getEventLogSize, getEventLogElem
 */
public class CongestionChargeSystem {

    private final List<ZoneBoundaryCrossing> eventLog = new ArrayList<>();
    private final Map<Vehicle, List<ZoneBoundaryCrossing>> vehicleCrossings = new HashMap<>();

    private final ChargeCalculator chargeCalculator;

    public CongestionChargeSystem(ChargeCalculator chargeCalculator) {
        this.chargeCalculator = chargeCalculator;
    }

    public void calculateCharges() {
        chargeCalculator.calculateCharges(vehicleCrossings);
    }
    public void vehicleEnteringZone(Vehicle vehicle) {
        eventLog.add(new EntryEvent(vehicle));
    }

    public void vehicleLeavingZone(Vehicle vehicle) {
        if (previouslyRegistered(vehicle)) {
            eventLog.add(new ExitEvent(vehicle));
        }
    }


    private boolean previouslyRegistered(Vehicle vehicle) {
        boolean res = false;
        for (ZoneBoundaryCrossing crossing : eventLog) {
            if (crossing.getVehicle().equals(vehicle)) {
                res = true;
            }
        }
        return res;
    }



    public int getEventLogSize() {
        return eventLog.size();
    }

    public ZoneBoundaryCrossing getEventLogElem(int index) {
        return eventLog.get(index);
    }

    public Map<Vehicle, List<ZoneBoundaryCrossing>> getHashMap() {
        return vehicleCrossings;
    }





}
