package com.ssa.ens.lambda.handler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.ssa.ens.lambda.GatewayResponse;

public class GetHelpHandler implements RequestHandler<Integer, String>{
    public String handleRequest(Integer integer, Context context) {
        return null;
    }



    /* get Help status given a Help id */
}
