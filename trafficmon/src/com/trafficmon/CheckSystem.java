package com.trafficmon;

import java.util.*;

public class CheckSystem {

    public boolean previouslyRegistered(Vehicle vehicle, List<ZoneBoundaryCrossing> eventLog) {
        boolean res = false;
        for (ZoneBoundaryCrossing crossing : eventLog) {
            if (crossing.getVehicle().equals(vehicle)) {
                res = true;
            }
        }
        return res;
    }

    public boolean checkOrderingOf(List<ZoneBoundaryCrossing> crossings) {
        return typeOfOrdering(crossings) == 0;
    }

    public int typeOfOrdering(List<ZoneBoundaryCrossing> crossings) {

        ZoneBoundaryCrossing lastEvent = crossings.get(0);

        for (ZoneBoundaryCrossing crossing : crossings.subList(1, crossings.size())) {
            if (crossing.timestamp() < lastEvent.timestamp()) {
                return 1;
            }
            if (crossing instanceof EntryEvent && lastEvent instanceof EntryEvent) {
                return 2;
            }
            if (crossing instanceof ExitEvent && lastEvent instanceof ExitEvent) {
                return 3;
            }
            lastEvent = crossing;
        }

        return 0;
    }


    /*
    ######################
    TESTING
    ######################
     */
    public int getTypeOfOrdering(List<ZoneBoundaryCrossing> crossings) {
        return typeOfOrdering(crossings);
    }

}
