package com.trafficmon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ChargeCalculator {

    private final CheckerInterface checkerInterface;
    private final PenaltiesService operationsTeam;
    private final AccountsService accountsService;


    BigDecimal CHARGE_RATE_POUNDS_PER_MINUTE = new BigDecimal(0.05);
    {}

    public ChargeCalculator ( CheckerInterface checkerInterface) {
        this.checkerInterface = checkerInterface;
        this.operationsTeam = OperationsTeam.getInstance();
        this.accountsService = RegisteredCustomerAccountsService.getInstance();
    }

    public ChargeCalculator (CheckerInterface checkerInterface, PenaltiesService operationsTeam) {
        this.checkerInterface = checkerInterface;
        this.operationsTeam = operationsTeam;
        this.accountsService = RegisteredCustomerAccountsService.getInstance();
    }



    public void calculateCharges(Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicle) {

        for (Map.Entry<Vehicle, List<ZoneBoundaryCrossing>> vehicleCrossings : crossingsByVehicle.entrySet()) {

            Vehicle vehicle = vehicleCrossings.getKey();
            List<ZoneBoundaryCrossing> crossings = vehicleCrossings.getValue();

            if (!checkerInterface.checkOrderingOf(crossings)) {
                operationsTeam.triggerInvestigationInto(vehicle);
            } else {
                executeCharge(vehicle, calculateChargeForTimeInZone(crossings));
            }
        }
    }

    BigDecimal calculateChargeForTimeInZone(List<ZoneBoundaryCrossing> crossings) {
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

    public void executeCharge(Vehicle vehicle, BigDecimal chargeAmount) {
        try {
            accountsService.accountFor(vehicle).deduct(chargeAmount);
        } catch (InsufficientCreditException | AccountNotRegisteredException e) {
            operationsTeam.issuePenaltyNotice(vehicle, chargeAmount);
        }
    }

    public BigDecimal getCalculateCharges(List<ZoneBoundaryCrossing> crossings) {
        return calculateChargeForTimeInZone(crossings);
    }

    int minutesBetween(long startTimeMs, long endTimeMs) {
        return (int) Math.ceil((endTimeMs - startTimeMs) / (1000.0 * 60.0));
    }

}
