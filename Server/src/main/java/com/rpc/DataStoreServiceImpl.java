package com.rpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class DataStoreServiceImpl extends DataServiceGrpc.DataServiceImplBase{
    private List<String> Replicas = new ArrayList<>(); // "host:port"
    private ConcurrentHashMap<String, Long> AvailableReplicas = new ConcurrentHashMap<>(); // "host:port,id"
    private String HOST, PORT; // store host and port
    private DataStore.Node LEADER;
    private Set<DataStore.Node> potentialLeader = ConcurrentHashMap.newKeySet();
    private int Nw, Nr;
    private long id;
    private static ManagedChannel channel;
    private static DataServiceGrpc.DataServiceBlockingStub dataServiceStub;
    private static DataServiceGrpc.DataServiceBlockingStub heartBeatServiceStub;
    private static final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>(); // key-value store
    private static ConcurrentHashMap<String, Long> clock = new ConcurrentHashMap<>(); // local clocks <"host:port", clock>
    private static DataStore.Vector_clock vector_clock;
    private static DataStore.Server conflict = DataStore.Server.newBuilder().setData(DataStore.Data.newBuilder().setKey("No conflict").build()).build();

    public DataStoreServiceImpl(String HOST, String PORT, List<String> HOSTS, List<String> PORTS, int Nw, int Nr){
        this.HOST = HOST;
        this.PORT = PORT;
        this.Nw = Nw;
        this.Nr = Nr;
        clock.put(HOST+":"+PORT, 0L);
        for (int i = 0; i < HOSTS.size(); ++i) {
            clock.put(HOSTS.get(i)+":"+PORTS.get(i), 0L);
            Replicas.add(HOSTS.get(i)+":"+PORTS.get(i));
        }
        this.id = System.currentTimeMillis();
        this.sendHeartbeat(Replicas, DataStore.Heartbeat.newBuilder().setFrom(DataStore.Node.newBuilder().setId(id).setAddr(HOST+":"+PORT).build()).build());
    }

//    @Override
//    public StreamObserver<DataStore.Heartbeat> sendHeartbeats(StreamObserver<DataStore.Heartbeat> responseObserver) {
//        return new StreamObserver<DataStore.Heartbeat>() {
//            @Override
//            public void onNext(DataStore.Heartbeat heartbeat) {
//                System.out.println("Received heartbeat:  \n{\n" + heartbeat + "}");
//                responseObserver.onNext(DataStore.Heartbeat.newBuilder().setFrom(DataStore.Node.newBuilder().setId(id).setAddr(HOST+":"+PORT).build()).build());
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//
//            }
//
//            @Override
//            public void onCompleted() {
//
//            }
//        };
//    }


    @Override
    public void sendHeartbeats(DataStore.Heartbeat request, StreamObserver<DataStore.Heartbeat> responseObserver) {
        System.out.println(request);
        System.out.println(request.hasLeader());
        if(!request.hasLeader()){
            responseObserver.onNext(DataStore.Heartbeat.newBuilder().setFrom(DataStore.Node.newBuilder().setId(id).setAddr(HOST+":"+PORT).build())
                    .setLeader(LEADER).build());
        }
        responseObserver.onCompleted();
    }

    public void sendHeartbeat(List<String> replicas, DataStore.Heartbeat heartbeat){
        for(String entry: replicas){
            long start = System.currentTimeMillis();
//            Thread object = new Thread(new Multithreading(entry, heartbeat));
//            object.start();
            AvailableReplicas.remove(entry);
            broadcast(entry, heartbeat);
            System.out.println("broadcast to " + entry + "   " + ((double)(System.currentTimeMillis()-start))/1000 + "s");
        }

        if(potentialLeader.isEmpty() && AvailableReplicas.isEmpty()){
            LEADER = DataStore.Node.newBuilder().setId(id).setAddr(HOST+":"+PORT).build();
        } else if (potentialLeader.size() > 1){
            System.out.println("re-elect leader");
        } else {
            LEADER = potentialLeader.iterator().next();;
        }

        System.out.println(LEADER);
    }

    private void broadcast(String target, DataStore.Heartbeat heartbeat) {
        String host = target.split(":")[0];
        int port = Integer.parseInt(target.split(":")[1]);
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        heartBeatServiceStub = DataServiceGrpc.newBlockingStub(channel);

//        StreamObserver<DataStore.Heartbeat> streamObserver = heartBeatServiceStub.withDeadlineAfter(2000, TimeUnit.MILLISECONDS).sendHeartbeats(new StreamObserver<DataStore.Heartbeat>() {
//            @Override
//            public void onNext(DataStore.Heartbeat heartbeat) {
//                System.out.println("Received heartbeat:  \n{\n" + heartbeat + "}");
//                AvailableReplicas.put(heartbeat.getFrom().getId(), heartbeat.getFrom().getAddr());
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                System.out.println(throwable.getMessage());
//
//            }
//
//            @Override
//            public void onCompleted() {
//                System.out.println("completed");
//            }
//        });

        System.out.println("Send heartbeat: \n {" + heartbeat + "}");
        try {
            DataStore.Heartbeat res = heartBeatServiceStub.withDeadlineAfter(3000, TimeUnit.MILLISECONDS).sendHeartbeats(heartbeat);
            AvailableReplicas.put(res.getFrom().getAddr(), res.getFrom().getId());
            potentialLeader.add(res.getLeader());
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

//        streamObserver.onNext(heartbeat);
    }

    @Override
    public void leaderElection(DataStore.Heartbeat request, StreamObserver<DataStore.Heartbeat> responseObserver) {
//        sendHeartbeats();
    }

    @Override
    public void read(DataStore.Data data, StreamObserver<DataStore.Data> responseObserver) {
        System.out.println("---read---");
        System.out.println(data);
        if(store.containsKey(data.getKey())) {
            responseObserver.onNext(DataStore.Data.newBuilder().setKey(data.getKey()).setVal(store.get(data.getKey())).build());
        }
        else { // key does not exist
            responseObserver.onNext(DataStore.Data.newBuilder().setKey("null").setVal("Key does not exist").build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void addUpdate(DataStore.Data data, StreamObserver<DataStore.Server> responseObserver) {
        System.out.println("---update---");
        System.out.println(data);

        store.put(data.getKey(), data.getVal());
        clock.put(HOST+":"+PORT, clock.get(HOST+":"+PORT)+1);

        // convert clock to Vector_Clock
        getVectorClock(data);

        System.out.println(HOST+":"+ PORT +" propagate...");
        // send to all replicas
        for(String entry: Replicas){
            long start = System.currentTimeMillis();
            propagate(entry.split(":")[0], Integer.parseInt(entry.split(":")[1]));
            System.out.println("propagate to " + entry + "   " + ((double)(System.currentTimeMillis()-start))/1000 + "s");
            if(conflict != null) responseObserver.onNext(conflict);
        }

        System.out.println(HOST+":"+ PORT +" current clock" + clock);
        responseObserver.onCompleted();
    }

    @Override
    public void propagate(DataStore.Vector_clock clocks, StreamObserver<DataStore.Server> responseObserver) {
        DataStore.Server conflict = DataStore.Server.newBuilder().setId(HOST+":"+PORT).setData(DataStore.Data.newBuilder().setKey("No conflict").build()).build();        boolean isGreater = false, isLess = false, isConflict = false;

        // either all local clocks <= received or received <= local
        for(DataStore.Server server: clocks.getClocksList()){
            if(clock.get(server.getId()) < server.getClock()){ // local < received
                if(isGreater){
                    conflict = conflict.toBuilder().setId(HOST+":"+PORT)
                            .setData(DataStore.Data.newBuilder().setKey(server.getData().getKey()).setVal(store.getOrDefault(server.getData().getKey(), "conflict")).build())
                            .build();
                    isConflict = true;
                    break;
                } else {
                    isLess = true;
                }
            } else if(clock.get(server.getId()) > server.getClock()){ // local > received
                if(isLess){
                    conflict = conflict.toBuilder().setId(HOST+":"+PORT)
                            .setData(DataStore.Data.newBuilder().setKey(server.getData().getKey()).setVal(store.getOrDefault(server.getData().getKey(), "conflict")).build())
                            .build();
                    isConflict = true;
                    break;
                } else {
                    isGreater = true;
                }
            }

        }

        // if no conflict update clock and update value
        if(!isConflict) {
            store.put(clocks.getClocks(0).getData().getKey(), clocks.getClocks(0).getData().getVal());
//            clock.put(HOST+":"+PORT, clock.get(HOST+":"+PORT)+1);
            for(DataStore.Server server: clocks.getClocksList())
                clock.put(server.getId(), Math.max(server.getClock(), clock.get(server.getId())));
        }

        System.out.println(HOST+":"+ PORT +" current clock" + clock);

        responseObserver.onNext(conflict);
        responseObserver.onCompleted();
    }

    private void propagate(String host, int port){
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        dataServiceStub = DataServiceGrpc.newBlockingStub(channel);

        // send local clock to every replicas
        Iterator<DataStore.Server> res = dataServiceStub.propagate(vector_clock);

        try{
            while (res.hasNext()){
                DataStore.Server d = res.next();
                if(d.getData().getKey().equals("No conflict"))
                    continue;

                conflict = d;
            }
        } catch (StatusRuntimeException e){
            conflict = null;
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
}
