package com.trafficmon;

import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import org.jmock.integration.junit4.JUnitRuleMockery;

public class CongestionChargeSystemTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    public CheckerInterface checkerInterface = new Checker();
    public PenaltiesService operationsTeam = context.mock(PenaltiesService.class);

    public ChargeCalculator chargeCalculator = new ChargeCalculator(checkerInterface);
    public CongestionChargeSystem congestionChargeSystem = new CongestionChargeSystem(chargeCalculator);

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
        BigDecimal calculatedCharge = chargeCalculator.calculateCharges(congestionChargeSystem.getHashMap());
        assertThat(calculatedCharge.round(precision), is(expectedCharge.round(precision)));
    }

    @Test
    public void eventChargeForEntryAndExitTwoVehicles() {
        Vehicle vehicle1 = Vehicle.withRegistration("A123 4NP");
        Vehicle vehicle2 = Vehicle.withRegistration("S123 4EF");
        BigDecimal expectedCharge = new BigDecimal(33.4);
        MathContext precision = new MathContext(5);
        congestionChargeSystem.vehicleEnteringZone(vehicle1);
        congestionChargeSystem.getEventLogElem(0).setNewTimestamp(0);
        congestionChargeSystem.vehicleEnteringZone(vehicle2);
        congestionChargeSystem.getEventLogElem(1).setNewTimestamp(10000000);
        congestionChargeSystem.vehicleLeavingZone(vehicle1);
        congestionChargeSystem.getEventLogElem(2).setNewTimestamp(20000000);
        congestionChargeSystem.vehicleLeavingZone(vehicle2);
        congestionChargeSystem.getEventLogElem(3).setNewTimestamp(30000000);
        BigDecimal calculatedChargeVehicle1 = congestionChargeSystem.calculateCharges(
                congestionChargeSystem.getEventLogElem(0),
                congestionChargeSystem.getEventLogElem(2));
        BigDecimal calculatedChargeVehicle2 = congestionChargeSystem.calculateCharges(
                congestionChargeSystem.getEventLogElem(1),
                congestionChargeSystem.getEventLogElem(3));
        assertThat(calculatedChargeVehicle1.add(calculatedChargeVehicle2).round(precision), is(expectedCharge.round(precision)));
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

        context.checkerInterface(new Expectations() {{
            exactly(1).of(operationsTeam).triggerInvestigationInto(vehicle);
        }});

        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLogElem(0).setNewTimestamp(1000);
        congestionChargeSystem.getEventLogElem(1).setNewTimestamp(0);
        congestionChargeSystem.calculateCharges();

    }

    @Test
    public void insufficientFundsTriggersPenalty() {
        BigDecimal expectedPenalty = new BigDecimal(Math.ceil((1000000000) / (1000.0 * 60.0)))
                .multiply(congestionChargeSystem.CHARGE_RATE_POUNDS_PER_MINUTE);

        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");

        context.checkerInterface(new Expectations() {{
            exactly(1).of(operationsTeam).issuePenaltyNotice(vehicle, expectedPenalty);
        }});

        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLogElem(0).setNewTimestamp(0);
        congestionChargeSystem.getEventLogElem(1).setNewTimestamp(1000000000);
        congestionChargeSystem.calculateCharges();
    }

    @Test
    public void unregisteredAccountTriggersPenalty() {
        BigDecimal expectedPenalty = new BigDecimal(Math.ceil((10) / (1000.0 * 60.0)))
                .multiply(congestionChargeSystem.CHARGE_RATE_POUNDS_PER_MINUTE);

        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");

        context.checkerInterface(new Expectations() {{
            exactly(1).of(operationsTeam).issuePenaltyNotice(vehicle, expectedPenalty);
        }});

        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLogElem(0).setNewTimestamp(0);
        congestionChargeSystem.getEventLogElem(1).setNewTimestamp(10);
        congestionChargeSystem.calculateCharges();
    }
*/
}