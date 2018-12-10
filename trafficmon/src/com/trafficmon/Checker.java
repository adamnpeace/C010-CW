package com.trafficmon;

import java.util.List;

public class Checker implements CheckerInterface {

    @Override
    public boolean checkOrderingOf(List<ZoneBoundaryCrossing> crossings) {
        return typeOfOrdering(crossings) == 0;
    }

    @Override
    public int getTypeOfOrdering(List<ZoneBoundaryCrossing> crossings) {
        return typeOfOrdering(crossings);
    }
}
