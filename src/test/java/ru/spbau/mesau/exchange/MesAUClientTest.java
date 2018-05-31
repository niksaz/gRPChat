package ru.spbau.mesau.exchange;

import static org.junit.Assert.assertEquals;

import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.util.MutableHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.spbau.mesau.MesAUGrpc.MesAUImplBase;
import ru.spbau.mesau.Message;

public class MesAUClientTest {
  private final MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();
  private Server fakeServer;
  private MesAUClient client;

  @Before
  public void setUp() throws Exception {
    String uniqueServerName = "fake server for " + getClass();

    // use a mutable service registry for later registering the service impl for each test case.
    fakeServer = InProcessServerBuilder.forName(uniqueServerName)
      .fallbackHandlerRegistry(serviceRegistry).directExecutor().build().start();
    client =
      new MesAUClient(InProcessChannelBuilder.forName(uniqueServerName).directExecutor());
  }

  @After
  public void tearDown() throws Exception {
    client.shutdown();
    fakeServer.shutdown();
  }

  @Test
  public void chatSend() throws Exception {
    List<Message> messagesDelivered = new ArrayList<>();
    MesAUImplBase fakeServerImpl = new MesAUImplBase() {
      @Override
      public StreamObserver<Message> chat(StreamObserver<Message> responseObserver) {
        return new StreamObserver<Message>() {
          @Override
          public void onNext(Message value) {
            messagesDelivered.add(value);
          }
          @Override
          public void onError(Throwable t) {
          }
          @Override
          public void onCompleted() {
          }
        };
      }
    };
    serviceRegistry.addService(fakeServerImpl);

    client.initiateChat(message -> {});
    client.sendMessage(FakeMessages.clientMessage);
    assertEquals(1, messagesDelivered.size());
    assertEquals(FakeMessages.clientMessage, messagesDelivered.get(0));
  }

  @Test
  public void chatReceive() throws Exception {
    MesAUImplBase fakeServerImpl = new MesAUImplBase() {
      @Override
      public StreamObserver<Message> chat(StreamObserver<Message> responseObserver) {
        responseObserver.onNext(FakeMessages.serverMessage);
        return new StreamObserver<Message>() {
          @Override
          public void onNext(Message value) {
          }
          @Override
          public void onError(Throwable t) {
          }
          @Override
          public void onCompleted() {
          }
        };
      }
    };
    serviceRegistry.addService(fakeServerImpl);

    List<Message> messagesReceived = new ArrayList<>();
    client.initiateChat(messagesReceived::add);
    assertEquals(1, messagesReceived.size());
    assertEquals(FakeMessages.serverMessage, messagesReceived.get(0));
  }
}
