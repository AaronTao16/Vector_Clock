package com.rpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.*;

public class DataStoreClient extends DataServiceGrpc.DataServiceImplBase{
    private static int PORT = 8000;
    private static String SERVER_IP = "localhost";
    private static List<String> argsList = new ArrayList<>();
    private static Map<String, String> optsMap = new HashMap<>();

    private static ManagedChannel channel;
    private static DataServiceGrpc.DataServiceBlockingStub dataServiceStub;


    public static void main(String[] args){
        initialize(args);

        channel = ManagedChannelBuilder.forAddress(optsMap.containsKey("SERVER_IP")?optsMap.get("SERVER_IP"):SERVER_IP, optsMap.containsKey("PORT")? Integer.parseInt(optsMap.get("PORT")) :PORT)
                .usePlaintext()
                .build();

        dataServiceStub = DataServiceGrpc.newBlockingStub(channel);

        if (optsMap.containsKey("READ")) {
            readData(optsMap.get("READ"));
        }

        if (optsMap.containsKey("UPDATE")) {
            updateData(DataStore.Data.newBuilder().setKey(optsMap.get("UPDATE").split(" ")[0]).setVal(optsMap.get("UPDATE").split(" ")[1]).build());
        }

    }

    // -server_ip 127.0.0.0 -port 7000 -read testData -update testData,6
    private static void initialize(String[] args) {
        for (int i = 0; i < args.length; i++) {

            if(args[i].charAt(0) == '-'){
                if(i+1 == args.length)
                    throw new IllegalArgumentException("Expected arg after: "+args[i]);
                switch (args[i]){
                    case "-":
                        throw new IllegalArgumentException("Not a valid argument: "+args[i]);
                    case "-client_id":
                        optsMap.put("CLIENT", args[i+1]);
                        break;
                    case "-server_ip":
                        optsMap.put("SERVER_IP", args[i+1]);
                        break;
                    case "-port":
                        optsMap.put("PORT", args[i+1]);
                        break;
                    case "-read":
                        optsMap.put("READ", args[i+1]);
                        break;
                    case "-update":
                        optsMap.put("UPDATE", args[i+1].replaceAll(",", " "));
                        break;
                    default:
                        throw new IllegalArgumentException("Not a valid argument: "+args[i] + "\n   -client_id <client_name> \n   -server_ip <ipaddress>\n    -port <port>");
                }
            } else {
                argsList.add(args[i]);
            }
        }

    }

    private static void readData(String key) {
        try {
            DataStore.Data res = dataServiceStub.read(DataStore.Data.newBuilder().setKey(key).build());
            System.out.println(res.getVal());
        } catch (Exception e){
            System.out.println(e);
        }
    }

    private static void updateData(DataStore.Data data) {
        try{
            DataStore.Heartbeat res = dataServiceStub.addUpdate(data);

            System.out.println(res.getData(0).getKey());
        } catch (StatusRuntimeException e){
            System.out.println("Server Error: " + e.getMessage());
        }

    }
}
