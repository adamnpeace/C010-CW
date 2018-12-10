package com.trafficmon;

import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;

public class ChargeCalculatorTest {
    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    public CheckerInterface checkerInterface = context.mock(CheckerInterface.class);
    public PenaltiesService operationsTeam = context.mock(PenaltiesService.class);

    public ChargeCalculator chargeCalculator = new ChargeCalculator(checkerInterface, operationsTeam);

    @Test
    public void calculateChargeForTimeInZoneReturnsCorrectVal() throws AccountNotRegisteredException, InsufficientCreditException {
        Vehicle vehicle1 = Vehicle.withRegistration("J091 4PY");
        Vehicle vehicle2 = Vehicle.withRegistration("A123 XYZ");

        BigDecimal expectedCharge = new BigDecimal(16.7);
        MathContext precision = new MathContext(5);
        List<ZoneBoundaryCrossing> fakeEventLog = new ArrayList<>();

        ZoneBoundaryCrossing crossing1v1 = new EntryEvent(vehicle1);
        crossing1v1.setNewTimestamp(0);
        ZoneBoundaryCrossing crossing2v1 = new ExitEvent(vehicle1);
        crossing2v1.setNewTimestamp(10000000);
        fakeEventLog.add(crossing1v1);
        fakeEventLog.add(crossing2v1);

        ZoneBoundaryCrossing crossing1v2 = new EntryEvent(vehicle2);
        crossing1v2.setNewTimestamp(10000000);
        ZoneBoundaryCrossing crossing2v2 = new ExitEvent(vehicle2);
        crossing2v2.setNewTimestamp(20000000);
        fakeEventLog.add(crossing1v2);
        fakeEventLog.add(crossing2v2);


        BigDecimal actualCharge = chargeCalculator.getCalculateCharges(fakeEventLog);

        assertThat(actualCharge.round(precision), is(expectedCharge.round(precision)));
    }

    @Test
    public void calculateChargesReturnsCorrectVal() throws AccountNotRegisteredException, InsufficientCreditException {
        Vehicle vehicle1 = Vehicle.withRegistration("J091 4PY");
        Vehicle vehicle2 = Vehicle.withRegistration("A123 XYZ");

        BigDecimal expectedCharge = new BigDecimal(16.7);
        MathContext precision = new MathContext(5);
        List<ZoneBoundaryCrossing> fakeEventLog = new ArrayList<>();

        ZoneBoundaryCrossing crossing1v1 = new EntryEvent(vehicle1);
        crossing1v1.setNewTimestamp(0);
        ZoneBoundaryCrossing crossing2v1 = new ExitEvent(vehicle1);
        crossing2v1.setNewTimestamp(10000000);
        fakeEventLog.add(crossing1v1);
        fakeEventLog.add(crossing2v1);

        ZoneBoundaryCrossing crossing1v2 = new EntryEvent(vehicle2);
        crossing1v2.setNewTimestamp(10000000);
        ZoneBoundaryCrossing crossing2v2 = new ExitEvent(vehicle2);
        crossing2v2.setNewTimestamp(20000000);
        fakeEventLog.add(crossing1v2);
        fakeEventLog.add(crossing2v2);


        BigDecimal charge = chargeCalculator.getCalculateCharges(fakeEventLog);
        Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicle = new HashMap<>();

        crossingsByVehicle.put(vehicle1, fakeEventLog);
        crossingsByVehicle.put(vehicle2, fakeEventLog);

        assertThat(charge.round(precision), is(expectedCharge.round(precision)));
    }

    @Test
    public void correctOrderingCausesCharge() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        BigDecimal fakeCharge = new BigDecimal(1);

        context.checking(new Expectations() {{
            exactly(0).of(operationsTeam).issuePenaltyNotice(vehicle, fakeCharge);
        }});

        chargeCalculator.executeCharge(vehicle, fakeCharge);
    }

    @Test
    public void insufficientCreditCausesPenalty() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        BigDecimal fakeCharge = new BigDecimal(100000000);

        context.checking(new Expectations() {{
            exactly(1).of(operationsTeam).issuePenaltyNotice(vehicle, fakeCharge);
        }});

        chargeCalculator.executeCharge(vehicle, fakeCharge);
    }

    @Test
    public void unregisteredAccountCausesPenalty() {
        Vehicle vehicle = Vehicle.withRegistration("ABCDEF");
        BigDecimal fakeCharge = new BigDecimal(1);

        context.checking(new Expectations() {{
            exactly(1).of(operationsTeam).issuePenaltyNotice(vehicle, fakeCharge);
        }});

        chargeCalculator.executeCharge(vehicle, fakeCharge);
    }

/*
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
    }*/


/*


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
    }*/
}
