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

public class CheckerTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    public PenaltiesService operationsTeam = context.mock(PenaltiesService.class);
    public Checker checker = new Checker();

    @Test
    public void exitBeforeEntryReturnsTimeStampErrorCode() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        List<ZoneBoundaryCrossing> crossings = new ArrayList<>();
        crossings.add(new ExitEvent(vehicle));
        crossings.add(new EntryEvent(vehicle));

        assertThat(checker.getTypeOfOrdering(crossings), is(1));
    }

    @Test
    public void twoEntryEventsReturnsDoubleEntryErrorCode() {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        List<ZoneBoundaryCrossing> crossings = new ArrayList<ZoneBoundaryCrossing>();
        crossings.add(new EntryEvent(vehicle));
        crossings.add(new EntryEvent(vehicle));
        assertThat(checker.getTypeOfOrdering(crossings), is(2));
    }

    @Test
    public void twoExitEventsReturnsDoubleExitErrorCode() {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        List<ZoneBoundaryCrossing> crossings = new ArrayList<ZoneBoundaryCrossing>();
        crossings.add(new ExitEvent(vehicle));
        crossings.add(new ExitEvent(vehicle));
        assertThat(checker.getTypeOfOrdering(crossings), is(3));
    }

    @Test
    public void correctOrderReturnsNoErrorCode() {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        List<ZoneBoundaryCrossing> crossings = new ArrayList<ZoneBoundaryCrossing>();
        crossings.add(new EntryEvent(vehicle));
        crossings.add(new ExitEvent(vehicle));
        assertThat(checker.getTypeOfOrdering(crossings), is(0));
    }
}
