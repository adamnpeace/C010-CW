package com.trafficmon;

import java.util.List;

public interface CheckerInterface {
    boolean checkOrderingOf(List<ZoneBoundaryCrossing> crossings);

    default int typeOfOrdering(List<ZoneBoundaryCrossing> crossings) {

        ZoneBoundaryCrossing lastEvent = crossings.get(0);

        for (ZoneBoundaryCrossing crossing : crossings.subList(1, crossings.size())) {
            if (crossing instanceof EntryEvent && lastEvent instanceof ExitEvent) {
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

    int getTypeOfOrdering(List<ZoneBoundaryCrossing> crossings);
}
