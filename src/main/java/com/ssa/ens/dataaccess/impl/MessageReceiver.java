package com.ssa.ens.dataaccess.impl;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MessageReceiver implements MqttCallbackExtended
{

    String broker;
    String clientId;
    String subscribeTopic;
    MqttClient client = null;

    public MessageReceiver(String broker, String clientId, String subscribeTopic) {
        this.broker = broker;
        this.clientId = clientId;
        this.subscribeTopic = subscribeTopic;
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
            client.setTimeToWait(30000);
            System.out.println("Connected");

        } catch (MqttException me) {
            me.printStackTrace();
        }
        return null;
    }

    void quit() {
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
    public void messageArrived(String string, MqttMessage mm) throws Exception {
        System.out.println("Message " + mm.toString() + "\n" + string);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {

    }

    @Override
    public void connectComplete(boolean bReconnect, String host) {
        final String METHOD = "connectComplete";
        System.out.println(METHOD + " Connected to " + host + " Auto reconnect ? " + bReconnect);
        try {
            client.subscribe(subscribeTopic, 2);
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }
}