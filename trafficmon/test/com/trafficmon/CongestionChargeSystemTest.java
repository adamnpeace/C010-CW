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

public class CongestionChargeSystemTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    PenaltiesService operationsTeam = context.mock(PenaltiesService.class);
    ICongestionChargeSystem ICongestionChargeSystem = new CongestionChargeSystem(operationsTeam, new CheckSystem());

    @Test
    public void vehicleEnteringAndLeavingZoneCreatesTwoEvents() {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        ICongestionChargeSystem.vehicleEnteringZone(vehicle);
        ICongestionChargeSystem.vehicleLeavingZone(vehicle);
        assertThat(ICongestionChargeSystem.getEventLogSize(), is(2));
    }

    @Test
    public void vehicleEnteringZoneAddsEntryEvent() {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        ICongestionChargeSystem.vehicleEnteringZone(vehicle);
        assertTrue(ICongestionChargeSystem.getEventLogElem(0) instanceof EntryEvent);
    }

    @Test
    public void vehicleLeavingZoneAddsExitEvent() {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        ICongestionChargeSystem.vehicleEnteringZone(vehicle);
        ICongestionChargeSystem.vehicleLeavingZone(vehicle);
        assertTrue(ICongestionChargeSystem.getEventLogElem(1) instanceof ExitEvent);
    }

    @Test
    public void notPreviouslyRegisteredVehicleAddsNoEvent() {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        ICongestionChargeSystem.vehicleLeavingZone(vehicle);
        assertThat(ICongestionChargeSystem.getEventLogSize(), is(0));
    }
    
    @Test
    public void eventChargeForEntryAndExit() {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        BigDecimal expectedCharge = new BigDecimal(8.3500);
        MathContext precision = new MathContext(5);
        ICongestionChargeSystem.vehicleEnteringZone(vehicle);
        ICongestionChargeSystem.getEventLogElem(0).setNewTimestamp(0);
        ICongestionChargeSystem.vehicleLeavingZone(vehicle);
        ICongestionChargeSystem.getEventLogElem(1).setNewTimestamp(10000000);
        BigDecimal calculatedCharge = ICongestionChargeSystem.getCalculateCharges(
                ICongestionChargeSystem.getEventLogElem(0),
                ICongestionChargeSystem.getEventLogElem(1));
        assertThat(calculatedCharge.round(precision), is(expectedCharge.round(precision)));
    }

    @Test
    public void eventChargeForEntryAndExitTwoVehicles() {
        Vehicle vehicle1 = Vehicle.withRegistration("A123 4NP");
        Vehicle vehicle2 = Vehicle.withRegistration("S123 4EF");
        BigDecimal expectedCharge = new BigDecimal(33.4);
        MathContext precision = new MathContext(5);
        ICongestionChargeSystem.vehicleEnteringZone(vehicle1);
        ICongestionChargeSystem.getEventLogElem(0).setNewTimestamp(0);
        ICongestionChargeSystem.vehicleEnteringZone(vehicle2);
        ICongestionChargeSystem.getEventLogElem(1).setNewTimestamp(10000000);
        ICongestionChargeSystem.vehicleLeavingZone(vehicle1);
        ICongestionChargeSystem.getEventLogElem(2).setNewTimestamp(20000000);
        ICongestionChargeSystem.vehicleLeavingZone(vehicle2);
        ICongestionChargeSystem.getEventLogElem(3).setNewTimestamp(30000000);
        BigDecimal calculatedChargeVehicle1 = ICongestionChargeSystem.getCalculateCharges(
                ICongestionChargeSystem.getEventLogElem(0),
                ICongestionChargeSystem.getEventLogElem(2));
        BigDecimal calculatedChargeVehicle2 = ICongestionChargeSystem.getCalculateCharges(
                ICongestionChargeSystem.getEventLogElem(1),
                ICongestionChargeSystem.getEventLogElem(3));
        assertThat(calculatedChargeVehicle1.add(calculatedChargeVehicle2).round(precision), is(expectedCharge.round(precision)));
    }

    @Test
    public void exitBeforeEntryTriggersInvestigation() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");

        context.checking(new Expectations() {{
            exactly(1).of(operationsTeam).triggerInvestigationInto(vehicle);
        }});

        ICongestionChargeSystem.vehicleEnteringZone(vehicle);
        ICongestionChargeSystem.vehicleLeavingZone(vehicle);
        ICongestionChargeSystem.getEventLogElem(0).setNewTimestamp(1000);
        ICongestionChargeSystem.getEventLogElem(1).setNewTimestamp(0);
        ICongestionChargeSystem.calculateCharges();

    }

    @Test
    public void insufficientFundsTriggersPenalty() {
        BigDecimal expectedPenalty = new BigDecimal(Math.ceil((1000000000) / (1000.0 * 60.0)))
                .multiply(ICongestionChargeSystem.CHARGE_RATE_POUNDS_PER_MINUTE);

        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");

        context.checking(new Expectations() {{
            exactly(1).of(operationsTeam).issuePenaltyNotice(vehicle, expectedPenalty);
        }});

        ICongestionChargeSystem.vehicleEnteringZone(vehicle);
        ICongestionChargeSystem.vehicleLeavingZone(vehicle);
        ICongestionChargeSystem.getEventLogElem(0).setNewTimestamp(0);
        ICongestionChargeSystem.getEventLogElem(1).setNewTimestamp(1000000000);
        ICongestionChargeSystem.calculateCharges();
    }

    @Test
    public void unregisteredAccountTriggersPenalty() {
        BigDecimal expectedPenalty = new BigDecimal(Math.ceil((10) / (1000.0 * 60.0)))
                .multiply(ICongestionChargeSystem.CHARGE_RATE_POUNDS_PER_MINUTE);

        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");

        context.checking(new Expectations() {{
            exactly(1).of(operationsTeam).issuePenaltyNotice(vehicle, expectedPenalty);
        }});

        ICongestionChargeSystem.vehicleEnteringZone(vehicle);
        ICongestionChargeSystem.vehicleLeavingZone(vehicle);
        ICongestionChargeSystem.getEventLogElem(0).setNewTimestamp(0);
        ICongestionChargeSystem.getEventLogElem(1).setNewTimestamp(10);
        ICongestionChargeSystem.calculateCharges();
    }

}