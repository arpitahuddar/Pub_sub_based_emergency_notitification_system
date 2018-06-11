package com.ssa.ens.utils.simulator;

import com.ssa.ens.lambda.handler.ProcessHelpResponseHandler;

public class CreateHelpRequestTest {

    public static void main(String[] args) {
        //RequestHelpHandler createHelpTest=new RequestHelpHandler();
        //System.out.print(createHelpTest.createHelpRequest(1,1));
        ProcessHelpResponseHandler Help=new ProcessHelpResponseHandler();
        Help.updateBestVolunteerId(1,527811267);
    }
}
