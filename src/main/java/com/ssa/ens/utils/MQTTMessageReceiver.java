package com.ssa.ens.utils;

import com.ssa.ens.utils.simulator.MQTTMessageHandler;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.List;

public class MQTTMessageReceiver implements MqttCallbackExtended
{

    private String broker;
    private String clientId;
    private String[] subscribeTopics;
    private MqttClient client = null;
    private MQTTMessageHandler msgHandler;

    public MQTTMessageReceiver(String broker, String clientId, String[] subscribeTopics, MQTTMessageHandler msgHandler) {
        this.broker = broker;
        this.clientId = clientId;
        this.subscribeTopics = subscribeTopics;
        this.msgHandler = msgHandler;
    }

    public Void subscribe() {

        try {
            if (client == null) {
                client = new MqttClient(broker, clientId, new MemoryPersistence());
            }

            System.out.println("Connecting");
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setKeepAliveInterval(10);
            connOpts.setCleanSession(true);
            connOpts.setAutomaticReconnect(true);
            client.setCallback(this);
            client.connect(connOpts);
            System.out.println("Connected");

        } catch (MqttException me) {
            me.printStackTrace();
        }
        return null;
    }

    public void quit() {
        final String METHOD = "quit";
        try {
            client.disconnect();
            System.out.println(METHOD + " disconnected!");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable thrwbl) {
        System.out.println("Connection lost ");

    }

    @Override
    public void messageArrived(String topicFilter, MqttMessage mm) throws Exception {
        msgHandler.handleMessage(topicFilter, mm);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {

    }

    @Override
    public void connectComplete(boolean bReconnect, String host) {
        final String METHOD = "connectComplete";
        System.out.println(METHOD + " Connected to " + host + " Auto reconnect ? " + bReconnect);
        try {
            client.subscribe(subscribeTopics);
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }
}