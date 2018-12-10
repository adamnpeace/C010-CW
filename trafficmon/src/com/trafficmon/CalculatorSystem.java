package com.trafficmon;

import java.math.BigDecimal;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.util.List;
import java.util.Map;

import java.time.LocalTime;

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
        int charge = 0;
        long length = 0;
        long fourHours = 14400;
        ZoneBoundaryCrossing initSession = crossings.get(0);
        ZoneBoundaryCrossing lastEvent = crossings.get(0);

        for (ZoneBoundaryCrossing crossing : crossings.subList(1, crossings.size())) {
            long durationSinceSessionBeginning = SECONDS.between(initSession.timestamp(), crossing.timestamp());
            long durationSinceLastEvent = SECONDS.between(lastEvent.timestamp(), crossing.timestamp());
            if (crossing instanceof ExitEvent) {
                // Current crossing is exit
               if (durationSinceSessionBeginning < fourHours && crossings.size() > 2) {
                    initSession = crossing;
               } else {
                   // Duration since session beginning < 4 hours
                   if (durationSinceLastEvent < fourHours) {
                       // Less than 4 hours
                       charge += lastEvent.timestamp().isBefore(LocalTime.of(14, 00)) ? 6 : 4;
                   } else {
                       // Greater than 4 hours
                       charge += 12;
                   }

               }

            }
            length += SECONDS.between(crossing.timestamp(), lastEvent.timestamp());
            lastEvent = crossing;
        }
        if (length > fourHours) return 12;

        return charge;
    }


    /*
    ######################
    TESTING
    ######################
     */

    public int getCalculatedCharge(List<ZoneBoundaryCrossing> eventLog) {
        return calculateChargeForCrossingList(eventLog);
    }
}
