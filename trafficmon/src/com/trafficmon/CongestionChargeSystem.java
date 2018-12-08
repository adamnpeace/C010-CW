package com.trafficmon;

import java.math.BigDecimal;
import java.util.*;
/*
Changes made to constructor, calculateCharges, checkOrderingOf, typeOfOrdering, getTypeOfOrdering, getEventLogSize, getEventLogElem
 */
public class CongestionChargeSystem {

    public static final BigDecimal CHARGE_RATE_POUNDS_PER_MINUTE = new BigDecimal(0.05);

    private final List<ZoneBoundaryCrossing> eventLog = new ArrayList<ZoneBoundaryCrossing>();

    private final PenaltiesService operationsTeam;

    public CongestionChargeSystem(PenaltiesService operationsTeam) {
        this.operationsTeam = operationsTeam;
    }

    public void vehicleEnteringZone(Vehicle vehicle) {
        eventLog.add(new EntryEvent(vehicle));
    }

    public void vehicleLeavingZone(Vehicle vehicle) {
        if (previouslyRegistered(vehicle)) {
            eventLog.add(new ExitEvent(vehicle));
        }
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

    private boolean previouslyRegistered(Vehicle vehicle) {
        boolean res = false;
        for (ZoneBoundaryCrossing crossing : eventLog) {
            if (crossing.getVehicle().equals(vehicle)) {
                res = true;
            }
        }
        return res;
    }

    private int minutesBetween(long startTimeMs, long endTimeMs) {
        return (int) Math.ceil((endTimeMs - startTimeMs) / (1000.0 * 60.0));
    }

    public int getEventLogSize() {
        return eventLog.size();
    }

    public ZoneBoundaryCrossing getEventLogElem(int index) {
        return eventLog.get(index);
    }

    public BigDecimal getCalculateCharges(ZoneBoundaryCrossing entry, ZoneBoundaryCrossing exit) {
        List<ZoneBoundaryCrossing> mockEventLog = new ArrayList<ZoneBoundaryCrossing>();
        mockEventLog.add(entry);
        mockEventLog.add(exit);
        return calculateChargeForTimeInZone(mockEventLog);
    }

    private boolean checkOrderingOf(List<ZoneBoundaryCrossing> crossings) {
        return typeOfOrdering(crossings) == 0 ? true : false;
    }

    private int typeOfOrdering(List<ZoneBoundaryCrossing> crossings) {

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

    public int getTypeOfOrdering(List<ZoneBoundaryCrossing> crossings) {
        return typeOfOrdering(crossings);
    }

}
