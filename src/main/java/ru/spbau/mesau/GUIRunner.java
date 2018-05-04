package ru.spbau.mesau;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import ru.spbau.mesau.exchange.MesAUClient;
import ru.spbau.mesau.exchange.MesAUServerRunner;

/** GUI for the messaging app. */
public class GUIRunner {
  private static final Logger logger = Logger.getLogger(GUIRunner.class.getName());
  private static final int PORT_TO_RUN_ON = 50051;
  private static final int WIDTH = 480;
  private static final int HEIGHT = 480;

  private static volatile GUIServiceStrategy serviceStrategy;
  private static String name;

  private static void addMessageTo(JEditorPane editorPane, Message message) {
    try {
      Document document = editorPane.getDocument();
      Date date = new Date(message.getFromDateTimestamp());
      DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
      String formattedDate = df.format(date);
      String formattedMessage =
        "[" + formattedDate + "] "
        + message.getAuthor() + ": " + message.getContent() + '\n';
      document.insertString(document.getLength(), formattedMessage, null);
    } catch (BadLocationException ignored) {
    }
  }

  private static void fillFramePane(Container pane) {
    JButton beServerButton = new JButton("Become server");
    JButton beClientButton = new JButton("Connect to someone");
    JEditorPane editorPane = new JEditorPane();
    editorPane.setEnabled(false);

    JTextField messageField = new JTextField();
    messageField.setEnabled(false);
    messageField.setMaximumSize(new Dimension(WIDTH, messageField.getPreferredSize().height));
    messageField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          String text = messageField.getText();
          messageField.setText("");
          Message message = formMessageFromGUIContext(text);
          addMessageTo(editorPane, message);
          serviceStrategy.sendMessage(message);
        }
      }
    });

    beServerButton.addActionListener(action -> {
      beServerButton.setEnabled(false);
      beClientButton.setVisible(false);
      messageField.setEnabled(true);
      new Thread(() -> {
        MesAUServerRunner runner = new MesAUServerRunner(PORT_TO_RUN_ON);
        serviceStrategy = new ServerGUIServiceStrategy(runner);
        Runtime.getRuntime().addShutdownHook(new Thread(runner::stop));
        try {
          runner.run(message -> addMessageTo(editorPane, message));
        } catch (IOException | InterruptedException e) {
          logger.log(Level.WARNING, "Server failed", Status.fromThrowable(e));
        }
      }).start();
    });

    beClientButton.addActionListener(action -> {
      beServerButton.setVisible(false);
      beClientButton.setEnabled(false);
      messageField.setEnabled(true);
      new Thread(() -> {
        try {
          MesAUClient client = new MesAUClient("localhost", PORT_TO_RUN_ON);
          Runtime.getRuntime().addShutdownHook(new Thread(client::shutdown));
          StreamObserver<Message> responseStreamObserver =
            client.initiateChat(message -> addMessageTo(editorPane, message));
          serviceStrategy = new ClientGUIServiceStrategy(responseStreamObserver);
        } catch (Exception e) {
          logger.log(Level.WARNING, "Server failed", Status.fromThrowable(e));
        }
      }).start();
    });

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(beServerButton);
    panel.add(beClientButton);

    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    pane.add(panel);
    pane.add(messageField);
    pane.add(editorPane);
  }

  private static Message formMessageFromGUIContext(String content) {
    return Message.newBuilder()
      .setContent(content)
      .setAuthor(name)
      .setFromDateTimestamp(System.currentTimeMillis())
      .build();
  }

  private static void createAndShowGui() {
    JFrame frame = new JFrame();
    frame.setSize(WIDTH, HEIGHT);

    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    Container pane = frame.getContentPane();
    fillFramePane(pane);

    frame.setVisible(true);

    JFrame dialogFrame = new JFrame("InputDialog Example #1");
    name = JOptionPane.showInputDialog(dialogFrame, "What is your Name?");
    if (name == null) {
      name = System.getProperty("user.name");
    }
  }

  public static void main(String[] args) {
    System.out.println("Timestamp: " + System.currentTimeMillis());
    SwingUtilities.invokeLater(GUIRunner::createAndShowGui);
  }
}
