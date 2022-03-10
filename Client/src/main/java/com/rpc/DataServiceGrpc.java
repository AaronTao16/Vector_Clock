package com.rpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.15.0)",
    comments = "Source: DataStore.proto")
public final class DataServiceGrpc {

  private DataServiceGrpc() {}

  public static final String SERVICE_NAME = "DataService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.rpc.DataStore.Data,
      com.rpc.DataStore.Data> getReadMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "read",
      requestType = com.rpc.DataStore.Data.class,
      responseType = com.rpc.DataStore.Data.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.rpc.DataStore.Data,
      com.rpc.DataStore.Data> getReadMethod() {
    io.grpc.MethodDescriptor<com.rpc.DataStore.Data, com.rpc.DataStore.Data> getReadMethod;
    if ((getReadMethod = DataServiceGrpc.getReadMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getReadMethod = DataServiceGrpc.getReadMethod) == null) {
          DataServiceGrpc.getReadMethod = getReadMethod = 
              io.grpc.MethodDescriptor.<com.rpc.DataStore.Data, com.rpc.DataStore.Data>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "DataService", "read"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.rpc.DataStore.Data.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.rpc.DataStore.Data.getDefaultInstance()))
                  .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("read"))
                  .build();
          }
        }
     }
     return getReadMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.rpc.DataStore.Data,
      com.rpc.DataStore.Server> getAddUpdateMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "add_update",
      requestType = com.rpc.DataStore.Data.class,
      responseType = com.rpc.DataStore.Server.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.rpc.DataStore.Data,
      com.rpc.DataStore.Server> getAddUpdateMethod() {
    io.grpc.MethodDescriptor<com.rpc.DataStore.Data, com.rpc.DataStore.Server> getAddUpdateMethod;
    if ((getAddUpdateMethod = DataServiceGrpc.getAddUpdateMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getAddUpdateMethod = DataServiceGrpc.getAddUpdateMethod) == null) {
          DataServiceGrpc.getAddUpdateMethod = getAddUpdateMethod = 
              io.grpc.MethodDescriptor.<com.rpc.DataStore.Data, com.rpc.DataStore.Server>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "DataService", "add_update"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.rpc.DataStore.Data.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.rpc.DataStore.Server.getDefaultInstance()))
                  .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("add_update"))
                  .build();
          }
        }
     }
     return getAddUpdateMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.rpc.DataStore.Vector_clock,
      com.rpc.DataStore.Server> getPropagateMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "propagate",
      requestType = com.rpc.DataStore.Vector_clock.class,
      responseType = com.rpc.DataStore.Server.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.rpc.DataStore.Vector_clock,
      com.rpc.DataStore.Server> getPropagateMethod() {
    io.grpc.MethodDescriptor<com.rpc.DataStore.Vector_clock, com.rpc.DataStore.Server> getPropagateMethod;
    if ((getPropagateMethod = DataServiceGrpc.getPropagateMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getPropagateMethod = DataServiceGrpc.getPropagateMethod) == null) {
          DataServiceGrpc.getPropagateMethod = getPropagateMethod = 
              io.grpc.MethodDescriptor.<com.rpc.DataStore.Vector_clock, com.rpc.DataStore.Server>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "DataService", "propagate"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.rpc.DataStore.Vector_clock.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.rpc.DataStore.Server.getDefaultInstance()))
                  .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("propagate"))
                  .build();
          }
        }
     }
     return getPropagateMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static DataServiceStub newStub(io.grpc.Channel channel) {
    return new DataServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static DataServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new DataServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static DataServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new DataServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class DataServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void read(com.rpc.DataStore.Data request,
        io.grpc.stub.StreamObserver<com.rpc.DataStore.Data> responseObserver) {
      asyncUnimplementedUnaryCall(getReadMethod(), responseObserver);
    }

    /**
     */
    public void addUpdate(com.rpc.DataStore.Data request,
        io.grpc.stub.StreamObserver<com.rpc.DataStore.Server> responseObserver) {
      asyncUnimplementedUnaryCall(getAddUpdateMethod(), responseObserver);
    }

    /**
     */
    public void propagate(com.rpc.DataStore.Vector_clock request,
        io.grpc.stub.StreamObserver<com.rpc.DataStore.Server> responseObserver) {
      asyncUnimplementedUnaryCall(getPropagateMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getReadMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.rpc.DataStore.Data,
                com.rpc.DataStore.Data>(
                  this, METHODID_READ)))
          .addMethod(
            getAddUpdateMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                com.rpc.DataStore.Data,
                com.rpc.DataStore.Server>(
                  this, METHODID_ADD_UPDATE)))
          .addMethod(
            getPropagateMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                com.rpc.DataStore.Vector_clock,
                com.rpc.DataStore.Server>(
                  this, METHODID_PROPAGATE)))
          .build();
    }
  }

  /**
   */
  public static final class DataServiceStub extends io.grpc.stub.AbstractStub<DataServiceStub> {
    private DataServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private DataServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DataServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new DataServiceStub(channel, callOptions);
    }

    /**
     */
    public void read(com.rpc.DataStore.Data request,
        io.grpc.stub.StreamObserver<com.rpc.DataStore.Data> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getReadMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void addUpdate(com.rpc.DataStore.Data request,
        io.grpc.stub.StreamObserver<com.rpc.DataStore.Server> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getAddUpdateMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void propagate(com.rpc.DataStore.Vector_clock request,
        io.grpc.stub.StreamObserver<com.rpc.DataStore.Server> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getPropagateMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class DataServiceBlockingStub extends io.grpc.stub.AbstractStub<DataServiceBlockingStub> {
    private DataServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private DataServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DataServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new DataServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.rpc.DataStore.Data read(com.rpc.DataStore.Data request) {
      return blockingUnaryCall(
          getChannel(), getReadMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<com.rpc.DataStore.Server> addUpdate(
        com.rpc.DataStore.Data request) {
      return blockingServerStreamingCall(
          getChannel(), getAddUpdateMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<com.rpc.DataStore.Server> propagate(
        com.rpc.DataStore.Vector_clock request) {
      return blockingServerStreamingCall(
          getChannel(), getPropagateMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class DataServiceFutureStub extends io.grpc.stub.AbstractStub<DataServiceFutureStub> {
    private DataServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private DataServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DataServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new DataServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.rpc.DataStore.Data> read(
        com.rpc.DataStore.Data request) {
      return futureUnaryCall(
          getChannel().newCall(getReadMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_READ = 0;
  private static final int METHODID_ADD_UPDATE = 1;
  private static final int METHODID_PROPAGATE = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final DataServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(DataServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_READ:
          serviceImpl.read((com.rpc.DataStore.Data) request,
              (io.grpc.stub.StreamObserver<com.rpc.DataStore.Data>) responseObserver);
          break;
        case METHODID_ADD_UPDATE:
          serviceImpl.addUpdate((com.rpc.DataStore.Data) request,
              (io.grpc.stub.StreamObserver<com.rpc.DataStore.Server>) responseObserver);
          break;
        case METHODID_PROPAGATE:
          serviceImpl.propagate((com.rpc.DataStore.Vector_clock) request,
              (io.grpc.stub.StreamObserver<com.rpc.DataStore.Server>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class DataServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    DataServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.rpc.DataStore.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("DataService");
    }
  }

  private static final class DataServiceFileDescriptorSupplier
      extends DataServiceBaseDescriptorSupplier {
    DataServiceFileDescriptorSupplier() {}
  }

  private static final class DataServiceMethodDescriptorSupplier
      extends DataServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    DataServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (DataServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new DataServiceFileDescriptorSupplier())
              .addMethod(getReadMethod())
              .addMethod(getAddUpdateMethod())
              .addMethod(getPropagateMethod())
              .build();
        }
      }
    }
    return result;
  }
}
