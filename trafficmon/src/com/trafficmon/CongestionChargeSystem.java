package com.trafficmon;

import java.util.*;

public class CongestionChargeSystem implements ICongestionChargeSystem {

    private final List<ZoneBoundaryCrossing> eventLog = new ArrayList<ZoneBoundaryCrossing>();
    private final Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicle = new HashMap<Vehicle, List<ZoneBoundaryCrossing>>();
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
        EntryEvent entryEvent = new EntryEvent(vehicle);
        eventLog.add(entryEvent);
        if (!crossingsByVehicle.containsKey(vehicle)) {
            crossingsByVehicle.put(vehicle, new ArrayList<ZoneBoundaryCrossing>());
        }
        crossingsByVehicle.get(entryEvent.getVehicle()).add(entryEvent);


    }

    @Override
    public void vehicleLeavingZone(Vehicle vehicle) {
        if (checkSystem.previouslyRegistered(vehicle, eventLog)) {
            ExitEvent exitEvent = new ExitEvent(vehicle);
            eventLog.add(exitEvent);
            crossingsByVehicle.get(exitEvent.getVehicle()).add(exitEvent);
        }
    }

    @Override
    public void calculateCharges() {
        calculatorSystem.calculateCharges(crossingsByVehicle);
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



}
