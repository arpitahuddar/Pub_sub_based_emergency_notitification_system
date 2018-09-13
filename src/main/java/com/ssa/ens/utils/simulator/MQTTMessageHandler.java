package com.ssa.ens.utils.simulator;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface MQTTMessageHandler
{
    public void handleMessage(String topicFilter, MqttMessage msg);
}
