package com.ssa.ens.utils.simulator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VolunteerSimulator
{

    private List<Volunteer> volunteers;

    public VolunteerSimulator(List<Volunteer> volunteers)
    {
        this.volunteers = volunteers;
    }

    public void simulate()
    {
        volunteers.stream().forEach(v -> v.start());
        try {
            Thread.sleep(600000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            volunteers.stream().forEach(v -> v.stop());
            e.printStackTrace();
        }

    }


    public static void main(String[] args)
    {
        Volunteer volunteer1 = new Volunteer("Brad Pitt", 1000, new String[]{"cardiac_arrest", "blood_donation"},
                -52.124566, -12.12345);
        Volunteer volunteer2 = new Volunteer("Chris Pratt", 500, new String[]{"blood_donation"},
                -23.124566, -43.12345);
        Volunteer volunteer3 = new Volunteer("Matt Daemon", 100, new String[]{"cardiac_arrest"},
                -32.124566, -15.12345);
        Volunteer volunteer4 = new Volunteer("Rajinikanth", 1, new String[]{"cardiac_arrest", "death", "blood_donation"},
                -52.124566, -30.12345);
        Volunteer volunteer5 = new Volunteer("Robert Downey Jr", 4567, new String[]{"t1d-hypoglycemia"},
                -152.124566, -12.12345);

        new VolunteerSimulator(Arrays.asList(new Volunteer[]{volunteer1, volunteer2, volunteer3, volunteer4, volunteer5})).simulate();
        System.exit(0);
    }
}
