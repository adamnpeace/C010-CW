package com.trafficmon;

import java.math.BigDecimal;
import java.util.*;
/*
Changes made to constructor, calculateCharges, checkOrderingOf, typeOfOrdering, getTypeOfOrdering, getEventLogSize, getEventLogElem
 */
public class CongestionChargeSystem implements ICongestionChargeSystem {

    private final List<ZoneBoundaryCrossing> eventLog = new ArrayList<ZoneBoundaryCrossing>();

    private final PenaltiesService operationsTeam;
    private CheckSystem checkSystem;

    public CongestionChargeSystem() {
        this.checkSystem = new CheckSystem();
        this.operationsTeam = OperationsTeam.getInstance();
    }

    public CongestionChargeSystem(PenaltiesService operationsTeam, CheckSystem checkSystem) {
        this.operationsTeam = operationsTeam;
        this.checkSystem = checkSystem;
    }

    @Override
    public void vehicleEnteringZone(Vehicle vehicle) {
        eventLog.add(new EntryEvent(vehicle));
    }

    @Override
    public void vehicleLeavingZone(Vehicle vehicle) {
        if (checkSystem.previouslyRegistered(vehicle, eventLog)) {
            eventLog.add(new ExitEvent(vehicle));
        }
    }

    @Override
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

            if (!checkSystem.checkOrderingOf(crossings)) {
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


    /*
    ######################
    TESTING
    ######################
     */
    @Override
    public int getEventLogSize() {
        return eventLog.size();
    }

    @Override
    public ZoneBoundaryCrossing getEventLogElem(int index) {
        return eventLog.get(index);
    }

    @Override
    public BigDecimal getCalculateCharges(ZoneBoundaryCrossing entry, ZoneBoundaryCrossing exit) {
        List<ZoneBoundaryCrossing> mockEventLog = new ArrayList<ZoneBoundaryCrossing>();
        mockEventLog.add(entry);
        mockEventLog.add(exit);
        return calculateChargeForTimeInZone(mockEventLog);
    }


}
