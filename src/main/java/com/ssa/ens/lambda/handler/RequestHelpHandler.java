package com.ssa.ens.lambda.handler;

import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.ssa.ens.lambda.GatewayResponse;

import java.util.HashMap;
import java.util.Map;

public class RequestHelpHandler implements RequestHandler<Map<String,Object>, GatewayResponse> {
    public GatewayResponse handleRequest(Map<String,Object> input, Context context)
    {
        context.getLogger().log("Received Input: " + input.toString());
        final Integer userId = Integer.valueOf((String) input.get("userId"));
        final Integer topicId = Integer.valueOf((String) input.get("topicId"));
        double lat=Double.valueOf((String) input.get("lat"));
        double lon=Double.valueOf((String) input.get("lon"));

        context.getLogger().log(String.format("Creating help request for user:%d,topicId:%d", userId, topicId));

        long helpRequestId = createHelpRequest(userId, topicId);
        context.getLogger().log(String.format("Created  help request: %d, userId:%d, topicId:%d",
                helpRequestId, userId, topicId));
        final String topicName = getTopicName(topicId);

        context.getLogger().log("invoking asynchronous process help responses handler");
        invokeProcessHelpResponseLambda(context, helpRequestId, topicName,lat,lon);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        return new GatewayResponse(String.format("{\"helpRequestId\":%d}", helpRequestId), headers, 200);
    }


    private String getTopicName(int topicId) {
        // get name from topics table;
        String resultName = "";
        DatabaseConnect topicName = new DatabaseConnect();
        String inputQuery;
        inputQuery=String.format("SELECT Topic_name FROM innodb.Topics WHERE Topic_id=%d",topicId);
        resultName = topicName.getResult(inputQuery);
        return resultName;
    }

    public long createHelpRequest(int userId, int topicId) {
        //create help request in help requests table
        //update the database after retrieving the largest helpID- modify the table.
        //Timestamp current_time = null;
        //Timestamp currentTimestamp = new Timestamp(current_time.getTime());
        //Timestamp expiry_time=null;
        //Timestamp expiry_time = new Timestamp((current_time.getTime()));
        String helpStatus="In Progress";
        String sql;
        sql=String.format("INSERT INTO innodb.Help(User_id, Topic_id, Help_status) values (%d, %d, '%s')", userId, topicId, "in_progress");
        int helpRequestId;
        DatabaseConnect dbconnect = new DatabaseConnect();
        helpRequestId = dbconnect.insertNewHelpRequest(userId,topicId);
        //context.getLogger().log(String.format("Created  help request: %d, userId:%d, topicId:%d",
          //      helpRequestId, userId, topicId));
        return helpRequestId;
        }

    private void invokeProcessHelpResponseLambda(Context context, long helpRequestId, String topicName,double lat,double lon)
    {
        AWSLambdaAsync lambda = AWSLambdaAsyncClientBuilder.defaultClient();
        String payload=String.format("{\"helpRequestId\":%d, \"topicName\": \"%s\",\"lat\":%f,\"lon\":%f}",
                helpRequestId, topicName,lat,lon);
        InvokeRequest req = new InvokeRequest()
                .withFunctionName("ProcessHelpResponses")
                .withPayload(payload)
                .withInvocationType(InvocationType.Event);
        context.getLogger().log("Invoking process help responses asynchronously."+payload);
        InvokeResult invokeResult = lambda.invoke(req);
        context.getLogger().log("Successfully triggered Async processRequestHandler:" + invokeResult.toString());
    }
}
