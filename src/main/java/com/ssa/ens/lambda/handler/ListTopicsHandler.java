package com.ssa.ens.lambda.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.ssa.ens.dataaccess.TopicsDataAccessObject;
import com.ssa.ens.dataaccess.impl.TopicsDataAccessObjectInMemory;
import com.ssa.ens.lambda.GatewayResponse;
import com.ssa.ens.model.Topic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListTopicsHandler implements RequestHandler<Object, Object>
{
    private static TopicsDataAccessObject TOPICS_DAO = new TopicsDataAccessObjectInMemory();

    public GatewayResponse handleRequest(Object input, Context context)
    {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        context.getLogger().log("Input: " + input);
        return new GatewayResponse(toJson(TOPICS_DAO.getTopics()), headers, 200);
    }

    // TODO better off using Jackson lib to convert pojos to json
    private String toJson(List<Topic> topics)
    {
        StringBuilder response = new StringBuilder("[");
        for (int i = 0; i < topics.size(); i += 1) {
            Topic topic = topics.get(i);
            response.append("{")
                    .append("id:").append(topic.getId())
                    .append(",")
                    .append("name:").append("\"").append(topic.getName()).append("\"")
                    .append("}");
            if (i < topics.size() - 1) {
                response.append(",");
            }
        }
        response.append("]");
        return response.toString();
    }
}
