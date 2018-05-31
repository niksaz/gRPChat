package ru.spbau.mesau.exchange;

import static org.junit.Assert.assertEquals;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.spbau.mesau.MesAUGrpc;
import ru.spbau.mesau.Message;

public class MesAUImplTest {
  private MesAUServerRunner serverRunner;
  private ManagedChannel inProcessChannel;
  private String serverName;

  @Before
  public void setUp() throws Exception {
    serverName = "in-process server for " + getClass();
    serverRunner = new MesAUServerRunner(45000);
    inProcessChannel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
  }

  @After
  public void tearDown() throws Exception {
    inProcessChannel.shutdown();
    serverRunner.stop();
  }

  @Test
  public void chatRecevie() throws Exception {
    List<Message> messagesReceived = new ArrayList<>();
    startServer(messagesReceived::add);
    MesAUGrpc.MesAUStub stub = MesAUGrpc.newStub(inProcessChannel);
    StreamObserver<Message> responseObserver = new StreamObserver<Message>() {
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
    StreamObserver<Message> requestObserver = stub.chat(responseObserver);
    requestObserver.onNext(FakeMessages.clientMessage);
    requestObserver.onCompleted();

    assertEquals(1, messagesReceived.size());
    assertEquals(FakeMessages.clientMessage, messagesReceived.get(0));
  }

  @Test
  public void chatSend() throws Exception {
    List<Message> messagesSend = new ArrayList<>();
    startServer(message -> {});
    MesAUGrpc.MesAUStub stub = MesAUGrpc.newStub(inProcessChannel);
    StreamObserver<Message> responseObserver = new StreamObserver<Message>() {
      @Override
      public void onNext(Message value) {
        messagesSend.add(value);
      }
      @Override
      public void onError(Throwable t) {
      }
      @Override
      public void onCompleted() {
      }
    };
    stub.chat(responseObserver);
    serverRunner.sendMessage(FakeMessages.serverMessage);

    assertEquals(1, messagesSend.size());
    assertEquals(FakeMessages.serverMessage, messagesSend.get(0));
  }

  private void startServer(Consumer<Message> messageConsumer) throws IOException {
    serverRunner.run(messageConsumer, InProcessServerBuilder.forName(serverName).directExecutor());
  }
}