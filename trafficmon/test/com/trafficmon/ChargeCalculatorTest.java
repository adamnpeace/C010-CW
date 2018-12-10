package com.trafficmon;

import org.junit.Rule;
import org.junit.Test;

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
    public PenaltiesService operationsTeam = context.mock(PenaltiesService.class);
    public Checker checker = new Checker();
    public ChargeCalculator chargeCalculator = new ChargeCalculator(operationsTeam, checker);
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
    }*/


/*


    @Test
    public void exitBeforeEntryTriggersInvestigation() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");

        context.checking(new Expectations() {{
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

        context.checking(new Expectations() {{
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

        context.checking(new Expectations() {{
            exactly(1).of(operationsTeam).issuePenaltyNotice(vehicle, expectedPenalty);
        }});

        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLogElem(0).setNewTimestamp(0);
        congestionChargeSystem.getEventLogElem(1).setNewTimestamp(10);
        congestionChargeSystem.calculateCharges();
    }*/
}