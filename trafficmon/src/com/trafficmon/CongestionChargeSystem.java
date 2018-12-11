package com.trafficmon;

import java.util.*;

public class CongestionChargeSystem {

    private final List<ZoneBoundaryCrossing> eventLog = new ArrayList<>();
    private final Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicle = new HashMap<>();
    private final PenaltiesService operationsTeam;
    private CheckSystem checkSystem;
    private CalculatorSystem calculatorSystem;

    // Default constructor - only used in actual implementation
    CongestionChargeSystem() {
        this.checkSystem = new CheckSystem();
        this.operationsTeam = OperationsTeam.getInstance();
        this.calculatorSystem = new CalculatorSystem(operationsTeam, checkSystem);
    }

    CongestionChargeSystem(PenaltiesService operationsTeam, CheckSystem checkSystem, CalculatorSystem calculatorSystem) {
        this.operationsTeam = operationsTeam;
        this.checkSystem = checkSystem;
        this.calculatorSystem = calculatorSystem;
    }

    public void vehicleEnteringZone(Vehicle vehicle) {
        EntryEvent entryEvent = new EntryEvent(vehicle);
        eventLog.add(entryEvent);
        if (!crossingsByVehicle.containsKey(vehicle)) {
            crossingsByVehicle.put(vehicle, new ArrayList<>());
        }
        crossingsByVehicle.get(entryEvent.getVehicle()).add(entryEvent);
    }

    public void vehicleLeavingZone(Vehicle vehicle) {
        if (checkSystem.previouslyRegistered(vehicle, eventLog)) {
            ExitEvent exitEvent = new ExitEvent(vehicle);
            eventLog.add(exitEvent);
            crossingsByVehicle.get(exitEvent.getVehicle()).add(exitEvent);
        }
    }

    public void calculateCharges() {
        calculatorSystem.calculateCharges(crossingsByVehicle);
    }


    /*
    ######################
    TESTING
    ######################
     */
    public int getEventLogSize() {
        return eventLog.size();
    }

    public ZoneBoundaryCrossing getEventLogElem(int index) {
        return eventLog.get(index);
    }
}
