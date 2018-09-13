package com.ssa.ens.utils.simulator;

import com.ssa.ens.lambda.handler.DatabaseConnect;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VolunteerSimulator2
{

    private List<Volunteer> volunteers;

    public VolunteerSimulator2(List<Volunteer> volunteers)
    {
        this.volunteers = volunteers;
    }

    public void simulate()
    {
        volunteers.stream().forEach(v -> v.start());
        try {
            Thread.sleep(300000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            volunteers.stream().forEach(v -> v.stop());
            e.printStackTrace();
        }

    }


    public static void main(String[] args)
    {
        DatabaseConnect connect=new DatabaseConnect();
        connect.reset();
        Volunteer volunteer1 = new Volunteer("Adam", 1, new String[]{"blood_donation","cardiac_arrest","leg_fracture","fatigue","slip_n_fall","arm_dislocation","seizure","asthma"},
                23.45, 0.12);
        Volunteer volunteer2 = new Volunteer("Bill", 2, new String[]{"hypoglycemia"},
                111.23, 19.23);
        Volunteer volunteer3 = new Volunteer("Claire", 3, new String[]{"hypoglycemia","fatigue","slip_n_fall","arm_disloaction","seizure","choke"},
                0.23, 67.23);
        Volunteer volunteer4 = new Volunteer("Damon", 4, new String[]{"arm_disloaction","seizure","choke"},
                89.23, 121.23);
        Volunteer volunteer5 = new Volunteer("Erica", 5, new String[]{"blood_donation"},
                111.2, 346.7);

        new VolunteerSimulator(Arrays.asList(new Volunteer[]{volunteer1 ,volunteer2, /*volunteer3, volunteer4,*/ volunteer5})).simulate();
        System.exit(0);
    }
}
