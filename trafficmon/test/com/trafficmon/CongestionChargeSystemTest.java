package com.trafficmon;

import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;

import java.time.*;

public class CongestionChargeSystemTest {


    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    PenaltiesService penaltiesService = context.mock(PenaltiesService.class);

    CongestionChargeSystem congestionChargeSystem = new CongestionChargeSystem(penaltiesService);

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
    public void beforeTwoLessThanFourHrsChargesSixPounds() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        BigDecimal expectedCharge = new BigDecimal(6);
        LocalTime entryTime = LocalTime.of(13, 00);
        LocalTime exitTime = LocalTime.of(16, 59);
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.getEventLog().get(0).setNewTimestamp(entryTime);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLog().get(1).setNewTimestamp(exitTime);
        BigDecimal calculatedCharge = congestionChargeSystem.getCalculateCharges(congestionChargeSystem.getEventLog());
        assertThat(calculatedCharge, is(expectedCharge));
    }

    @Test
    public void afterTwoLessThanFourHrsChargesFourPounds() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        BigDecimal expectedCharge = new BigDecimal(4);
        LocalTime entryTime = LocalTime.of(15, 00);
        LocalTime exitTime = LocalTime.of(18, 59);
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.getEventLog().get(0).setNewTimestamp(entryTime);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLog().get(1).setNewTimestamp(exitTime);
        BigDecimal calculatedCharge = congestionChargeSystem.getCalculateCharges(congestionChargeSystem.getEventLog());
        assertThat(calculatedCharge, is(expectedCharge));
    }

    @Test
    public void beforeTwoMoreThanFourHrsChargesTwelvePounds() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        BigDecimal expectedCharge = new BigDecimal(12);
        LocalTime entryTime = LocalTime.of(13, 00);
        LocalTime exitTime = LocalTime.of(17, 01);
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.getEventLog().get(0).setNewTimestamp(entryTime);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLog().get(1).setNewTimestamp(exitTime);
        BigDecimal calculatedCharge = congestionChargeSystem.getCalculateCharges(congestionChargeSystem.getEventLog());
        assertThat(calculatedCharge, is(expectedCharge));
    }

    @Test
    public void afterTwoMoreThanFourHrsChargesTwelvePounds() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        BigDecimal expectedCharge = new BigDecimal(12);
        LocalTime entryTime = LocalTime.of(15, 00);
        LocalTime exitTime = LocalTime.of(19, 01);
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.getEventLog().get(0).setNewTimestamp(entryTime);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLog().get(1).setNewTimestamp(exitTime);
        BigDecimal calculatedCharge = congestionChargeSystem.getCalculateCharges(congestionChargeSystem.getEventLog());
        assertThat(calculatedCharge, is(expectedCharge));
    }

    @Test
    public void exitBeforeEntryReturnsTimeStampErrorCode() {
        List<ZoneBoundaryCrossing> crossings = new ArrayList<ZoneBoundaryCrossing>();
        crossings.add(new EntryEvent(Vehicle.withRegistration("A123 4NP")));
        crossings.add(new ExitEvent(Vehicle.withRegistration("A123 4NP")));
        crossings.get(0).setNewTimestamp(LocalTime.of(2, 00));
        crossings.get(1).setNewTimestamp(LocalTime.of(1, 00));
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
        context.checking(new Expectations() {{
            exactly(1).of(penaltiesService).triggerInvestigationInto(vehicle);
        }});

        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLog().get(0).setNewTimestamp(LocalTime.of(2, 00));
        congestionChargeSystem.getEventLog().get(1).setNewTimestamp(LocalTime.of(1, 00));
        congestionChargeSystem.calculateCharges();

    }

    @Test
    public void insufficientFundsTriggersPenalty() {
        final BigDecimal penaltyCharge = new BigDecimal(600);
        Clock now = Clock.fixed(LocalDateTime.of(2018, 1, 1, 11, 00).toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");

        context.checking(new Expectations() {{
            exactly(1).of(penaltiesService).issuePenaltyNotice(vehicle, penaltyCharge);
        }});
        int i = 100;
        while ( i>0) {
            congestionChargeSystem.vehicleEnteringZone(vehicle);
            congestionChargeSystem.vehicleLeavingZone(vehicle);
            i--;
        }
        congestionChargeSystem.calculateCharges();
        //assert(false); // Fix problem with clock


    }

    @Test
    public void unregisteredAccountTriggersPenalty() {
        final BigDecimal penaltyCharge = new BigDecimal(6);
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");

        context.checking(new Expectations() {{
            exactly(1).of(penaltiesService).issuePenaltyNotice(vehicle, penaltyCharge);
        }});

        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLog().get(0).setNewTimestamp(LocalTime.of(2, 00));
        congestionChargeSystem.getEventLog().get(1).setNewTimestamp(LocalTime.of(3, 00));
        congestionChargeSystem.calculateCharges();
    }

}