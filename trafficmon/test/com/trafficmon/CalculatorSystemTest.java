package com.trafficmon;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CalculatorSystemTest {
    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    PenaltiesService operationsTeam = context.mock(PenaltiesService.class);
    CheckSystem checkSystem = new CheckSystem();
    CalculatorSystem calculatorSystem = new CalculatorSystem(operationsTeam, checkSystem);
    /*
        ######################
        Correct Single Entries
        ######################
    */
    @Test
    public void beforeTwoLessThanFourHrsCostsSixPounds() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        int expectedCharge = 6;
        LocalTime entryTime = LocalTime.of(13, 59);
        LocalTime exitTime = LocalTime.of(17, 58);

        List<ZoneBoundaryCrossing> fakeEventLog = new ArrayList<>();

        EntryEvent entryEvent1 = new EntryEvent(vehicle);
        ExitEvent exitEvent1 = new ExitEvent(vehicle);

        fakeEventLog.add(entryEvent1);
        fakeEventLog.add(exitEvent1);

        fakeEventLog.get(0).setNewTimestamp(entryTime);
        fakeEventLog.get(1).setNewTimestamp(exitTime);

        int calculatedCharge = calculatorSystem.getCalculatedCharge(fakeEventLog);
        assertThat(calculatedCharge, is(expectedCharge));
    }

    @Test
    public void afterTwoLessThanFourHrsCostsFourPounds() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        int expectedCharge = 4;
        LocalTime entryTime = LocalTime.of(14, 01);
        LocalTime exitTime = LocalTime.of(18, 00);

        List<ZoneBoundaryCrossing> fakeEventLog = new ArrayList<>();

        EntryEvent entryEvent1 = new EntryEvent(vehicle);
        ExitEvent exitEvent1 = new ExitEvent(vehicle);

        fakeEventLog.add(entryEvent1);
        fakeEventLog.add(exitEvent1);

        fakeEventLog.get(0).setNewTimestamp(entryTime);
        fakeEventLog.get(1).setNewTimestamp(exitTime);

        int calculatedCharge = calculatorSystem.getCalculatedCharge(fakeEventLog);
        assertThat(calculatedCharge, is(expectedCharge));
    }

    @Test
    public void beforeTwoMoreThanFourHrsCostsTwelvePounds() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        int expectedCharge = 12;
        LocalTime entryTime = LocalTime.of(13, 59);
        LocalTime exitTime = LocalTime.of(18, 00);

        List<ZoneBoundaryCrossing> fakeEventLog = new ArrayList<>();

        EntryEvent entryEvent1 = new EntryEvent(vehicle);
        ExitEvent exitEvent1 = new ExitEvent(vehicle);

        fakeEventLog.add(entryEvent1);
        fakeEventLog.add(exitEvent1);

        fakeEventLog.get(0).setNewTimestamp(entryTime);
        fakeEventLog.get(1).setNewTimestamp(exitTime);

        int calculatedCharge = calculatorSystem.getCalculatedCharge(fakeEventLog);
        assertThat(calculatedCharge, is(expectedCharge));
    }

    @Test
    public void afterTwoMoreThanFourHrsCostsTwelvePounds() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        int expectedCharge = 12;
        LocalTime entryTime = LocalTime.of(14, 01);
        LocalTime exitTime = LocalTime.of(18, 01);

        List<ZoneBoundaryCrossing> fakeEventLog = new ArrayList<>();

        EntryEvent entryEvent1 = new EntryEvent(vehicle);
        ExitEvent exitEvent1 = new ExitEvent(vehicle);

        fakeEventLog.add(entryEvent1);
        fakeEventLog.add(exitEvent1);

        fakeEventLog.get(0).setNewTimestamp(entryTime);
        fakeEventLog.get(1).setNewTimestamp(exitTime);

        int calculatedCharge = calculatorSystem.getCalculatedCharge(fakeEventLog);
        assertThat(calculatedCharge, is(expectedCharge));
    }
    /*
        ######################
        Correct Re-Entries
        ######################
    */
