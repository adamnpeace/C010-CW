package com.trafficmon;

import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalTime;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import org.jmock.integration.junit4.JUnitRuleMockery;

public class CongestionChargeSystemTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    PenaltiesService operationsTeam = context.mock(PenaltiesService.class);
    CheckSystem checkSystem = new CheckSystem();
    CalculatorSystem calculatorSystem = new CalculatorSystem(operationsTeam, checkSystem);
    ICongestionChargeSystem congestionChargeSystem = new CongestionChargeSystem(operationsTeam, checkSystem, calculatorSystem);

    @Test
    public void vehicleEnteringAndLeavingZoneCreatesTwoEvents() {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        assertThat(congestionChargeSystem.getEventLogSize(), is(2));
    }

    @Test
    public void vehicleEnteringZoneAddsEntryEvent() {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        assertTrue(congestionChargeSystem.getEventLogElem(0) instanceof EntryEvent);
    }

    @Test
    public void vehicleLeavingZoneAddsExitEvent() {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        assertTrue(congestionChargeSystem.getEventLogElem(1) instanceof ExitEvent);
    }

    @Test
    public void notPreviouslyRegisteredVehicleAddsNoEvent() {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        assertThat(congestionChargeSystem.getEventLogSize(), is(0));
    }
    /*
    @Test
    public void eventChargeForEntryAndExit() {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        BigDecimal expectedCharge = new BigDecimal(8.3500);
        MathContext precision = new MathContext(5);
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.getEventLogElem(0).setNewTimestamp(0);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLogElem(1).setNewTimestamp(10000000);
        BigDecimal calculatedCharge = calculatorSystem.getCalculateCharges(
                congestionChargeSystem.getEventLogElem(0),
                congestionChargeSystem.getEventLogElem(1));
        assertThat(calculatedCharge.round(precision), is(expectedCharge.round(precision)));
    }*/

    @Test
    public void beforeTwoLessThanFourHrsChargesSixPounds() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        int expectedCharge = 6;
        LocalTime entryTime = LocalTime.of(13, 59);
        LocalTime exitTime = LocalTime.of(17, 58);
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.getEventLogElem(0).setNewTimestamp(entryTime);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLogElem(1).setNewTimestamp(exitTime);
        int calculatedCharge = calculatorSystem.getCalculateCharges(congestionChargeSystem.getEventLogElem(0), congestionChargeSystem.getEventLogElem(1));
        assertThat(calculatedCharge, is(expectedCharge));
    }

    @Test
    public void afterTwoLessThanFourHrsChargesFourPounds() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        int expectedCharge = 4;
        LocalTime entryTime = LocalTime.of(14, 01);
        LocalTime exitTime = LocalTime.of(18, 00);
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.getEventLogElem(0).setNewTimestamp(entryTime);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLogElem(1).setNewTimestamp(exitTime);
        int calculatedCharge = calculatorSystem.getCalculateCharges(congestionChargeSystem.getEventLogElem(0), congestionChargeSystem.getEventLogElem(1));
        assertThat(calculatedCharge, is(expectedCharge));
    }

    @Test
    public void beforeTwoMoreThanFourHrsChargesTwelvePounds() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        int expectedCharge = 12;
        LocalTime entryTime = LocalTime.of(13, 59);
        LocalTime exitTime = LocalTime.of(18, 00);
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.getEventLogElem(0).setNewTimestamp(entryTime);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLogElem(1).setNewTimestamp(exitTime);
        int calculatedCharge = calculatorSystem.getCalculateCharges(congestionChargeSystem.getEventLogElem(0), congestionChargeSystem.getEventLogElem(1));
        assertThat(calculatedCharge, is(expectedCharge));
    }

    @Test
    public void afterTwoMoreThanFourHrsChargesTwelvePounds() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        int expectedCharge = 12;
        LocalTime entryTime = LocalTime.of(14, 01);
        LocalTime exitTime = LocalTime.of(18, 01);
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.getEventLogElem(0).setNewTimestamp(entryTime);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLogElem(1).setNewTimestamp(exitTime);
        int calculatedCharge = calculatorSystem.getCalculateCharges(congestionChargeSystem.getEventLogElem(0), congestionChargeSystem.getEventLogElem(1));
        assertThat(calculatedCharge, is(expectedCharge));
    }

    @Test
    public void eventChargeForEntryAndExitTwoVehicles() {
        Vehicle vehicle1 = Vehicle.withRegistration("A123 4NP");
        Vehicle vehicle2 = Vehicle.withRegistration("S123 4EF");
        int expectedCharge = 10;
        LocalTime entryTime1 = LocalTime.of(14, 01);
        LocalTime exitTime1 = LocalTime.of(18, 00);
        LocalTime entryTime2 = LocalTime.of(13, 59);
        LocalTime exitTime2 = LocalTime.of(17, 58);
        congestionChargeSystem.vehicleEnteringZone(vehicle1);
        congestionChargeSystem.getEventLogElem(0).setNewTimestamp(entryTime1);
        congestionChargeSystem.vehicleEnteringZone(vehicle2);
        congestionChargeSystem.getEventLogElem(1).setNewTimestamp(entryTime2);
        congestionChargeSystem.vehicleLeavingZone(vehicle1);
        congestionChargeSystem.getEventLogElem(2).setNewTimestamp(exitTime1);
        congestionChargeSystem.vehicleLeavingZone(vehicle2);
        congestionChargeSystem.getEventLogElem(3).setNewTimestamp(exitTime2);
        int calculatedChargeVehicle1 = calculatorSystem.getCalculateCharges(
                congestionChargeSystem.getEventLogElem(0),
                congestionChargeSystem.getEventLogElem(2));
        int calculatedChargeVehicle2 = calculatorSystem.getCalculateCharges(
                congestionChargeSystem.getEventLogElem(1),
                congestionChargeSystem.getEventLogElem(3));
        assertThat(calculatedChargeVehicle1+calculatedChargeVehicle2, is(expectedCharge));

    }
}