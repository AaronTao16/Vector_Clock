package com.rpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

public class DataStoreServiceImpl extends DataServiceGrpc.DataServiceImplBase{
    private final List<String> Replicas = new ArrayList<>(); // "host:port"
    private final ConcurrentHashMap<String, Long> AvailableReplicas = new ConcurrentHashMap<>(); // "host:port,id"
    private DataStore.Node LEADER;
    private final DataStore.Node ME;
    private final Set<DataStore.Node> potentialLeader = ConcurrentHashMap.newKeySet();
    private int Nw, Nr, commit;
    private long id;
    private static ManagedChannel channel;
    private static final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>(); // key-value store
    private static final ConcurrentHashMap<String, Long> clock = new ConcurrentHashMap<>(); // local clocks <"host:port", clock>
    private static final ConcurrentLinkedDeque<DataStore.Log> logs = new ConcurrentLinkedDeque<>(); // logs
    private final Set<String> quorum_result = new HashSet<>();
    private boolean receiveFromAllReplicas = true, isCommitted = false, isConflict = false, isReElection = false;

    public DataStoreServiceImpl(String HOST, String PORT, List<String> HOSTS, List<String> PORTS, int Nw, int Nr){
        this.id = System.currentTimeMillis();
        this.Nw = Nw;
        this.Nr = Nr;
        this.ME = DataStore.Node.newBuilder().setId(id).setAddr(HOST+":"+PORT).build();
        clock.put(HOST+":"+PORT, 0L);
        for (int i = 0; i < HOSTS.size(); ++i) {
            clock.put(HOSTS.get(i)+":"+PORTS.get(i), 0L);
            Replicas.add(HOSTS.get(i)+":"+PORTS.get(i));
        }
        DataStore.Heartbeat initialHeartbeat = DataStore.Heartbeat.newBuilder().setFrom(ME).setOperation("INIT").build();
        initialHeartbeat = formatClocks(initialHeartbeat);
        this.sendHeartbeat(Replicas, initialHeartbeat);
    }

    @Override
    public void sendHeartbeats(DataStore.Heartbeat request, StreamObserver<DataStore.Heartbeat> responseObserver) {
        System.out.println("---RECEIVED HEARTBEAT---");
        System.out.println(request);
        switch (request.getOperation()){
            case "INIT":
                AvailableReplicas.put(request.getFrom().getAddr(), request.getFrom().getId());
                System.out.println("---AVAILABLE REPLICAS---");
                System.out.println(AvailableReplicas);

                if(!request.hasLeader()){
                    responseObserver.onNext(DataStore.Heartbeat.newBuilder().setFrom(ME).setLeader(LEADER).build());
                }
                break;
            case "SYNC-LOGS":
                Iterator<DataStore.Log> iterator = logs.descendingIterator();
                DataStore.Heartbeat res = DataStore.Heartbeat.newBuilder().setFrom(ME).setLeader(LEADER).build();
                while(iterator.hasNext()){
                    DataStore.Log log = iterator.next();
                    if(log.getClocksMap().get(ME.getAddr()).getClock() <= request.getLogsList().get(0).getClocksMap().get(ME.getAddr()).getClock()){
                        break;
                    }
                    for(Map.Entry<String, DataStore.Clock> l: log.getClocksMap().entrySet()){
                        res = res.toBuilder().addLogs(DataStore.Log.newBuilder().putClocks(l.getKey(), l.getValue()).setData(log.getData())).build();
                    }
                }
                responseObserver.onNext(res);
                break;
            case "UPDATE":
                checkConflict(request);
                String resMsg = "cannot commit";

                if(!isConflict) {
                    this.sendHeartbeat(Replicas, DataStore.Heartbeat.newBuilder().setFrom(ME).addData(0, request.getData(0)).setOperation("UPDATE").build());
                    if(commit >= Nw) resMsg = "committed";
                    responseObserver.onNext(DataStore.Heartbeat.newBuilder().setFrom(ME).setLeader(LEADER).setOperation(resMsg).build());
                } else
//                    if(AvailableReplicas.isEmpty()){
//                    resMsg += " less than " + Nw + " nodes are alive";
//                    responseObserver.onNext(DataStore.Heartbeat.newBuilder().setFrom(ME).setLeader(LEADER).setOperation(resMsg).build());
//                } else
                {
                    resMsg += " since conflict";
                    DataStore.Heartbeat response = DataStore.Heartbeat.newBuilder().setFrom(ME).setLeader(LEADER).setOperation(resMsg).build();
                    Iterator<DataStore.Log> logIterator = logs.descendingIterator();
                    while(logIterator.hasNext()){
                        DataStore.Log log = logIterator.next();
//                        if(log.getClock() == remote_clock_ME.getClock()) break;
                        response = response.toBuilder().addData(log.getData()).build();
                    }
                    responseObserver.onNext(response);
                }

                break;
        }
        responseObserver.onCompleted();
    }

