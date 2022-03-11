package com.rpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.*;

public class DataStoreServer{

    private static final int SERVER_PORT = 8000;
    private static List<String> argsList = new ArrayList<>();
    private static final Map<String, List<String>> optsMap = new HashMap<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        initialize(args);

        // Build server
        Server server = ServerBuilder.forPort(optsMap.containsKey("PORT")?Integer.parseInt(optsMap.get("PORT").get(0)):SERVER_PORT)
                .addService(new DataStoreServiceImpl(optsMap.get("HOST").get(0), optsMap.get("PORT").get(0),optsMap.get("HOSTS"), optsMap.get("PORTS")))
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
                        List<String> host = new ArrayList<>(Collections.singletonList(args[i + 1]));
                        optsMap.put("HOST", host);
                        break;
                    case "-hosts":
                        List<String> hosts = new ArrayList<>(Arrays.asList(args[i+1].split(",")));
                        optsMap.put("HOSTS", hosts);
                        break;
                    case "-port":
                        List<String> port = new ArrayList<>(Collections.singletonList(args[i + 1]));
                        if(port.size() > 1) throw new IllegalArgumentException("Not a valid argument: "+args[i]);
                        else optsMap.put("PORT", port);
                        break;
                    case "-ports":
                        List<String> ports = new ArrayList<>(Arrays.asList(args[i+1].split(",")));
                        optsMap.put("PORTS", ports);
                        break;
                    case "-N":
                        List<String> n = new ArrayList<>(Arrays.asList(args[i+1].split(",")));
                        optsMap.put("N", n);
                        break;
                    default:
                        throw new IllegalArgumentException("Not a valid argument: "+args[i] + "\n   -client [client_name,...] \n    -port <port>");
                }
            } else {
                argsList.add(args[i]);
            }
        }
    }
}
