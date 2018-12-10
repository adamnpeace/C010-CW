package com.trafficmon;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
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

    @Test
    public void exitBeforeEntryTriggersInvestigation() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        List<ZoneBoundaryCrossing> fakeEventLog = new ArrayList<>();
        EntryEvent entryEvent = new EntryEvent(vehicle);
        ExitEvent exitEvent = new ExitEvent(vehicle);
        fakeEventLog.add(entryEvent);
        fakeEventLog.add(exitEvent);
        fakeEventLog.get(0).setNewTimestamp(1000);
        fakeEventLog.get(1).setNewTimestamp(0);
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
