package com.trafficmon;

import java.util.*;

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
        Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicle = new HashMap<Vehicle, List<ZoneBoundaryCrossing>>();

        for (ZoneBoundaryCrossing crossing : eventLog) {
            if (!crossingsByVehicle.containsKey(crossing.getVehicle())) {
                crossingsByVehicle.put(crossing.getVehicle(), new ArrayList<ZoneBoundaryCrossing>());
            }
            crossingsByVehicle.get(crossing.getVehicle()).add(crossing);
        }
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
