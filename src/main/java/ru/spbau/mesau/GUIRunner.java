package ru.spbau.mesau;

import io.grpc.Status;
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
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
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
  private static final Dimension FRAME_DIMENSION = new Dimension(480, 480);

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
    editorPane.setEditable(false);

    JTextField messageField = createMessageSendingField(editorPane);

    beServerButton.addActionListener(action -> {
      beServerButton.setEnabled(false);
      beClientButton.setVisible(false);
      messageField.setEnabled(true);
      new Thread(() -> {
        String portToRunOn = askForInput("Enter the port number for the server:", PORT_TO_RUN_ON);
        MesAUServerRunner runner = new MesAUServerRunner(Integer.valueOf(portToRunOn));
        serviceStrategy = new ServerGUIServiceStrategy(runner);
        Runtime.getRuntime().addShutdownHook(new Thread(runner::stop));
        try {
          runner.run(message -> addMessageTo(editorPane, message));
        } catch (IOException e) {
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
          String host = askForInput("Enter the hostname of the server:", "localhost");
          String port = askForInput("Enter the port number of the server:", PORT_TO_RUN_ON);
          MesAUClient client = new MesAUClient(host, Integer.valueOf(port));
          Runtime.getRuntime().addShutdownHook(new Thread(client::shutdown));
          client.initiateChat(message -> addMessageTo(editorPane, message));
          serviceStrategy = new ClientGUIServiceStrategy(client);
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
    JScrollPane scrollPane = new JScrollPane(editorPane);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    pane.add(scrollPane);
  }

  private static JTextField createMessageSendingField(JEditorPane editorPane) {
    JTextField messageField = new JTextField();
    messageField.setEnabled(false);
    messageField.setMaximumSize(new Dimension(Integer.MAX_VALUE, messageField.getPreferredSize().height));
    messageField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && !messageField.getText().isEmpty()) {
          String text = messageField.getText();
          messageField.setText("");
          Message message = formMessageFromGUIContext(text);
          addMessageTo(editorPane, message);
          serviceStrategy.sendMessage(message);
        }
      }
    });
    return messageField;
  }

  private static String askForInput(String prompt, Object defaultValue) {
    JFrame dialogFrame = new JFrame();
    String result = JOptionPane.showInputDialog(dialogFrame, prompt);
    if (result == null || result.isEmpty()) {
      return defaultValue.toString();
    } else {
      return result;
    }
  }

  private static Message formMessageFromGUIContext(String content) {
    return Message.newBuilder()
      .setContent(content)
      .setAuthor(name)
      .setFromDateTimestamp(System.currentTimeMillis())
      .build();
  }

  private static void createAndShowGui() {
    JFrame frame = new JFrame("MesAU");
    frame.setMinimumSize(FRAME_DIMENSION);

    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    Container pane = frame.getContentPane();
    fillFramePane(pane);

    frame.setVisible(true);

    name = askForInput("What is your Name?", System.getProperty("user.name"));
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(GUIRunner::createAndShowGui);
  }
}
