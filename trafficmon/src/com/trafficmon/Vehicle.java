package com.trafficmon;

public class Vehicle {

    private final String registration;
    
    private final boolean isElectric;

    private Vehicle(String registration, boolean isElectric) {
        this.registration = registration;
        this.isElectric = isElectric;
    }
    
    public boolean isElectric() {
        return isElectric;
    }

    public static Vehicle withRegistration(String registration) {
        return new Vehicle(registration);
    }

    @Override
    public String toString() {
        return "Vehicle [" + registration + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vehicle vehicle = (Vehicle) o;

        return registration != null ? registration.equals(vehicle.registration) : vehicle.registration == null;
    }

    @Override
    public int hashCode() {
        return registration != null ? registration.hashCode() : 0;
    }

    /*
    ######################
    TESTING
    ######################
     */
    public String getRegistration() {
        return registration;
    }
}