    @Override
    public void leaderElection(DataStore.Heartbeat request, StreamObserver<DataStore.Heartbeat> responseObserver) {
        System.out.println("---RECEIVED LEADER ELECTION---");
        System.out.println(request);
        switch (request.getOperation()){
            case "RE-ELECTION":
                System.out.println(AvailableReplicas);
                String newLeader = AvailableReplicas.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue()? 1:-1).get().getKey();
                DataStore.Node leader = DataStore.Node.newBuilder().setAddr(newLeader).setId(id).build();
                if(leader.getAddr().equals(ME.getAddr()))
                    LEADER = ME;
                sendHeartbeat(Replicas, request.toBuilder().setFrom(ME).setOperation("IWON").build());

                responseObserver.onNext(request.toBuilder().setFrom(ME).setOperation("OK").build());
                break;
            case "IWON":
                LEADER = request.getFrom();
                responseObserver.onNext(request.toBuilder().setFrom(ME).setOperation("OK").build());
                System.out.println("NEW LEADER:");
                System.out.println(LEADER);
                break;
            default:
                System.out.println("INVALID OPERATION");
        }
        responseObserver.onCompleted();
    }

    @Override
    public void sendQuorumRequest(DataStore.Heartbeat request, StreamObserver<DataStore.Heartbeat> responseObserver) {
        System.out.println("---RECEIVED QUORUM REQUEST---");
        System.out.println(request);
        request.toBuilder().setFrom(ME);

        switch (request.getOperation()){
            case "READ":
                if(store.containsKey(request.getData(0).getKey())) {
                    responseObserver.onNext(request.toBuilder().setData(0, DataStore.Data.newBuilder().setKey(request.getData(0).getKey()).setVal(store.get(request.getData(0).getKey())).build()).build());
                }
                else { // key does not exist
                    responseObserver.onNext(request.toBuilder().setData(0, DataStore.Data.newBuilder().setKey("null").setVal("Key does not exist").build()).build());
                }
                break;
            case "UPDATE":
                responseObserver.onNext(request.toBuilder().setOperation("ready").build());
                break;
            case "COMMIT":
                isCommitted = true;
                store.put(request.getData(0).getKey(), request.getData(0).getVal());
                // commit update, local clock+1
//                clock.put(ME.getAddr(), clock.get(ME.getAddr())+1);
                // synchronize local clock and remote clock
                syncClock(request.getClocksList());
                // store commit log in local
                storeLog(request.getData(0));
                request = formatClocks(request);
                // send current clock back to leader
                responseObserver.onNext(request.toBuilder().setOperation("COMMIT_FINISH").build());
                break;
            default:
                System.out.println("INVALID OPERATION");
        }

        responseObserver.onCompleted();
    }

    private void storeLog(DataStore.Data data) {
        DataStore.Log newLog = DataStore.Log.newBuilder().setData(data).build();
        for(Map.Entry<String, Long> c: clock.entrySet()){
            newLog = newLog.toBuilder().putClocks(c.getKey(), DataStore.Clock.newBuilder().setClock(c.getValue()).setNode(DataStore.Node.newBuilder().setAddr(c.getKey()).build()).build()).build();
        }
        logs.add(newLog);
        System.out.println("---LOCAL LOGS---");
        System.out.println(logs);
    }

    @Override
    public void read(DataStore.Data data, StreamObserver<DataStore.Data> responseObserver) {
        System.out.println("---READ REQUEST---");
        System.out.println("---LOCAL STORE---");
        System.out.println(store.containsKey(data.getKey())?(data.getKey()+":"+store.get(data.getKey())):"null:Key does not exist");
        System.out.println("---ASK REMOTE STORE---");
        sendHeartbeat(new ArrayList<>(AvailableReplicas.keySet()),
                DataStore.Heartbeat.newBuilder().setFrom(DataStore.Node.newBuilder().setId(id).setAddr(ME.getAddr()).build())
                        .addData(0, data).setOperation("READ").build());

        if (quorum_result.size() == 1 && receiveFromAllReplicas) {
            String str = quorum_result.iterator().next();
            responseObserver.onNext(DataStore.Data.newBuilder().setKey(str.split(":")[0]).setVal(str.split(":")[1]).build());
        } else if (!receiveFromAllReplicas) {
            responseObserver.onNext(DataStore.Data.newBuilder().setKey(data.getKey()).setVal("Read Error: Less than " + Nr + " nodes have agreed value").build());
        } else {
            responseObserver.onNext(DataStore.Data.newBuilder().setKey(data.getKey()).setVal("Read Error").build());
        }
        quorum_result.clear();
        System.out.println();
        responseObserver.onCompleted();
    }

    @Override
    public void addUpdate(DataStore.Data data, StreamObserver<DataStore.Heartbeat> responseObserver) {
        System.out.println("---UPDATE REQUEST---");
        System.out.println(data);

        redirectToLeader(data);

        if(isReElection){
            System.out.println("Please wait ReTry");
            redirectToLeader(data);
        }

        if((ME.getAddr().equals(LEADER.getAddr()) && commit >= Nw) || (!ME.getAddr().equals(LEADER.getAddr()) && isCommitted)) {
            System.out.println("update successfully");
            responseObserver.onNext(DataStore.Heartbeat.newBuilder().addData(DataStore.Data.newBuilder().setKey("Update successfully, no conflict")).build());
        } else if((ME.getAddr().equals(LEADER.getAddr()) && commit < Nw) || (!ME.getAddr().equals(LEADER.getAddr()) && !isCommitted)){
            System.out.println("Update fail");
            responseObserver.onNext(DataStore.Heartbeat.newBuilder().addData(DataStore.Data.newBuilder().setKey("Update Fail: Less than " + Nw + " nodes are alive")).build());
        }
        System.out.println(ME.getAddr() +" current clock" + clock);
        System.out.println();

        responseObserver.onCompleted();
    }

    public void sendHeartbeat(List<String> replicas, DataStore.Heartbeat heartbeat){
        switch (heartbeat.getOperation()){
            case "INIT":
                for(String entry: replicas){
                    if (entry.equals(ME.getAddr())) continue;
                    AvailableReplicas.remove(entry);
                    broadcast(entry, heartbeat);
                }

                System.out.println("---AVAILABLE REPLICAS---");
                System.out.println(AvailableReplicas);
                if(potentialLeader.isEmpty() || AvailableReplicas.isEmpty()){
                    LEADER = ME;
                } else if (potentialLeader.size() == 1){
                    LEADER = potentialLeader.iterator().next();
                    broadcast(LEADER.getAddr(), DataStore.Heartbeat.newBuilder().setFrom(ME).setOperation("SYNC-LOGS").build());
                } else {
                    sendHeartbeat(Replicas, DataStore.Heartbeat.newBuilder().setFrom(ME).setOperation("RE-ELECTION").build());
                }

                System.out.println("LEADER: ");
                System.out.println(LEADER);
                break;
            case "RE-ELECTION":
                for(String entry: replicas){
                    if (entry.equals(ME.getAddr())) continue;
                    AvailableReplicas.remove(entry);
                    broadcast(entry, heartbeat);
                }
                if(AvailableReplicas.isEmpty()){
                    LEADER = ME;
                }
                break;
            case "IWON":
                for(String entry: replicas){
                    if (entry.equals(ME.getAddr())) continue;
                    AvailableReplicas.remove(entry);
                    broadcast(entry, heartbeat);
                }
                break;
            case "READ":
                String key = heartbeat.getData(0).getKey();
                List<List<String>> combination = subsets();
                int index = 0;
                for(List<String> com: combination){
                    receiveFromAllReplicas = true;
                    System.out.println((++index) + " TIME(S)\nTRY " + Nr + "NODES: " + com);
                    for(String target: com){
                        if(target.equals(ME.getAddr()))
                            quorum_result.add(store.containsKey(key)?(key+":"+store.get(key)):"null:Key does not exist");
                        else {
                            AvailableReplicas.remove(target);
                            broadcast(target, heartbeat);
                        }
                    }

                    if (quorum_result.size() == 1 && receiveFromAllReplicas) {
                        break;
                    }
                    quorum_result.clear();
                }
                break;
            case "UPDATE":
                commit = 1;
                isCommitted = false;
                for(String entry: replicas){
                    if (entry.equals(ME.getAddr())) continue;
                    AvailableReplicas.remove(entry);
                    broadcast(entry, heartbeat);
                }
                if (commit >= Nw) {
                    System.out.println("---READY TO COMMIT---");
                    // leader commit update
                    store.put(heartbeat.getData(0).getKey(), heartbeat.getData(0).getVal());
                    clock.put(ME.getAddr(), clock.get(ME.getAddr())+1);
                    storeLog(heartbeat.getData(0));
                    heartbeat = formatClocks(heartbeat);
                    // broadcast to others
                    this.sendHeartbeat(Replicas, heartbeat.toBuilder().setOperation("COMMIT").build());
                } else {
                    System.out.println("---NOT READY TO COMMIT---");
                }
                break;
            case "COMMIT":
                for(String entry: replicas){
                    if (entry.equals(ME.getAddr())) continue;
                    System.out.println();
                    AvailableReplicas.remove(entry);
                    broadcast(entry, heartbeat);
                }
                break;
            default:
                System.out.println("INVALID OPERATION");

        }
    }

    private DataStore.Heartbeat formatClocks(DataStore.Heartbeat heartbeat) {
        for(Map.Entry<String, Long> c: clock.entrySet()){
            heartbeat = heartbeat.toBuilder().addClocks(DataStore.Clock.newBuilder().setClock(c.getValue()).setNode(DataStore.Node.newBuilder().setAddr(c.getKey()).build()).build()).build();
        }
        return heartbeat;
    }

    private void broadcast(String target, DataStore.Heartbeat heartbeat) {
        String host = target.split(":")[0];
        int port = Integer.parseInt(target.split(":")[1]);
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        DataServiceGrpc.DataServiceBlockingStub heartBeatServiceStub = DataServiceGrpc.newBlockingStub(channel);

        long start = System.currentTimeMillis();
        System.out.println("---HEARTBEAT TO "+ target +"---");
        System.out.println(heartbeat);
        switch (heartbeat.getOperation()){
            case "INIT":
                try {
                    DataStore.Heartbeat res = heartBeatServiceStub.sendHeartbeats(heartbeat); //.withDeadlineAfter(3000, TimeUnit.MILLISECONDS)
                    AvailableReplicas.put(res.getFrom().getAddr(), res.getFrom().getId());
                    potentialLeader.add(res.getLeader());
                } catch (Exception e){
                    System.out.println("---INITIAL HEARTBEAT ERROR---");
                    System.out.println(e.getMessage());
                }
                channel.shutdown();
                break;
            case "SYNC-LOGS":
                try {
                    DataStore.Log lastLog = logs.peekFirst();
                    if (lastLog == null){
                        lastLog = DataStore.Log.newBuilder().build();
                        for(Map.Entry<String, Long> c: clock.entrySet()){
                            lastLog = lastLog.toBuilder().putClocks(c.getKey(), DataStore.Clock.newBuilder().setClock(c.getValue()).build()).build();
                        }
                    }
                    heartbeat = heartbeat.toBuilder().addLogs(lastLog).build();
                    DataStore.Heartbeat res = heartBeatServiceStub.sendHeartbeats(heartbeat); //.withDeadlineAfter(3000, TimeUnit.MILLISECONDS)
                    for(int j = 0; j < res.getLogsList().size(); ++j){
                        DataStore.Log log = res.getLogs(j);
                        store.put(log.getData().getKey(), log.getData().getVal());
                        int index = 1;
                        for(Map.Entry<String, DataStore.Clock> cl: log.getClocksMap().entrySet()){
                            if(cl.getKey().equals(ME.getAddr()))  clock.put(cl.getKey(), cl.getValue().getClock()+(index++));
                            else  clock.put(cl.getKey(), cl.getValue().getClock());
                        }
                    }
                    storeLog(res.getLogs(0).getData());
                    System.out.println("---SYNC FROM LEADER---");
                    System.out.println("---STORE---");
                    System.out.println(store);
                    System.out.println("---CLOCK---");
                    System.out.println(clock);
                    System.out.println("---LOGS---");
                    System.out.println(logs);
                } catch (Exception e){
                    System.out.println("---SYNC LOGS HEARTBEAT ERROR---");
                    System.out.println(e.getMessage());
                }
                channel.shutdown();
                break;
            case "RE-ELECTION":
                try {
                    DataStore.Heartbeat res = heartBeatServiceStub.leaderElection(heartbeat); //.withDeadlineAfter(3000, TimeUnit.MILLISECONDS)
                    AvailableReplicas.put(res.getFrom().getAddr(), res.getFrom().getId());
                } catch (Exception e){
                    System.out.println("---RE-ELECTION FAIL---");
                    System.out.println(e.getMessage());
                }
                channel.shutdown();
                break;
            case "IWON":
                try {
                    DataStore.Heartbeat res = heartBeatServiceStub.withDeadlineAfter(5000, TimeUnit.MILLISECONDS).leaderElection(heartbeat);
                    AvailableReplicas.put(res.getFrom().getAddr(), res.getFrom().getId());
                } catch (Exception e){
                    System.out.println("---NEW LEADER ERROR---");
                    System.out.println(e.getMessage());
                }
                channel.shutdown();
                break;
            case "READ":
                try {
                    DataStore.Heartbeat res = heartBeatServiceStub.sendQuorumRequest(heartbeat);
                    AvailableReplicas.put(res.getFrom().getAddr(), res.getFrom().getId());
                    quorum_result.add(res.getData(0).getKey() + ":" + res.getData(0).getVal());
                } catch (Exception e){
                    System.out.println("---READ ERROR---");
                    System.out.println(e.getMessage());
                    receiveFromAllReplicas = false;
                    channel.shutdown();
                    break;
                }
                channel.shutdown();
                break;
            case "REDIRECT-TO-LEADER":
                isReElection = false;
                try {
                    setVectorClock(heartbeat);
                    DataStore.Heartbeat res = heartBeatServiceStub.sendHeartbeats(heartbeat.toBuilder().setOperation("UPDATE").build());

                    // Leader is not crashed
                    AvailableReplicas.put(res.getFrom().getAddr(), res.getFrom().getId());
                    if (res.getOperation().equals("cannot commit")) isCommitted = false;
                    else if (res.getOperation().equals("cannot commit since conflict")) System.out.println(res);
                    else System.out.println(res.getOperation());
                } catch (Exception e){ // leader is crashed
                    //  RE-ELECTION
                    System.out.println("---LEADER UNRESPONSIVE---");
                    isReElection = true;
                    sendHeartbeat(Replicas, DataStore.Heartbeat.newBuilder().setFrom(ME).setOperation("RE-ELECTION").build());
                    System.out.println(e.getMessage());
                    break;
                }
                break;
            case "UPDATE":
                try {
                    DataStore.Heartbeat res = heartBeatServiceStub.sendQuorumRequest(heartbeat);
                    AvailableReplicas.put(res.getFrom().getAddr(), res.getFrom().getId());
                    if (res.getOperation().equals("ready")) {
                        System.out.println("---"+ target +" READY TO UPDATE---");
                        commit++;
                    }
                } catch (Exception e){
                    System.out.println("---"+ target +" NOT READY TO UPDATE---");
                    System.out.println(e.getMessage());
                }
                break;
            case "COMMIT":
                try {
                    DataStore.Heartbeat res = heartBeatServiceStub.sendQuorumRequest(heartbeat);
                    AvailableReplicas.put(res.getFrom().getAddr(), res.getFrom().getId());
                    System.out.println("---COMMIT RESPONSE---");
                    System.out.println(res);
                    syncClock(res.getClocksList());
                } catch (Exception e){
                    System.out.println("---"+ target +" COMMIT FAIL---");
                    System.out.println(e.getMessage());
                }
                break;
            default:
                System.out.println("INVALID OPERATION");
        }

        System.out.println("--- " + ((double)(System.currentTimeMillis()-start))/1000 + "s ---");
        System.out.println();
    }

    private void redirectToLeader(DataStore.Data data) {
        if(LEADER.getAddr().equals(ME.getAddr()))
            sendHeartbeat(Replicas, DataStore.Heartbeat.newBuilder().setFrom(ME).addData(0, data).setOperation("UPDATE").build());
        else
            broadcast(LEADER.getAddr(), DataStore.Heartbeat.newBuilder().setFrom(ME).addData(0, data).setOperation("REDIRECT-TO-LEADER").build());
    }

    private void checkConflict(DataStore.Heartbeat heartbeat) {
        boolean isGreater = false, isLess = false;
        isConflict = false;

        // either all local clocks <= received or received <= local
        for(DataStore.Clock remote_clock: heartbeat.getClocksList()){
            if(clock.get(remote_clock.getNode().getAddr()) < remote_clock.getClock()){ // local < received
                if(isGreater){
                    isConflict = true;
                    break;
                } else {
                    isLess = true;
                }
            } else if(clock.get(remote_clock.getNode().getAddr()) > remote_clock.getClock()){ // local > received
                if(isLess){
                    isConflict = true;
                    break;
                } else {
                    isGreater = true;
                }
            }
        }

        // if no conflict update clock and update value
        if(!isConflict) {
            syncClock(heartbeat.getClocksList());
        }

        System.out.println(ME.getAddr() +" current clock" + clock);
    }

    private void syncClock(List<DataStore.Clock> clocks) {
        clock.put(ME.getAddr(), clock.get(ME.getAddr())+1);
        for(DataStore.Clock remote_clock: clocks)
            clock.put(remote_clock.getNode().getAddr(), Math.max(remote_clock.getClock(), clock.get(remote_clock.getNode().getAddr())));
        System.out.println("---CURRENT CLOCKS---");
        System.out.println(clock);
    }


