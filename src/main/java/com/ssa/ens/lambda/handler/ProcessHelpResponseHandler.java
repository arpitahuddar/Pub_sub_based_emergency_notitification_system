package com.ssa.ens.lambda.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssa.ens.utils.MQTTMessageReceiver;
import com.ssa.ens.utils.Utils;
import com.ssa.ens.utils.simulator.MQTTMessageHandler;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class ProcessHelpResponseHandler  implements RequestHandler<Map<String, Object>, Object> {


    private static final String MQTT_SERVER_URL = "tcp://ec2-34-220-247-164.us-west-2.compute.amazonaws.com:1883";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Object handleRequest(Map<String, Object> input, Context context) {
        context.getLogger().log("Received Input: " + input.toString());
        final int helpRequestId = (int) input.get("helpRequestId");

        context.getLogger().log("Subscribing help responses.. for help requestId:" + helpRequestId);
        final String helpRequestVolunteerRespTopic = String.format("%d_response", helpRequestId);
        final HelpResponseMessageHandler msgHandler = new HelpResponseMessageHandler(context.getLogger());
        MQTTMessageReceiver msgReceiver = new MQTTMessageReceiver(Utils.MQTT_SERVER_URL, UUID.randomUUID().toString(),
                new String[]{helpRequestVolunteerRespTopic}, msgHandler);
        msgReceiver.subscribe();
        context.getLogger().log("Subscribed to help requestId topic:" + helpRequestVolunteerRespTopic);

        final String topicName = (String) input.get("topicName");
        context.getLogger().log(String.format("Publishing helpRequest:%d to topic:%s", helpRequestId, topicName));
        publishToTopic(helpRequestId, topicName, context);
        context.getLogger().log(String.format("Successfully published helpRequest:%d to topic:%s", helpRequestId, topicName));

        try {
            Thread.sleep(Duration.ofMinutes(1).toMillis());
        } catch (InterruptedException e) {
            context.getLogger().log("Thread interrupted. stopping subscription");
            msgReceiver.quit();
        }
        context.getLogger().log(String.format("Received in total responses:" + msgHandler.getAccumulatedResponses().size()));
        if (msgHandler.getAccumulatedResponses().size() > 0) {

            //Map<String, Object> volunteers = msgHandler.getAccumulatedResponses().iterator().next();// implement find best volunteerId(find the best volunteerId, update the database and return volunteerId id)

            List<Integer> retrievedVolunteerList = orderedVolunteerByClosestDistance(msgHandler.getAccumulatedResponses(), (double) input.get("lat"), (double) input.get("lon"));
            int volunteerId = updateVolunteer(retrievedVolunteerList, helpRequestId);

            context.getLogger().log("Matching volunteers:" + volunteerId);
            if(volunteerId!=-1)
            {
                matchVolunteer(volunteerId, helpRequestId);
            }


        } else {
            context.getLogger().log("No Volunteer responded");
        }
        return null;
    }

    public int updateVolunteer(List<Integer> retrievedVolunteerList, int helpId) {
        DatabaseConnect volunteerUpdate = new DatabaseConnect();
        for (Integer entry : retrievedVolunteerList) {
            if (volunteerUpdate.updateBestVolunteerDb(entry, helpId)) {
                return entry;
            }
        }
        return -1;
    }


    public List<Integer> orderedVolunteerByClosestDistance(List<Map<String,Object>> volunteerResponses, double userLat,double userLong) {
        Iterator<Map<String, Object>> iterator = volunteerResponses.iterator();
        Map<Double,List<Integer>> orderedVolunteerBasedOnDistance=new TreeMap<>();
        List<Integer> listVolunteerID;
        List<Integer> volunteerResult=new ArrayList<>();
        int bestVolunteerID = 0;
        double bestVolunteerDistance = Double.MAX_VALUE;
        double recordedDistance;
        while (iterator.hasNext()) {
            Map<String, Object> volunteerResponse = iterator.next();
            Integer volunteerId = (Integer) (volunteerResponse.get("volunteerUserId"));
            double latitude = (double) volunteerResponse.get("lat");
            double longtitude = (double) volunteerResponse.get("lon");
            recordedDistance = findDistance(latitude, longtitude, userLat, userLong);
            if(!orderedVolunteerBasedOnDistance.containsKey(recordedDistance))
            {
                orderedVolunteerBasedOnDistance.put(recordedDistance, new ArrayList<>());
            }
            listVolunteerID=orderedVolunteerBasedOnDistance.get(recordedDistance);
            listVolunteerID.add(volunteerId);
        }
        for(List<Integer> entry: orderedVolunteerBasedOnDistance.values())
        {
            volunteerResult.addAll(entry);
        }
        return volunteerResult;
    }


    static class HelpResponseMessageHandler implements MQTTMessageHandler
    {
        List<Map<String, Object>> responseAccumulator = new ArrayList<>();
        LambdaLogger logger;

        public  HelpResponseMessageHandler(LambdaLogger logger) {
            this.logger = logger;
        }
        @Override
        public void handleMessage(String topicFilter, MqttMessage msg)
        {
            try {
                logger.log(String.format("Received message, Topic:%s, msg:%s", topicFilter, msg));
                Map<String, Object> payload = MAPPER.reader().forType(Map.class).readValue(msg.getPayload());
                this.responseAccumulator.add(payload);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        List<Map<String, Object>> getAccumulatedResponses()
        {
            return this.responseAccumulator;
        }
    }

    public void matchVolunteer(final int volunteerId, final long helpRequestId)
    {
        {
            try {
                final String topicName = String.format("server-notifications-%d", volunteerId);
                MqttClient client = new MqttClient(Utils.MQTT_SERVER_URL,
                        UUID.randomUUID().toString(), new MemoryPersistence());
                client.connect();
                MqttMessage message = new MqttMessage();
                message.setPayload(String.format("{\"helpRequestId\":%s, " +
                        "\"notificationType\":\"service_help_request_accepted\"" + "}", helpRequestId).getBytes());
                client.publish(topicName, message);
                client.disconnect();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean publishToTopic(long helpRequestId, String topicName, Context context)
    {
        try {
            MqttClient client = new MqttClient(Utils.MQTT_SERVER_URL,
                    UUID.randomUUID().toString(), new MemoryPersistence());
            client.connect();
            MqttMessage message = new MqttMessage();
            message.setPayload(String.format("{\"helpRequestId\":%d}", helpRequestId).getBytes());
            client.publish(topicName, message);
            client.disconnect();
        } catch (final Exception e) {
            context.getLogger().log("Exception raised:" + e.getMessage());
            throw new RuntimeException(e);
        }
        return true;
    }

        public double findDistance(double volLat, double volLong,double userLat,double userLong)
        {

            double distance=Math.sqrt(Math.abs(((volLat-userLat)*(volLat-userLat))-((volLong-userLong)*(volLong-userLong))));
            return distance;
        }

    //User case 1: New volunteer Mapping
    //Step 1: Server: establish connection with clients based on topic -> Help ID
    //Step 2: Server: listen to the clients based on the Help ID(subscribe to the Help ID)
    //Step 3: start timer and wait for x seconds, collect the responses for x seconds and go to step 4.
    //Step 4: invoke function to calculate the volunteer based on location from the list of responses.
    //Step 5: Update the database with the user id against the help id of the best volunteer found
    //Step 6: invoke another function to retrieve the help id record based for the user id mapping. (Does GetHelp() keep checking the status of help continuously)
    //Step 7: Return the user id


    //User case 2: Update on new Volunteer
    //Step 1: Update the database with the user id against the help id of the best volunteer found
    //Step 2: invoke another function to retrieve the help id record based for the user id mapping. (Does GetHelp() keep checking the status of help continuously)
    //Step 3: Return the user id
}
