package com.trafficmon;

import java.math.BigDecimal;
import java.util.List;

public interface ICongestionChargeSystem {
    BigDecimal CHARGE_RATE_POUNDS_PER_MINUTE = new BigDecimal(0.05);

    void vehicleEnteringZone(Vehicle vehicle);

    void vehicleLeavingZone(Vehicle vehicle);

    void calculateCharges();

    default BigDecimal calculateChargeForTimeInZone(List<ZoneBoundaryCrossing> crossings) {

        BigDecimal charge = new BigDecimal(0);

        ZoneBoundaryCrossing lastEvent = crossings.get(0);

        for (ZoneBoundaryCrossing crossing : crossings.subList(1, crossings.size())) {

            if (crossing instanceof ExitEvent) {
                charge = charge.add(
                        new BigDecimal(minutesBetween(lastEvent.timestamp(), crossing.timestamp()))
                                .multiply(CHARGE_RATE_POUNDS_PER_MINUTE));
            }

            lastEvent = crossing;
        }

        return charge;
    }

    default int minutesBetween(long startTimeMs, long endTimeMs) {
        return (int) Math.ceil((endTimeMs - startTimeMs) / (1000.0 * 60.0));
    }

    int getEventLogSize();

    ZoneBoundaryCrossing getEventLogElem(int index);

    BigDecimal getCalculateCharges(ZoneBoundaryCrossing entry, ZoneBoundaryCrossing exit);

}
