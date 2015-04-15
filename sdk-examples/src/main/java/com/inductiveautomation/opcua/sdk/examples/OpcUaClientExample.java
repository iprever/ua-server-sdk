/*
 * Copyright 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.inductiveautomation.opcua.sdk.examples;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.inductiveautomation.opcua.sdk.client.OpcUaClient;
import com.inductiveautomation.opcua.sdk.client.OpcUaClientConfig;
import com.inductiveautomation.opcua.stack.client.UaTcpStackClient;
import com.inductiveautomation.opcua.stack.client.UaTcpStackClientConfig;
import com.inductiveautomation.opcua.stack.core.Identifiers;
import com.inductiveautomation.opcua.stack.core.types.builtin.LocalizedText;
import com.inductiveautomation.opcua.stack.core.types.builtin.NodeId;
import com.inductiveautomation.opcua.stack.core.types.enumerated.MessageSecurityMode;
import com.inductiveautomation.opcua.stack.core.types.enumerated.TimestampsToReturn;
import com.inductiveautomation.opcua.stack.core.types.structured.EndpointDescription;

public class OpcUaClientExample {

    public static void main(String[] args) throws Exception {
//        EndpointDescription[] endpoints = UaTcpStackClient.getEndpoints("opc.tcp://localhost:4096").get();
        EndpointDescription[] endpoints = UaTcpStackClient.getEndpoints("opc.tcp://10.20.5.210:49320").get();

        EndpointDescription endpoint = Arrays.stream(endpoints)
                .filter(e -> e.getSecurityMode() == MessageSecurityMode.None)
                .findFirst()
                .orElseThrow(() -> new Exception("no desired endpoints returned"));

        System.out.println("Connecting to endpoint: " + endpoint.getEndpointUrl() + " [" + endpoint.getSecurityPolicyUri() + "]");

        UaTcpStackClientConfig stackConfig = UaTcpStackClientConfig.builder()
                .setApplicationName(LocalizedText.english("Stack Example Client"))
                .setApplicationUri(String.format("urn:example-client:%s", UUID.randomUUID()))
//                .setCertificate(certificate)
//                .setKeyPair(keyPair)
                .setEndpoint(endpoint)
                .build();

        UaTcpStackClient stackClient = new UaTcpStackClient(stackConfig);


        OpcUaClientConfig configConfig = OpcUaClientConfig.builder()
                .setStackClient(stackClient)
                .setRequestTimeout(120000)
                .build();

        OpcUaClient client = new OpcUaClient(configConfig);

        client.connect().get();

//        SubscriptionManager subscriptionManager = new SubscriptionManager(client);
//
//
//        OpcUaSubscription subscription = new OpcUaSubscription(
//                client,
//                1000.0,
//                uint(15),
//                uint(5),
//                uint(0),
//                true,
//                ubyte(0));
//
//        UInteger clientHandle = subscriptionManager.nextClientHandle();
//
//        ReadValueId readValueId = new ReadValueId(
//                Identifiers.Server_ServerStatus_CurrentTime,
//                uint(13), null, QualifiedName.NULL_VALUE);
//
//        OpcUaMonitoredItem item = new OpcUaMonitoredItem(clientHandle, readValueId);
//
//        subscriptionManager.addSubscription(subscription).thenCompose(s -> {
//            return subscription.createItems(TimestampsToReturn.Both, createItemsContext -> {
//                MonitoringParameters parameters = new MonitoringParameters(
//                        uint(1),
//                        250.0,
//                        null,
//                        uint(5),
//                        true);
//
//                createItemsContext.addItem(item, parameters, MonitoringMode.Reporting);
//            });
//        }).thenAccept(results -> results.forEach(System.out::println));

        int N_CONCURRENT = 1000;
        int N_NODES = 1000;

        List<NodeId> nodeIds = Collections.nCopies(N_NODES, Identifiers.Server_ServerStatus_CurrentTime);

        long timeA = System.nanoTime();
        CompletableFuture<?>[] futures = new CompletableFuture<?>[N_CONCURRENT];

        for (int i = 0; i < N_CONCURRENT; i++) {
            futures[i] = client.readValues(0.0d, TimestampsToReturn.Both, nodeIds);
        }

        CompletableFuture.allOf(futures).get();
        long timeB = System.nanoTime();

        System.out.println(String.format("%s requests took %sms",
                N_CONCURRENT, TimeUnit.MILLISECONDS.convert(timeB - timeA, TimeUnit.NANOSECONDS)));

        Thread.sleep(999999999);
    }

}
