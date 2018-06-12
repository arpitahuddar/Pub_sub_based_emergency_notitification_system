package com.ssa.ens.utils;

import com.ssa.ens.lambda.handler.ProcessHelpResponseHandler;
import com.ssa.ens.lambda.handler.RequestHelpHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessHelpResponseHandlerTest {

    public static void main(String[] args) {
        ProcessHelpResponseHandler processHelpTest=new ProcessHelpResponseHandler();
        List<Map<String,Object>> responseTest=new ArrayList<>();
        Map<String,Object> volunteer1=new HashMap<>();
        volunteer1.put("volunteerUserId",500);
        volunteer1.put("lat",123.14);
        volunteer1.put("lon",342.12);
        responseTest.add(volunteer1);
        Map<String,Object> volunteer2=new HashMap<>();
        volunteer2.put("volunteerUserId",100);
        volunteer2.put("lat",0.9235);
        volunteer2.put("lon",33.897869);
        responseTest.add(volunteer2);
        processHelpTest.updateVolunteer(processHelpTest.orderedVolunteerByClosestDistance(responseTest,0.9896,33.675),468882402);
    }
}
