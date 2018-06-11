package com.ssa.ens.utils.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssa.ens.utils.MQTTMessageReceiver;
import com.ssa.ens.utils.Utils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class Volunteer
{
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private String name;
    private Integer id;
    private double lat;
    private double lon;
    private String[] subscribedTopics;
    private String serverNotificationsTopicName;
    private Integer currentlyServicingHelpRequestId;
    private MQTTMessageReceiver serverNotificationMsgReceiver;
    private MQTTMessageReceiver subscribedTopicMsgReceivers;
    private MqttClient sendHelpResponseClient;

    public Volunteer(String name, Integer id, String[] subscribedTopics, double lat, double lon)
    {
        this.id = id;
        this.subscribedTopics = subscribedTopics;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.serverNotificationsTopicName = String.format("server-notifications-%d", id);
    }

    public void start()
    {
        try {
            sendHelpResponseClient = new MqttClient(Utils.MQTT_SERVER_URL,
                            UUID.randomUUID().toString(), new MemoryPersistence());

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setKeepAliveInterval(10);
            connOpts.setCleanSession(false);
            connOpts.setAutomaticReconnect(true);
            sendHelpResponseClient.connect(connOpts);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

        subscribedTopicMsgReceivers = new MQTTMessageReceiver(Utils.MQTT_SERVER_URL,
                UUID.randomUUID().toString(), subscribedTopics, new SubscribedTopicMessageHandler(this));
        serverNotificationMsgReceiver = new MQTTMessageReceiver(Utils.MQTT_SERVER_URL,
                UUID.randomUUID().toString(), new String[]{serverNotificationsTopicName}, new ServerNotificationsMessageHandler(this));
        subscribedTopicMsgReceivers.subscribe();
        serverNotificationMsgReceiver.subscribe();

    }

    public void stop()
    {
        serverNotificationMsgReceiver.quit();
        subscribedTopicMsgReceivers.quit();
    }

    static class SubscribedTopicMessageHandler implements MQTTMessageHandler
    {

        private Volunteer owner;

        public SubscribedTopicMessageHandler(Volunteer owner)
        {
            this.owner = owner;
        }

        @Override
        public void handleMessage(String topicFilter, MqttMessage msg)
        {
            System.out.println(String.format(
                    "[%s,%d]HelpRequest Received For:%s, Msg:%s", owner.name, owner.id,
                    topicFilter, msg.toString()));
            try {
                Map<String, Object> payload = MAPPER.reader().forType(Map.class).readValue(msg.getPayload());
                //retrieve from database
                if (owner.currentlyServicingHelpRequestId == null) {
                    acceptHelp((int)payload.get("helpRequestId"));
                } else {
                    System.out.println(String.format(
                    "[%s,%d] owner not available skipping", owner.name, owner.id));
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void acceptHelp(int helpRequestId)
        {
           // helpRequestId = 6709322897899884248L;
            String topicName =  String.format("%d_response", helpRequestId);
            {
                try {
                    MqttMessage message = new MqttMessage();
                    String payload = String.format("{\"helpRequestId\":%d, " +
                            "\"volunteerUserId\":%d, " +
                            "\"volunteerUserName\":\"%s\", " +
                            "\"lat\":%f, " +
                            "\"lon\":%f}", helpRequestId, owner.id, owner.name,
                            owner.lat, owner.lon);
                    System.out.println("" +
                            String.format("Volunteer Accepting HelpRequest, TopicName:%s" +
                                    " MsgPayload:%s", topicName, payload));
                    message.setPayload(payload.getBytes());
                    message.setQos(1);
                    owner.sendHelpResponseClient.publish(topicName, message);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    static class ServerNotificationsMessageHandler implements MQTTMessageHandler

    {
        private Volunteer owner;

        public ServerNotificationsMessageHandler(Volunteer owner)
        {
            this.owner = owner;
        }

        @Override
        public void handleMessage(String topicFilter, MqttMessage msg)
        {
            try {
                Map<String, Object> payload = MAPPER.reader().forType(Map.class).readValue(msg.getPayload());
                String notificationType = (String) payload.get("notificationType");
                if (notificationType.equals("service_help_request_accepted")) {
                    System.out.println(owner.name +" is the Volunteer Matched, MsgPayload:" + payload);
                    final int helpRequestId = (Integer) payload.get("helpRequestId");
                    owner.currentlyServicingHelpRequestId = helpRequestId;
                } else if (notificationType.equals("service_help_request_completed")) {
                    owner.currentlyServicingHelpRequestId = null;
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
