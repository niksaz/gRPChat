package ru.spbau.mesau.exchange;

import static org.junit.Assert.assertEquals;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.Test;
import ru.spbau.mesau.Message;

public class MesAUTest {
  @Test
  public void testInteraction() throws Exception {
    Queue<Message> messagesServerReceived = new ConcurrentLinkedQueue<>();
    Message serverMessage =
      Message.newBuilder()
        .setContent("server-content")
        .setFromDateTimestamp(0)
        .setAuthor("server")
        .build();
    MesAUServerRunner runner = new MesAUServerRunner(50051);
    Thread serverThread = new Thread(() -> {
      try {
        Thread runnerThread = new Thread(() -> {
          try {
            runner.run(messagesServerReceived::add);
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
        runnerThread.setDaemon(true);
        runnerThread.start();
        Thread.sleep(10000);
        runner.sendMessage(serverMessage);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    serverThread.start();
    Thread.sleep(5000);

    Queue<Message> messagesClientReceived = new ConcurrentLinkedQueue<>();
    Message clientMessage =
      Message.newBuilder()
        .setContent("client-content")
        .setFromDateTimestamp(0)
        .setAuthor("client")
        .build();
    MesAUClient client = new MesAUClient("localhost", 50051);
    Thread clientThread = new Thread(() -> {
      try {
        client.initiateChat(messagesClientReceived::add);
        client.sendMessage(clientMessage);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    clientThread.start();
    Thread.sleep(10000);

    runner.stop();
    client.shutdown();

    serverThread.join();
    clientThread.join();

    assertEquals(1, messagesClientReceived.size());
    assertEquals(serverMessage, messagesClientReceived.peek());

    assertEquals(1, messagesClientReceived.size());
    assertEquals(clientMessage, messagesServerReceived.peek());
  }
}
