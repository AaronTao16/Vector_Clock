package com.rpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataStoreServiceImpl extends DataServiceGrpc.DataServiceImplBase{
    Map<String, Integer> PORTS = new HashMap<>();
    private String HOST;
    private static ManagedChannel channel;
    private static DataServiceGrpc.DataServiceBlockingStub dataServiceStub;
    private static final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Long> clock = new ConcurrentHashMap<>();
    private static DataStore.Vector_clock vector_clock;
    private static DataStore.Server conflict = DataStore.Server.newBuilder().setData(DataStore.Data.newBuilder().setKey("No conflict").build()).build();

    public DataStoreServiceImpl(String HOST, List<String> HOSTS, List<String> PORTS){
        this.HOST = HOST;
        clock.put(HOST, 0L);
        for (int i = 0; i < HOSTS.size(); ++i) {
            clock.put(HOSTS.get(i), 0L);
            this.PORTS.put(HOSTS.get(i), Integer.parseInt(PORTS.get(i)));
        }
    }

    @Override
    public void read(DataStore.Data data, StreamObserver<DataStore.Data> responseObserver) {
        System.out.println("---read---");
        System.out.println(data);
        if(store.containsKey(data.getKey())) {
            responseObserver.onNext(DataStore.Data.newBuilder().setKey(data.getKey()).setVal(store.get(data.getKey())).build());

//            clock.put(HOST, clock.get(HOST)+1);
//
//            getVectorClock();
//
//            for(Map.Entry<String, Integer> entry: PORTS.entrySet()){
//                propagate(entry.getKey(), entry.getValue());
//            }
        }
        else {
            responseObserver.onNext(DataStore.Data.newBuilder().setKey("null").setVal("Key is not exist").build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void addUpdate(DataStore.Data data, StreamObserver<DataStore.Server> responseObserver) {
        System.out.println("---update---");
        System.out.println(data);

        store.put(data.getKey(), data.getVal());
        clock.put(HOST, clock.get(HOST)+1);

        getVectorClock(data);

        for(Map.Entry<String, Integer> entry: PORTS.entrySet()){
            propagate(entry.getKey(), entry.getValue());
            responseObserver.onNext(conflict);
        }

        System.out.println("current clock" + clock);
        responseObserver.onCompleted();
    }

    @Override
    public void propagate(DataStore.Vector_clock clocks, StreamObserver<DataStore.Server> responseObserver) {

        boolean isGreater = false, isLess = false, isConflict = false;
        store.put(clocks.getClocks(0).getData().getKey(), clocks.getClocks(0).getData().getVal());
        for(DataStore.Server server: clocks.getClocksList()){
            if(clock.get(server.getId()) < server.getClock()){ // local < received
                if(isGreater){
                    conflict = DataStore.Server.newBuilder().setId(HOST)
                            .setData(DataStore.Data.newBuilder().setKey(server.getData().getKey()).setVal(store.getOrDefault(server.getData().getKey(), "conflict")).build())
                            .build();
                    isConflict = true;
                    break;
                } else {
                    isLess = true;
                    clock.put(server.getId(), Math.max(server.getClock(), clock.get(server.getId())));
                }
            } else if(clock.get(server.getId()) > server.getClock()){
                if(isLess){
                    conflict = DataStore.Server.newBuilder().setId(HOST)
                            .setData(DataStore.Data.newBuilder().setKey(server.getData().getKey()).setVal(store.getOrDefault(server.getData().getKey(), "conflict")).build())
                            .build();
                    isConflict = true;
                    break;
                } else {
                    isGreater = true;
                    clock.put(server.getId(), Math.max(server.getClock(), clock.get(server.getId())));
                }
            }

        }
        System.out.println("current clock" + clock);
        if(!isConflict) {
            store.put(clocks.getClocks(0).getData().getKey(), clocks.getClocks(0).getData().getVal());
            conflict = conflict.toBuilder().setId(HOST).setData(DataStore.Data.newBuilder().setKey("No conflict").build()).build();
        }
        else {
            conflict = conflict.toBuilder().setId(HOST).build();
        }
        responseObserver.onNext(conflict);
        responseObserver.onCompleted();
    }

    private void propagate(String host, int port){
        channel = ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();
        dataServiceStub = DataServiceGrpc.newBlockingStub(channel);

        Iterator<DataStore.Server> res = dataServiceStub.propagate(vector_clock);

        try{
            while (res.hasNext()){
                DataStore.Server d = res.next();
                if(d.getData().getKey().equals("No conflict"))
                    continue;
                if(d.getData().getKey().equals("conflict"))
                    System.out.println("---conflicts---");
                System.out.println(d);
            }
        } catch (StatusRuntimeException e){
            System.out.println("Server Error: " + host + ":"+ port + " " + e.getMessage());
        }

    }

    private void getVectorClock(DataStore.Data data){
        vector_clock = DataStore.Vector_clock.newBuilder().build();
        for (Map.Entry<String, Long> entry :clock.entrySet()) {
            DataStore.Server serverC = DataStore.Server.newBuilder().setId(entry.getKey()).setClock(entry.getValue()).setData(data).build();
            vector_clock = vector_clock.toBuilder().addClocks(serverC).build();
        }
    }

    private void getVectorClock(){
        vector_clock = DataStore.Vector_clock.newBuilder().build();
        for (Map.Entry<String, Long> entry :clock.entrySet()) {
            DataStore.Server serverC = DataStore.Server.newBuilder().setId(entry.getKey()).setClock(entry.getValue()).build();
            vector_clock = vector_clock.toBuilder().addClocks(serverC).build();
        }
    }
}
