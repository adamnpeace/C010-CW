package com.trafficmon;

import org.jmock.Expectations;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import org.jmock.integration.junit4.JUnitRuleMockery;

public class CongestionChargeSystemTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(stream);
    PrintStream systemOut = System.out;
    PenaltiesService operationsTeam = context.mock(PenaltiesService.class);
    CheckSystem checkSystem = new CheckSystem();
    AccountsService accountsService = context.mock(AccountsService.class);
    CalculatorSystem calculatorSystem = new CalculatorSystem(operationsTeam, checkSystem, accountsService);
    CongestionChargeSystem congestionChargeSystem = new CongestionChargeSystem(operationsTeam, checkSystem, calculatorSystem);

    // Following 2 methods redirect the console log to a bytearray that can be accessed
    // throughout the code.
    @Before
    public void redirectOutToStream() {
        System.setOut(printStream);
    }

    @After
    public void redirectOutToConsole() {
        System.out.flush();
        System.setOut(systemOut);
        System.out.println("Console says: " + stream.toString());
    }

    /*
        ######################
        Events
        ######################
    */
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
        ######################
        Correct Single Entries
        ######################
    */
    @Test
    public void beforeTwoLessThanFourHrsChargesSixPounds() throws AccountNotRegisteredException {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        String expectedOutput = new String("Charge made to account of Adam Peace, £6.00 deducted, balance: £494.00\n");
        LocalTime entryTime = LocalTime.of(13, 00);
        LocalTime exitTime = LocalTime.of(16, 00);

        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.vehicleLeavingZone(vehicle);

        congestionChargeSystem.getEventLogElem(0).setNewTimestamp(entryTime);
        congestionChargeSystem.getEventLogElem(1).setNewTimestamp(exitTime);

        final Account account = new Account("Adam Peace", Vehicle.withRegistration("A123 4NP"), new BigDecimal(500));

        context.checking(new Expectations() {{
            allowing(accountsService).accountFor(vehicle); will(returnValue(account));
        }});

        congestionChargeSystem.calculateCharges();
        assertThat(stream.toString(), is(expectedOutput));
    }

    @Test
    public void afterTwoLessThanFourHrsChargesFourPounds() throws AccountNotRegisteredException {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        String expectedOutput = new String("Charge made to account of Adam Peace, £4.00 deducted, balance: £496.00\n");
        LocalTime entryTime = LocalTime.of(15, 00);
        LocalTime exitTime = LocalTime.of(18, 00);

        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.vehicleLeavingZone(vehicle);

        congestionChargeSystem.getEventLogElem(0).setNewTimestamp(entryTime);
        congestionChargeSystem.getEventLogElem(1).setNewTimestamp(exitTime);

        final Account account = new Account("Adam Peace", Vehicle.withRegistration("A123 4NP"), new BigDecimal(500));

        context.checking(new Expectations() {{
            allowing(accountsService).accountFor(vehicle); will(returnValue(account));
        }});

        congestionChargeSystem.calculateCharges();
        assertThat(stream.toString(), is(expectedOutput));
    }

    @Test
    public void beforeTwoMoreThanFourHrsChargesTwelvePounds() throws AccountNotRegisteredException {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        String expectedOutput = new String("Charge made to account of Adam Peace, £12.00 deducted, balance: £488.00\n");
        LocalTime entryTime = LocalTime.of(13, 00);
        LocalTime exitTime = LocalTime.of(18, 00);

        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.vehicleLeavingZone(vehicle);

        congestionChargeSystem.getEventLogElem(0).setNewTimestamp(entryTime);
        congestionChargeSystem.getEventLogElem(1).setNewTimestamp(exitTime);

        final Account account = new Account("Adam Peace", Vehicle.withRegistration("A123 4NP"), new BigDecimal(500));

        context.checking(new Expectations() {{
            allowing(accountsService).accountFor(vehicle); will(returnValue(account));
        }});

        congestionChargeSystem.calculateCharges();
        assertThat(stream.toString(), is(expectedOutput));
    }

    @Test
    public void afterTwoMoreThanFourHrsChargesTwelvePounds() throws AccountNotRegisteredException {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        String expectedOutput = new String("Charge made to account of Adam Peace, £12.00 deducted, balance: £488.00\n");
        LocalTime entryTime = LocalTime.of(14, 00);
        LocalTime exitTime = LocalTime.of(19, 00);

        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.vehicleLeavingZone(vehicle);

        congestionChargeSystem.getEventLogElem(0).setNewTimestamp(entryTime);
        congestionChargeSystem.getEventLogElem(1).setNewTimestamp(exitTime);

        final Account account = new Account("Adam Peace", Vehicle.withRegistration("A123 4NP"), new BigDecimal(500));

        context.checking(new Expectations() {{
            allowing(accountsService).accountFor(vehicle); will(returnValue(account));
        }});

        congestionChargeSystem.calculateCharges();
        assertThat(stream.toString(), is(expectedOutput));
    }

    /*
        ######################
        Correct Re-entries
        ######################
    */

    @Test
    public void beforeTwoReentryWithinFourHoursChargesSixPounds() throws AccountNotRegisteredException {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        String expectedOutput = new String("Charge made to account of Adam Peace, £6.00 deducted, balance: £494.00\n");
        LocalTime entryTime1 = LocalTime.of(13, 00);
        LocalTime exitTime1 = LocalTime.of(14, 00);
        LocalTime entryTime2 = LocalTime.of(15, 00);
        LocalTime exitTime2 = LocalTime.of(16, 00);
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.getEventLogElem(0).setNewTimestamp(entryTime1);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLogElem(1).setNewTimestamp(exitTime1);
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.getEventLogElem(2).setNewTimestamp(entryTime2);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLogElem(3).setNewTimestamp(exitTime2);

        final Account account = new Account("Adam Peace", Vehicle.withRegistration("A123 4NP"), new BigDecimal(500));

        context.checking(new Expectations() {{
        allowing(accountsService).accountFor(vehicle); will(returnValue(account));
    }});
        congestionChargeSystem.calculateCharges();
    assertThat(stream.toString(), is(expectedOutput));
}

    @Test
    public void afterTwoReentryWithinFourHoursChargesFourPounds() throws AccountNotRegisteredException {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        String expectedOutput = new String("Charge made to account of Adam Peace, £4.00 deducted, balance: £496.00\n");
        LocalTime entryTime1 = LocalTime.of(14, 00);
        LocalTime exitTime1 = LocalTime.of(15, 00);
        LocalTime entryTime2 = LocalTime.of(16, 00);
        LocalTime exitTime2 = LocalTime.of(17, 00);
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.getEventLogElem(0).setNewTimestamp(entryTime1);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLogElem(1).setNewTimestamp(exitTime1);
        congestionChargeSystem.vehicleEnteringZone(vehicle);
        congestionChargeSystem.getEventLogElem(2).setNewTimestamp(entryTime2);
        congestionChargeSystem.vehicleLeavingZone(vehicle);
        congestionChargeSystem.getEventLogElem(3).setNewTimestamp(exitTime2);

        final Account account = new Account("Adam Peace", Vehicle.withRegistration("A123 4NP"), new BigDecimal(500));

        context.checking(new Expectations() {{
            allowing(accountsService).accountFor(vehicle); will(returnValue(account));
        }});
        congestionChargeSystem.calculateCharges();
        assertThat(stream.toString(), is(expectedOutput));
    }
}