package com.trafficmon;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import static org.junit.Assert.*;

public class VehicleTest {

    @Test
    public void toStringTest() {
        Vehicle vehicle = Vehicle.withRegistration("J091 4PY");
        assertThat(vehicle.toString(), is("Vehicle [J091 4PY]"));
    }

    @Test
    public void twoSimilarVehiclesEqualEachother() {
        Vehicle vehicleA = Vehicle.withRegistration("A123 4NP");
        Vehicle vehicleB = Vehicle.withRegistration("A123 4NP");
        assertEquals(vehicleA, vehicleB);
    }

    @Test
    public void twoDifferentVehiclesDoNotEqualEachother() {
        Vehicle vehicleA = Vehicle.withRegistration("A123 4NP");
        Vehicle vehicleB = Vehicle.withRegistration("S123 4EF");
        assertNotEquals(vehicleA, vehicleB);
    }

    @Test
    public void nullRegistrationHasHashCodeZero() {
        Vehicle vehicle = Vehicle.withRegistration(null);
        assertThat(vehicle.hashCode(), is(0));
    }

    @Test
    public void notNullRegistrationReturnsHashCode() {
        Vehicle vehicle = Vehicle.withRegistration("A123 4NP");
        int actualHashCode = vehicle.getRegistration().hashCode();
        assertThat(vehicle.hashCode(), is(actualHashCode));
    }
}