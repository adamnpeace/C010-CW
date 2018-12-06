package com.trafficmon;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

public class CongestionChargeSystem {

    private final List<ZoneBoundaryCrossing> eventLog = new ArrayList<ZoneBoundaryCrossing>();

    private PenaltiesService operationsTeam;

    public CongestionChargeSystem(PenaltiesService operationsTeam) {
        this.operationsTeam = operationsTeam;
    }

    public void vehicleEnteringZone(Vehicle vehicle) {
        eventLog.add(new EntryEvent(vehicle));
    }

    public void vehicleLeavingZone(Vehicle vehicle) {
        if (!previouslyRegistered(vehicle)) {
            return;
        }
        eventLog.add(new ExitEvent(vehicle));
    }

    private int typeOfOrdering(List<ZoneBoundaryCrossing> crossings) {

        ZoneBoundaryCrossing lastEvent = crossings.get(0);

        for (ZoneBoundaryCrossing crossing : crossings.subList(1, crossings.size())) {
            if (crossing.timestamp().isBefore(lastEvent.timestamp())) {
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

    private boolean checkOrderingOf(List<ZoneBoundaryCrossing> crossings) {
        return typeOfOrdering(crossings) == 0;
    }

    public void calculateCharges() {

        Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicle = new HashMap<Vehicle, List<ZoneBoundaryCrossing>>();

        for (ZoneBoundaryCrossing crossing : eventLog) {
            if (!crossingsByVehicle.containsKey(crossing.getVehicle())) {
                crossingsByVehicle.put(crossing.getVehicle(), new ArrayList<ZoneBoundaryCrossing>());
            }
            crossingsByVehicle.get(crossing.getVehicle()).add(crossing);
        }

        for (Map.Entry<Vehicle, List<ZoneBoundaryCrossing>> vehicleCrossings : crossingsByVehicle.entrySet()) {
            Vehicle vehicle = vehicleCrossings.getKey();
            List<ZoneBoundaryCrossing> crossings = vehicleCrossings.getValue();

            if (!checkOrderingOf(crossings)) {
                operationsTeam.triggerInvestigationInto(vehicle);
            } else {

                BigDecimal charge = calculateChargeForTimeInZone(crossings);

                try {
                    RegisteredCustomerAccountsService.getInstance().accountFor(vehicle).deduct(charge);
                } catch (InsufficientCreditException ice) {
                    operationsTeam.issuePenaltyNotice(vehicle, charge);
                } catch (AccountNotRegisteredException e) {
                    operationsTeam.issuePenaltyNotice(vehicle, charge);
                }
            }
        }
    }

    private BigDecimal calculateChargeForTimeInZone(List<ZoneBoundaryCrossing> crossings) {

        int charge = 0;

        ZoneBoundaryCrossing lastEvent = crossings.get(0);

        for (ZoneBoundaryCrossing crossing : crossings.subList(1, crossings.size())) {

            if (crossing instanceof ExitEvent) {
                Duration durationInZone = Duration.between(lastEvent.timestamp(), crossing.timestamp());
                if (durationInZone.minus(Duration.ofHours(4)).isNegative())
                {
                    if (lastEvent.timestamp().isBefore(LocalTime.of(14, 00))) {
                        charge += 6;
                    } else {
                        charge += 4;
                    }
                } else {
                    charge += 12;
                }
            }

            lastEvent = crossing;
        }

        return new BigDecimal(charge);
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

    public List<ZoneBoundaryCrossing> getEventLog() {
        return eventLog;
    }

    public BigDecimal getCalculateCharges(List<ZoneBoundaryCrossing> crossings) {
        return calculateChargeForTimeInZone(crossings);
    }

    public int getTypeOfOrdering(List<ZoneBoundaryCrossing> crossings) {
        return typeOfOrdering(crossings);
    }

}
