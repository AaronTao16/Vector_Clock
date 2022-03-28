package com.rpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.*;

public class DataStoreServer{

    private static final int SERVER_PORT = 8000;
    private static final Map<String, List<String>> optsMap = new HashMap<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        initialize(args);

        // Build server
        Server server = ServerBuilder.forPort(optsMap.containsKey("PORT")?Integer.parseInt(optsMap.get("PORT").get(0)):SERVER_PORT)
                .addService(new DataStoreServiceImpl(optsMap.get("HOST").get(0), optsMap.get("PORT").get(0),
                        optsMap.get("HOSTS"), optsMap.get("PORTS"), Integer.parseInt(optsMap.get("Nw").get(0)),
                        Integer.parseInt(optsMap.get("Nr").get(0))))
                .build();

        // Start server
        System.out.println("Starting server on port " + (optsMap.containsKey("PORT")?Integer.parseInt(optsMap.get("PORT").get(0)):SERVER_PORT));
        server.start();

        // Keep it running
        System.out.println("Server started!");
        server.awaitTermination();
    }

    // -host 127.0.0.0 -port 7000 -N 3 -hosts 130.2.xx.xx,192.xx.xx.xx -ports 7001,7002
    private static void initialize(String[] args) {
        for (int i = 0; i < args.length; i++) {

            if(args[i].charAt(0) == '-'){
                if(i+1 == args.length)
                    throw new IllegalArgumentException("Expected arg after: "+args[i]);
                switch (args[i]){
                    case "-":
                        throw new IllegalArgumentException("Not a valid argument: "+args[i]);
                    case "-host":
                        optsMap.put("HOST", new ArrayList<>(Collections.singletonList(args[i + 1])));
                        break;
                    case "-hosts":
                        optsMap.put("HOSTS", new ArrayList<>(Arrays.asList(args[i+1].split(","))));
                        break;
                    case "-port":
                        List<String> port = new ArrayList<>(Collections.singletonList(args[i + 1]));
                        if(port.size() > 1) throw new IllegalArgumentException("Not a valid argument: "+args[i]);
                        else optsMap.put("PORT", port);
                        break;
                    case "-ports":
                        optsMap.put("PORTS", new ArrayList<>(Arrays.asList(args[i+1].split(","))));
                        break;
                    case "-N":
                        optsMap.put("N", new ArrayList<>(Arrays.asList(args[i+1].split(","))));
                        break;
                    case "-Nw":
                        optsMap.put("Nw", new ArrayList<>(Arrays.asList(args[i+1].split(","))));
                        break;
                    case "-Nr":
                        optsMap.put("Nr", new ArrayList<>(Arrays.asList(args[i+1].split(","))));
                        break;
                    default:
                        throw new IllegalArgumentException("Not a valid argument: "+args[i] + "\n   -client [client_name,...] \n    -port <port>");
                }
            }
        }
        if((optsMap.containsKey("Nw") && Integer.parseInt(optsMap.get("Nw").get(0)) < Integer.parseInt(optsMap.get("N").get(0))/2) || (optsMap.containsKey("Nr") && Integer.parseInt(optsMap.get("Nr").get(0)) < Integer.parseInt(optsMap.get("N").get(0))/2)){
            throw new IllegalArgumentException("Nw/Nr should greater than " + optsMap.get("N").get(0));
        }
    }
}
