package com.trafficmon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.time.Duration;
import java.time.LocalTime;

public class CalculatorSystem {

    private PenaltiesService operationsTeam;
    private CheckSystem checkSystem;

    CalculatorSystem(PenaltiesService operationsTeam, CheckSystem checkSystem) {
        this.operationsTeam = operationsTeam;
        this.checkSystem = checkSystem;
    }

    public void calculateCharges(Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicle) {
        for (Map.Entry<Vehicle, List<ZoneBoundaryCrossing>> vehicleCrossings : crossingsByVehicle.entrySet()) {
            Vehicle vehicle = vehicleCrossings.getKey();
            List<ZoneBoundaryCrossing> crossings = vehicleCrossings.getValue();

            if (!checkSystem.checkOrderingOf(crossings)) {
                operationsTeam.triggerInvestigationInto(vehicle);
            } else {
                BigDecimal charge = new BigDecimal(calculateChargeForTimeInZone(crossings));
                makeChargeTo(vehicle, charge);
            }
        }
    }

    public void makeChargeTo(Vehicle vehicle, BigDecimal charge) {
        try {
            RegisteredCustomerAccountsService.getInstance().accountFor(vehicle).deduct(charge);
        } catch (InsufficientCreditException | AccountNotRegisteredException e) {
            operationsTeam.issuePenaltyNotice(vehicle, charge);
        }
    }

    private int calculateChargeForTimeInZone(List<ZoneBoundaryCrossing> crossings) {

        int charge = 0;

        ZoneBoundaryCrossing lastEvent = crossings.get(0);

        for (ZoneBoundaryCrossing crossing : crossings.subList(1, crossings.size())) {
            if (crossing instanceof ExitEvent) {
                if (crossing.timestamp().minusHours(4).isBefore(lastEvent.timestamp())) {
                    // Less than 4 hours
                    if (lastEvent.timestamp().isBefore(LocalTime.of(14, 00))) {
                        // Before 1400
                        charge += 6;
                    } else {
                        // After 1400
                        charge += 4;
                    }
                } else {
                    // Greater than 4 hours
                    charge += 12;
                }
            }
            lastEvent = crossing;
        }
        return charge;
    }

    private int minutesBetween(long startTimeMs, long endTimeMs) {
        return (int) Math.ceil((endTimeMs - startTimeMs) / (1000.0 * 60.0));
    }


    /*
    ######################
    TESTING
    ######################
     */
    public int getCalculateCharges(ZoneBoundaryCrossing entry, ZoneBoundaryCrossing exit) {
        List<ZoneBoundaryCrossing> mockEventLog = new ArrayList<ZoneBoundaryCrossing>();
        mockEventLog.add(entry);
        mockEventLog.add(exit);
        return calculateChargeForTimeInZone(mockEventLog);
    }
}
