package com.ssa.ens.utils;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTTPubslishToHelpResponses
{

    public static void main(String args[])
    {
        final String[] userIds = new String[]{"123", "456", "789", "101112"};
        final String topicName = "3419489791173618308" + "_response";

        try {
            MqttClient client = new MqttClient("tcp://ec2-34-220-247-164.us-west-2.compute.amazonaws.com:1883", MqttClient.generateClientId());
            client.connect();
            for (String userId : userIds) {
                MqttMessage message = new MqttMessage();
                final String msg = String.format("{\"canHelp\":1, \"volunteerUserId\":%s, \"location\":\"52.134, -51.02\"}", userId);
                message.setPayload(msg.getBytes());
                client.publish(topicName, message);
            }

            client.disconnect();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
