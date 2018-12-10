package com.trafficmon;

import java.math.BigDecimal;
import java.util.List;

public interface ICongestionChargeSystem {

    void vehicleEnteringZone(Vehicle vehicle);

    void vehicleLeavingZone(Vehicle vehicle);

    void calculateCharges();

    int getEventLogSize();

    ZoneBoundaryCrossing getEventLogElem(int index);

    BigDecimal getCalculateCharges(ZoneBoundaryCrossing entry, ZoneBoundaryCrossing exit);

}