//    @Override
//    public void propagate(DataStore.Vector_clock clocks, StreamObserver<DataStore.Server> responseObserver) {
//        DataStore.Server conflict = DataStore.Server.newBuilder().setId(ME.getAddr()).setData(DataStore.Data.newBuilder().setKey("No conflict").build()).build();        boolean isGreater = false, isLess = false, isConflict = false;
//
//        // either all local clocks <= received or received <= local
//        for(DataStore.Server server: clocks.getClocksList()){
//            if(clock.get(server.getId()) < server.getClock()){ // local < received
//                if(isGreater){
//                    conflict = conflict.toBuilder().setId(ME.getAddr())
//                            .setData(DataStore.Data.newBuilder().setKey(server.getData().getKey()).setVal(store.getOrDefault(server.getData().getKey(), "conflict")).build())
//                            .build();
//                    isConflict = true;
//                    break;
//                } else {
//                    isLess = true;
//                }
//            } else if(clock.get(server.getId()) > server.getClock()){ // local > received
//                if(isLess){
//                    conflict = conflict.toBuilder().setId(ME.getAddr())
//                            .setData(DataStore.Data.newBuilder().setKey(server.getData().getKey()).setVal(store.getOrDefault(server.getData().getKey(), "conflict")).build())
//                            .build();
//                    isConflict = true;
//                    break;
//                } else {
//                    isGreater = true;
//                }
//            }
//
//        }
//
//        // if no conflict update clock and update value
//        if(!isConflict) {
//            store.put(clocks.getClocks(0).getData().getKey(), clocks.getClocks(0).getData().getVal());
////            clock.put(HOST+":"+PORT, clock.get(HOST+":"+PORT)+1);
//            for(DataStore.Server server: clocks.getClocksList())
//                clock.put(server.getId(), Math.max(server.getClock(), clock.get(server.getId())));
//        }
//
//        System.out.println(ME.getAddr() +" current clock" + clock);
//
//        responseObserver.onNext(conflict);
//        responseObserver.onCompleted();
//    }

    private void propagate(String host, int port){
//        channel = ManagedChannelBuilder.forAddress(host, port)
//                .usePlaintext()
//                .build();
//        DataServiceGrpc.DataServiceBlockingStub dataServiceStub = DataServiceGrpc.newBlockingStub(channel);
//
//        // send local clock to every replicas
//        Iterator<DataStore.Server> res = dataServiceStub.propagate(vector_clock);
//
//        try{
//            while (res.hasNext()){
//                DataStore.Server d = res.next();
//                if(d.getData().getKey().equals("No conflict"))
//                    continue;
//
//                conflict = d;
//            }
//        } catch (StatusRuntimeException e){
//            conflict = null;
//            System.out.println("Server Error: " + host + ":"+ port + " " + e.getMessage());
//        }

    }

    private void setVectorClock(DataStore.Heartbeat heartbeat){
        int index = 0;
        for (Map.Entry<String, Long> entry :clock.entrySet()) {
            heartbeat = heartbeat.toBuilder().addClocks(index++, DataStore.Clock.newBuilder().setNode(DataStore.Node.newBuilder().setAddr(entry.getKey()).build()).setClock(entry.getValue())).build();
        }
    }

    public List<List<String>> subsets() {
        List<List<String>> list = new ArrayList<>();
        List<String> rep = new ArrayList<>(Replicas);
        rep.add(ME.getAddr());
        backtrack(list, new ArrayList<>(), rep, 0);
        return list;
    }

    private void backtrack(List<List<String>> list , List<String> tempList, List<String> rep, int start){
        if(tempList.size() == Nr)list.add(new ArrayList<>(tempList));
        for(int i = start; i < rep.size(); i++){
            tempList.add(rep.get(i));
            backtrack(list, tempList, rep, i + 1);
            tempList.remove(tempList.size() - 1);
        }
    }
}
