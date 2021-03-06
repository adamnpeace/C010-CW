package com.trafficmon;

import org.junit.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CheckSystemTest {
    CheckSystem checkSystem = new CheckSystem();
    @Test
    public void exitBeforeEntryReturnsTimeStampErrorCode() {
        List<ZoneBoundaryCrossing> crossings = new ArrayList<>();
        crossings.add(new EntryEvent(Vehicle.withRegistration("A123 4NP")));
        crossings.add(new ExitEvent(Vehicle.withRegistration("A123 4NP")));
        crossings.get(0).setNewTimestamp(LocalTime.of(12, 00));
        crossings.get(1).setNewTimestamp(LocalTime.of(11, 00));
        assertThat(checkSystem.getTypeOfOrdering(crossings), is(1));
    }

    @Test
    public void twoEntryEventsReturnsDoubleEntryErrorCode() {
        List<ZoneBoundaryCrossing> crossings = new ArrayList<>();
        crossings.add(new EntryEvent(Vehicle.withRegistration("A123 4NP")));
        crossings.add(new EntryEvent(Vehicle.withRegistration("A123 4NP")));
        assertThat(checkSystem.getTypeOfOrdering(crossings), is(2));
    }

    @Test
    public void twoExitEventsReturnsDoubleExitErrorCode() {
        List<ZoneBoundaryCrossing> crossings = new ArrayList<>();
        crossings.add(new ExitEvent(Vehicle.withRegistration("A123 4NP")));
        crossings.add(new ExitEvent(Vehicle.withRegistration("A123 4NP")));
        assertThat(checkSystem.getTypeOfOrdering(crossings), is(3));
    }

    @Test
    public void correctOrderReturnsNoErrorCode() {
        List<ZoneBoundaryCrossing> crossings = new ArrayList<>();
        crossings.add(new EntryEvent(Vehicle.withRegistration("A123 4NP")));
        crossings.add(new ExitEvent(Vehicle.withRegistration("A123 4NP")));
        assertThat(checkSystem.getTypeOfOrdering(crossings), is(0));
    }
}
