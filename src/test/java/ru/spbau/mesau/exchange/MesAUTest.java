package ru.spbau.mesau.exchange;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import ru.spbau.mesau.Message;

public class MesAUTest {
  @Test
  public void testInteraction() throws Exception {
    List<Message> messagesServerReceived = new ArrayList<>();
    MesAUServerRunner runner = new MesAUServerRunner(50051);
    Thread serverThread = new Thread(() -> {
      try {
        runner.run(messagesServerReceived::add);
        Thread.sleep(10000);
        runner.sendMessage(FakeMessages.serverMessage);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    serverThread.start();
    Thread.sleep(5000);

    List<Message> messagesClientReceived = new ArrayList<>();
    MesAUClient client = new MesAUClient("localhost", 50051);
    Thread clientThread = new Thread(() -> {
      try {
        client.initiateChat(messagesClientReceived::add);
        client.sendMessage(FakeMessages.clientMessage);
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
    assertEquals(FakeMessages.serverMessage, messagesClientReceived.get(0));

    assertEquals(1, messagesClientReceived.size());
    assertEquals(FakeMessages.clientMessage, messagesServerReceived.get(0));
  }
}
