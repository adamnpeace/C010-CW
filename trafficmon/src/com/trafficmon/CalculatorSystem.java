package com.trafficmon;

import java.math.BigDecimal;

import static java.time.temporal.ChronoUnit.SECONDS;
import java.time.LocalTime;

import java.util.List;
import java.util.Map;



public class CalculatorSystem {

    private PenaltiesService operationsTeam;
    private CheckSystem checkSystem;
    private AccountsService accountsService;

    CalculatorSystem(PenaltiesService operationsTeam, CheckSystem checkSystem) {
        this.operationsTeam = operationsTeam;
        this.checkSystem = checkSystem;
        this.accountsService = RegisteredCustomerAccountsService.getInstance();
    }

    CalculatorSystem(PenaltiesService operationsTeam, CheckSystem checkSystem, AccountsService accountsService) {
        this.operationsTeam = operationsTeam;
        this.checkSystem = checkSystem;
        this.accountsService = accountsService;
    }

    public void calculateCharges(Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicle) {
        for (Map.Entry<Vehicle, List<ZoneBoundaryCrossing>> vehicleCrossings : crossingsByVehicle.entrySet()) {
            Vehicle vehicle = vehicleCrossings.getKey();
            List<ZoneBoundaryCrossing> crossings = vehicleCrossings.getValue();

            if (!checkSystem.checkOrderingOf(crossings)) {
                operationsTeam.triggerInvestigationInto(vehicle);
            } else {
                BigDecimal charge = new BigDecimal(calculateChargeForCrossingList(crossings));
                makeChargeTo(vehicle, charge);
            }
        }
    }

    public void makeChargeTo(Vehicle vehicle, BigDecimal charge) {
        try {
            accountsService.accountFor(vehicle).deduct(charge);
        } catch (InsufficientCreditException | AccountNotRegisteredException e) {
            operationsTeam.issuePenaltyNotice(vehicle, charge);
        }
    }

    private int calculateChargeForCrossingList(List<ZoneBoundaryCrossing> crossings) {
        /* ASSUMPTIONS:
        - Staying longer than 4 hours per day causes charge of 12£ no matter what
        - Entering at 1200, leaving at 1400 and reentering at 1601 will charge twice no matter what
        - Entering at 1200, leaving at 1400 and reentering at 1559 and leaving at 1958 will charge once (4£)
        - Entering at 1200, leaving at 1400, reentering at 1559, leaving at 1700, reentering at 1900 will charge twice
         */
        int charge;
        int totalCharge = 0;
        long timeSincePreviousEvent;
        long timeSpentInZone = 0;
        long fourHoursInSeconds = 14400;
        ZoneBoundaryCrossing previousEvent = crossings.get(0);

        charge = entryTimeCharge(previousEvent);
        for (ZoneBoundaryCrossing crossing : crossings.subList(1, crossings.size())) {
            timeSincePreviousEvent = Math.abs(timeDifference(crossing, previousEvent));
            if ((crossing instanceof EntryEvent) && timeSincePreviousEvent >= fourHoursInSeconds) {
                totalCharge += charge;
                charge = entryTimeCharge(crossing);
            }
            if (crossing instanceof ExitEvent) {
                timeSpentInZone += timeSincePreviousEvent;
            }
            previousEvent = crossing;
            if (timeSpentInZone >= fourHoursInSeconds) {
                return 12;
            }
        }
        totalCharge += charge;
        return totalCharge;

    }

    private int entryTimeCharge(ZoneBoundaryCrossing event) {
        return event.timestamp().isBefore(LocalTime.of(14, 00)) ? 6 : 4;
    }

    private long timeDifference(ZoneBoundaryCrossing firstEvent, ZoneBoundaryCrossing secondEvent) {
        return SECONDS.between(firstEvent.timestamp(), secondEvent.timestamp());
    }

    /*
    ######################
    TESTING
    ######################
     */

    public int getCalculatedCharge(List<ZoneBoundaryCrossing> fakeEventLog) {
        return calculateChargeForCrossingList(fakeEventLog);
    }
}
