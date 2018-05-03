package ru.spbau.mesau.exchange;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import ru.spbau.mesau.MesAUGrpc;
import ru.spbau.mesau.MesAUGrpc.MesAUStub;
import ru.spbau.mesau.Message;

public class MesAUClient {
  private final ManagedChannel channel;
  private final MesAUStub asyncStub;

  public MesAUClient(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build());
  }

  private MesAUClient(ManagedChannel channel) {
    this.channel = channel;
    this.asyncStub = MesAUGrpc.newStub(channel);
  }

  public void shutdown() {
    try {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException ignored) {
    }
  }

  public StreamObserver<Message> initiateChat(Consumer<Message> incomingMessageConsumer) {
    IncomingStreamObserver incomingStreamObserver =
      new IncomingStreamObserver(incomingMessageConsumer);
    return asyncStub.chat(incomingStreamObserver);
  }
}
