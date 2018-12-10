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
    private CalculatorSystem calculatorSystem;

    public CongestionChargeSystem() {
        this.checkSystem = new CheckSystem();

        this.operationsTeam = OperationsTeam.getInstance();
        this.calculatorSystem = new CalculatorSystem(operationsTeam, checkSystem);
    }

    public CongestionChargeSystem(PenaltiesService operationsTeam, CheckSystem checkSystem, CalculatorSystem calculatorSystem) {
        this.operationsTeam = operationsTeam;
        this.checkSystem = checkSystem;
        this.calculatorSystem = calculatorSystem;
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
        calculatorSystem.calculateCharges(eventLog);
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
        return calculatorSystem.calculateChargeForTimeInZone(mockEventLog);
    }


}
