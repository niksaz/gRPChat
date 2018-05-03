package ru.spbau.mesau.exchange;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Logger;
import ru.spbau.mesau.Message;

/** Class that runs the server with the service bound to its port. */
public class MesAUServerRunner {
  private static final Logger logger = Logger.getLogger(MesAUServerRunner.class.getName());

  private final int port;
  private Server server;
  private MesAUImpl mesAU;

  public MesAUServerRunner(int port) {
    this.port = port;
  }

  public void run(Consumer<Message> messageConsumer) throws IOException, InterruptedException {
    mesAU = new MesAUImpl(messageConsumer);
    server = ServerBuilder.forPort(port).addService(mesAU).build().start();
    server.awaitTermination();
  }

  public void sendMessage(Message message) {
    if (mesAU != null) {
      mesAU.sendMessage(message);
    }
  }

  public void stop() {
    logger.info("*** shutting down gRPC server");
    if (server != null) {
      server.shutdown();
    }
    logger.info("*** server shut down");
  }
}
