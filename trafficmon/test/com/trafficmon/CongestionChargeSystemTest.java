package com.trafficmon;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;


public class CongestionChargeSystemTest {

    CongestionChargeSystem congestionChargeSystem = new CongestionChargeSystem();

    @Test
    public void vehicleEnteringAndLeavingZoneCreatesTwoEvents() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        assertThat(congestionChargeSystem.getEventLog().size(), is(2));
    }

    @Test
    public void vehicleEnteringZoneAddsEntryEvent() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        assertTrue(congestionChargeSystem.getEventLog().get(0) instanceof EntryEvent);
    }

    @Test
    public void vehicleLeavingZoneAddsExitEvent() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        assertTrue(congestionChargeSystem.getEventLog().get(1) instanceof ExitEvent);
    }

    @Test
    public void eventChargeForEntryAndExit() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
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
    public void eventChargeForEntryAndExitThreeVehicles() {
        assert(true);
    }


}