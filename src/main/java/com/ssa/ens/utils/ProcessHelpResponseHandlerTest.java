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
        volunteer1.put("volunteerUserId",1);
        volunteer1.put("lat",0.8869696);
        volunteer1.put("lon",34.897869);
        responseTest.add(volunteer1);
        Map<String,Object> volunteer2=new HashMap<>();
        volunteer2.put("volunteerUserId",2);
        volunteer2.put("lat",0.23468);
        volunteer2.put("lon",17.897869);
        responseTest.add(volunteer2);
        processHelpTest.findBestVolunteer(responseTest,0.9896,33.675);

    }
}
