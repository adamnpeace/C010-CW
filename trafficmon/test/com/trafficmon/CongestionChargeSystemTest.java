package com.trafficmon;

import org.junit.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;



public class CongestionChargeSystemTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    CongestionChargeSystem congestionChargeSystem = new CongestionChargeSystem();

    @Test
    public void vehicleEnteringAndLeavingZoneCreatesTwoEvents() {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        assertThat(congestionChargeSystem.getEventLog().size(), is(2));
    }

    @Test
    public void vehicleEnteringZoneAddsEntryEvent() {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        assertTrue(congestionChargeSystem.getEventLog().get(0) instanceof EntryEvent);
    }

    @Test
    public void vehicleLeavingZoneAddsExitEvent() {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        assertTrue(congestionChargeSystem.getEventLog().get(1) instanceof ExitEvent);
    }

    @Test
    public void notPreviouslyRegisteredVehicleAddsNoEvent() {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        assertThat(congestionChargeSystem.getEventLog().size(), is(0));
    }

    @Test
    public void eventChargeForEntryAndExit() {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        BigDecimal expectedCharge = new BigDecimal(8.3500);
        MathContext precision = new MathContext(5);
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.getEventLog().get(0).setNewTimestamp(0);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLog().get(1).setNewTimestamp(10000000);
        BigDecimal calculatedCharge = congestionChargeSystem.getCalculateCharges(congestionChargeSystem.getEventLog());
        assertThat(calculatedCharge.round(precision), is(expectedCharge.round(precision)));
    }

    @Test
    public void eventChargeForEntryAndExitTwoVehicles() {
        Vehicle vehicle1 = Vehicle.withRegistration("A123 4NP");
        Vehicle vehicle2 = Vehicle.withRegistration("S123 4EF");
        BigDecimal expectedCharge = new BigDecimal(16.7);
        MathContext precision = new MathContext(5);
        congestionChargeSystem.vehicleEnteringZone(vehicle1);
        congestionChargeSystem.getEventLog().get(0).setNewTimestamp(0);
        congestionChargeSystem.vehicleEnteringZone(vehicle2);
        congestionChargeSystem.getEventLog().get(1).setNewTimestamp(10000000);
        congestionChargeSystem.vehicleLeavingZone(vehicle1);
        congestionChargeSystem.getEventLog().get(2).setNewTimestamp(20000000);
        congestionChargeSystem.vehicleLeavingZone(vehicle2);
        congestionChargeSystem.getEventLog().get(3).setNewTimestamp(30000000);
        BigDecimal calculatedCharge = congestionChargeSystem.getCalculateCharges(congestionChargeSystem.getEventLog());
        assertThat(calculatedCharge.round(precision), is(expectedCharge.round(precision)));
    }

    @Test
    public void exitBeforeEntryReturnsTimeStampErrorCode() {
        List<ZoneBoundaryCrossing> crossings = new ArrayList<ZoneBoundaryCrossing>();
        crossings.add(new EntryEvent(Vehicle.withRegistration("A123 4NP")));
        crossings.add(new ExitEvent(Vehicle.withRegistration("A123 4NP")));
        crossings.get(0).setNewTimestamp(1000);
        crossings.get(1).setNewTimestamp(0);
        assertThat(congestionChargeSystem.getTypeOfOrdering(crossings), is(1));
    }

    @Test
    public void twoEntryEventsReturnsDoubleEntryErrorCode() {
        List<ZoneBoundaryCrossing> crossings = new ArrayList<ZoneBoundaryCrossing>();
        crossings.add(new EntryEvent(Vehicle.withRegistration("A123 4NP")));
        crossings.add(new EntryEvent(Vehicle.withRegistration("A123 4NP")));
        assertThat(congestionChargeSystem.getTypeOfOrdering(crossings), is(2));
    }

    @Test
    public void twoExitEventsReturnsDoubleExitErrorCode() {
        List<ZoneBoundaryCrossing> crossings = new ArrayList<ZoneBoundaryCrossing>();
        crossings.add(new ExitEvent(Vehicle.withRegistration("A123 4NP")));
        crossings.add(new ExitEvent(Vehicle.withRegistration("A123 4NP")));
        assertThat(congestionChargeSystem.getTypeOfOrdering(crossings), is(3));
    }

    @Test
    public void correctOrderReturnsNoErrorCode() {
        List<ZoneBoundaryCrossing> crossings = new ArrayList<ZoneBoundaryCrossing>();
        crossings.add(new EntryEvent(Vehicle.withRegistration("A123 4NP")));
        crossings.add(new ExitEvent(Vehicle.withRegistration("A123 4NP")));
        assertThat(congestionChargeSystem.getTypeOfOrdering(crossings), is(0));
    }

    @Test
    public void exitBeforeEntryTriggersInvestigation() {

        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLog().get(0).setNewTimestamp(1000);
        congestionChargeSystem.getEventLog().get(1).setNewTimestamp(0);
        congestionChargeSystem.calculateCharges();
        assertThat(outContent.toString(), is("Mismatched entries/exits. Triggering investigation into vehicle: Vehicle [J091 4PY]\n"));

    }

    @Test
    public void insufficientFundsTriggersPenalty() {

        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLog().get(0).setNewTimestamp(0);
        congestionChargeSystem.getEventLog().get(1).setNewTimestamp(1000000000);
        congestionChargeSystem.calculateCharges();
        assertThat(outContent.toString(), is("Penalty notice for: Vehicle [J091 4PY]\n"));
    }

    @Test
    public void unregisteredAccountTriggersPenalty() {

        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLog().get(0).setNewTimestamp(0);
        congestionChargeSystem.getEventLog().get(1).setNewTimestamp(10);
        congestionChargeSystem.calculateCharges();
        assertThat(outContent.toString(), is("Penalty notice for: Vehicle [A123 4NP]\n"));
    }



}