import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.Toolkit.getDefaultToolkit;
import static java.lang.System.exit;
import static java.lang.System.out;
import static java.time.LocalTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static javax.swing.text.StyleConstants.setBackground;
import static javax.swing.text.StyleConstants.setBold;

class Client extends JFrame {
    private static final int ROOT_WIDTH;
    private static final int ROOT_HEIGHT;

    static {
        final Dimension dimensionOfTheScreen;
        dimensionOfTheScreen = getDefaultToolkit().getScreenSize();

        ROOT_WIDTH = dimensionOfTheScreen.width / 2;
        ROOT_HEIGHT = dimensionOfTheScreen.height / 2;
    }

    private static final SimpleAttributeSet ATTRIBUTES_FOR_SENT_MESSAGE;
    private static final SimpleAttributeSet ATTRIBUTES_FOR_RECEIVED_MESSAGE;

    static {
        ATTRIBUTES_FOR_SENT_MESSAGE = new SimpleAttributeSet();
        StyleConstants.setForeground(
            ATTRIBUTES_FOR_SENT_MESSAGE, Color.decode("0x2e8b57")
        );
        StyleConstants.setBold(ATTRIBUTES_FOR_SENT_MESSAGE, true);

        ATTRIBUTES_FOR_RECEIVED_MESSAGE = new SimpleAttributeSet();
        StyleConstants.setForeground(
            ATTRIBUTES_FOR_RECEIVED_MESSAGE, Color.decode("0xff8c00")
        );
        StyleConstants.setBold(ATTRIBUTES_FOR_RECEIVED_MESSAGE, true);
    }

    private final JPanel root;
    private final JTextPane jTextPaneForChat;
    private final Document chatDocument;
    private final JTextArea jTextAreaForTypingMessage;
    private final JButton jButtonForSendingMessage;
    private final JPanel jPanelForTypingAndSendingMessage;

    {
        root = new JPanel();
        jTextPaneForChat = new JTextPane();
        chatDocument = jTextPaneForChat.getStyledDocument();
        jTextAreaForTypingMessage = new JTextArea();
        jButtonForSendingMessage = new JButton();
        jPanelForTypingAndSendingMessage = new JPanel();
    }

    private final String nickname;
    private Connection connection;
    private Session session;
    private Destination groupChat;
    private MessageProducer messageProducer;
    private MessageConsumer messageConsumer;

    Client(final String nickname, final String groupChatName) {
        this.nickname = nickname;

        configureRoot();
        configureClientAsJFrame();
        configureClientAsChatUser(nickname, groupChatName);
    }

    private void configureRoot() {
        root.setPreferredSize(new Dimension(ROOT_WIDTH, ROOT_HEIGHT));
        root.setLayout(new BorderLayout());

        configureJTextPaneForChat();
        configureJTextAreaForTypingMessage();
        configureJButtonForSendingMessage();
        configureJPanelForTypingAndSendingMessage();

        root.add(new JScrollPane(jTextPaneForChat), CENTER);
        root.add(jPanelForTypingAndSendingMessage, SOUTH);
    }

    private void configureJTextPaneForChat() {
        jTextPaneForChat.setPreferredSize(new Dimension(
            (int) (ROOT_WIDTH * 0.9),
            (int) (ROOT_HEIGHT * 0.75)
        ));
        jTextPaneForChat.setEditable(false);
    }

    private void configureJTextAreaForTypingMessage() {
        jTextAreaForTypingMessage.setPreferredSize(new Dimension(
            (int) (ROOT_WIDTH * 0.75),
            ROOT_HEIGHT / 6
        ));
        jTextAreaForTypingMessage.setLineWrap(true);
    }

    private void configureJButtonForSendingMessage() {
        jButtonForSendingMessage.setPreferredSize(new Dimension(
            (int) (ROOT_WIDTH * 0.1),
            ROOT_HEIGHT / 8
        ));
        jButtonForSendingMessage.setText("Send!");
        jButtonForSendingMessage.addActionListener(listener -> {
            final String messageText = jTextAreaForTypingMessage.getText();

            if (!messageText.isBlank()) {
                jTextAreaForTypingMessage.setText("");

                try {
                    final TextMessage textMessage;
                    textMessage = session.createTextMessage();
                    textMessage.setText(nickname + "|" + messageText);

                    messageProducer.send(textMessage);
                } catch (final JMSException jmsException) {
                    jmsException.printStackTrace(out);
                }

                jTextAreaForTypingMessage.requestFocusInWindow();
            }
        });
    }

    private void configureJPanelForTypingAndSendingMessage() {
        jPanelForTypingAndSendingMessage.setPreferredSize(new Dimension(
            ROOT_WIDTH,
            (int) (ROOT_HEIGHT * 0.25)
        ));
        jPanelForTypingAndSendingMessage.add(jTextAreaForTypingMessage);
        jPanelForTypingAndSendingMessage.add(jButtonForSendingMessage);
    }

    private void configureClientAsJFrame() {
        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Logged as: " + nickname);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent closing) {
                try {
                    if (messageProducer != null) messageProducer.close();
                    if (messageConsumer != null) messageConsumer.close();
                    if (session != null) session.close();
                    if (connection != null) connection.close();
                } catch (final JMSException jmsException) {
                    jmsException.printStackTrace(out);
                }
            }
        });
        setVisible(true);

        jTextAreaForTypingMessage.requestFocusInWindow();
    }

    private void configureClientAsChatUser(final String nickname,
                                           final String groupChatName) {
        try {
            final Context initialContext = new InitialContext();

            final ConnectionFactory connectionFactory;
            connectionFactory = (ConnectionFactory) initialContext.lookup(
                "ConnectionFactory"
            );

            connection = connectionFactory.createConnection();

            session = connection.createSession(false, AUTO_ACKNOWLEDGE);

            this.groupChat = (Destination) initialContext.lookup(groupChatName);

            messageProducer = session.createProducer(this.groupChat);
            messageConsumer = session.createDurableSubscriber(
                (Topic) initialContext.lookup(groupChatName),
                nickname
            );
            messageConsumer.setMessageListener(message -> {
                if (message instanceof TextMessage textMessage) {
                    try {
                        final String[] splitMessage = textMessage
                            .getText()
                            .split("\\|", 2);

                        appendMessage(splitMessage[0], splitMessage[1]);
                    } catch (final JMSException jmsException) {
                        jmsException.printStackTrace(out);
                    }
                }
            });

            connection.start();
        } catch (final NamingException | JMSException exception) {
            exception.printStackTrace(out);

            exit(1);
        }
    }

    private synchronized void appendMessage(final String nickname,
                                            final String text) {
        final boolean messageIsSent = nickname.equals(this.nickname);

        final String message = "%s%nFrom: %s%n%s%n%n".formatted(
            now().truncatedTo(SECONDS),
            (messageIsSent) ? "you" : nickname,
            text
        );

        try {
            chatDocument.insertString(
                chatDocument.getLength(),
                message,
                (messageIsSent) ? ATTRIBUTES_FOR_SENT_MESSAGE
                                : ATTRIBUTES_FOR_RECEIVED_MESSAGE
            );
        } catch (final BadLocationException ble) {
            ble.printStackTrace(out);
        }
    }
}