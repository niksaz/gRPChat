package ru.spbau.mesau.exchange;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;
import ru.spbau.mesau.MesAUGrpc;
import ru.spbau.mesau.MesAUGrpc.MesAUStub;
import ru.spbau.mesau.Message;

/** Class that connects and communicates with the service server. */
public class MesAUClient {
  private static final Logger logger = Logger.getLogger(MesAUClient.class.getName());

  private final ManagedChannel channel;
  private final MesAUStub asyncStub;

  private StreamObserver<Message> responseObserver;

  public MesAUClient(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
  }

  MesAUClient(ManagedChannelBuilder channel) {
    this.channel = channel.build();
    this.asyncStub = MesAUGrpc.newStub(this.channel);
  }

  public void shutdown() {
    try {
      logger.info("shutting down client's channel");
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException ignored) {
    }
  }

  public void initiateChat(Consumer<Message> incomingMessageConsumer) {
    IncomingStreamObserver incomingStreamObserver =
      new IncomingStreamObserver(incomingMessageConsumer);
    responseObserver = asyncStub.chat(incomingStreamObserver);
  }

  public void sendMessage(Message message) {
    if (responseObserver != null) {
      logger.info("sending message: " + message);
      responseObserver.onNext(message);
    }
  }
}