/*
    @Test
    public void afterTwoReentryWithinFourHours() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        int expectedCharge = 4;
        LocalTime entryTime1 = LocalTime.of(14, 01);
        LocalTime exitTime1 = LocalTime.of(15, 01);
        LocalTime entryTime2 = LocalTime.of(16, 01);
        LocalTime exitTime2 = LocalTime.of(17, 01);
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.getEventLogElem(0).setNewTimestamp(entryTime1);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLogElem(1).setNewTimestamp(exitTime1);
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.getEventLogElem(0).setNewTimestamp(entryTime2);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLogElem(1).setNewTimestamp(exitTime2);
        int calculatedCharge = calculatorSystem.getCalculateCosts(congestionChargeSystem.getEventLogElem(0), congestionChargeSystem.getEventLogElem(1));
        assertThat(calculatedCharge, is(expectedCharge));
    }
    */
/*
    @Test
    public void beforeTwoReentryLessThanFourHrsCostsSixPounds() {
        Vehicle vehicle1 = Vehicle.withRegistration("J091 4PY");
        Vehicle vehicle2 = Vehicle.withRegistration("A123 XYZ");
        int expectedCharge = 10;

        List<ZoneBoundaryCrossing> fakeEventLog = new ArrayList<>();

        EntryEvent entryEvent1 = new EntryEvent(vehicle1);
        ExitEvent exitEvent1 = new ExitEvent(vehicle1);
        EntryEvent entryEvent2 = new EntryEvent(vehicle2);
        ExitEvent exitEvent2 = new ExitEvent(vehicle2);

        fakeEventLog.add(entryEvent1);
        fakeEventLog.add(exitEvent1);
        fakeEventLog.add(entryEvent2);
        fakeEventLog.add(exitEvent2);

        LocalTime entryTime1 = LocalTime.of(14, 01); // After 2pm
        LocalTime exitTime1 = LocalTime.of(18, 00); // Before 4 hours
        LocalTime entryTime2 = LocalTime.of(13, 59); // Before 2pm
        LocalTime exitTime2 = LocalTime.of(17, 58); // Before 4 hours
        fakeEventLog.get(0).setNewTimestamp(entryTime1);
        fakeEventLog.get(2).setNewTimestamp(entryTime2);
        fakeEventLog.get(1).setNewTimestamp(exitTime1);
        fakeEventLog.get(3).setNewTimestamp(exitTime2);

        int calculatedCharge = calculatorSystem.getCalculatedCharge(fakeEventLog);
        assertThat(calculatedCharge, is(expectedCharge));
    }
    /*
        ######################
        ERRORS
        ######################
    */

    @Test
    public void exitBeforeEntryTriggersInvestigation() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        List<ZoneBoundaryCrossing> fakeEventLog = new ArrayList<>();
        EntryEvent entryEvent = new EntryEvent(vehicle);
        ExitEvent exitEvent = new ExitEvent(vehicle);
        fakeEventLog.add(entryEvent);
        fakeEventLog.add(exitEvent);
        fakeEventLog.get(0).setNewTimestamp(LocalTime.of(5, 00));
        fakeEventLog.get(1).setNewTimestamp(LocalTime.of(4, 00));
        Map<Vehicle, List<ZoneBoundaryCrossing>> fakeCrossingsByVehicle = new HashMap<>();

        for (ZoneBoundaryCrossing crossing : fakeEventLog) {
            if (!fakeCrossingsByVehicle.containsKey(crossing.getVehicle())) {
                fakeCrossingsByVehicle.put(crossing.getVehicle(), new ArrayList<>());
            }
            fakeCrossingsByVehicle.get(crossing.getVehicle()).add(crossing);
        }
        context.checking(new Expectations() {{
            exactly(1).of(operationsTeam).triggerInvestigationInto(vehicle);
        }});
        calculatorSystem.calculateCharges(fakeCrossingsByVehicle);
    }

    @Test
    public void insufficientFundsTriggersPenalty() {
        BigDecimal charge = new BigDecimal(1000000);

        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");

        context.checking(new Expectations() {{
            exactly(1).of(operationsTeam).issuePenaltyNotice(vehicle, charge);
        }});

        calculatorSystem.makeChargeTo(vehicle, charge);
    }

    @Test
    public void unregisteredAccountTriggersPenalty() {
        BigDecimal charge = new BigDecimal(1);

        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");

        context.checking(new Expectations() {{
            exactly(1).of(operationsTeam).issuePenaltyNotice(vehicle, charge);
        }});

        calculatorSystem.makeChargeTo(vehicle, charge);
    }

}
