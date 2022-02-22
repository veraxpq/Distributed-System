import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.util.Time;

import javax.jms.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MailboxConsumer implements Consumer {
    private static String topicName = "MailBox";
    private static Connection connection = null;
    private static Session session = null;
    private static MessageConsumer consumer = null;
    private static BufferedReader reader = null;
    private String clientId;

    public void create(String clientId) throws JMSException {
        this.clientId = clientId;
        //create a connectionFactory
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("failover://tcp://localhost:61616");

        //create a connection
        connection = connectionFactory.createConnection();
        connection.setClientID(clientId);

        //create a session
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        //create the destination
        Topic topic = session.createTopic(topicName);

        //create a message consumer from the session to the topic
        consumer = session.createDurableSubscriber(topic, topicName);

        //start the connection
        connection.start();
    }

    @Override
    public void subscribe() throws JMSException, IOException {
        create("subscriber");
        try {
            while (true) {
                System.out.println("Do you want to receive message from Mailbox? ");
                String str = reader.readLine();

                if (str.toLowerCase().equals("yes")) {
                    //wait for a message
                    Message message = consumer.receive(10000);

                    if (message != null && message instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) message;
                        String text = textMessage.getText();
                        System.out.println("received from Mailbox: " + text);
                    }
                } else {
                    System.out.println("Do you want to unsubscribe the service?");
                    String ans = reader.readLine();
                    if (ans.toLowerCase().equals("yes")) {
                        unSubscribe();
                        return;
                    }
                }
            }

        } catch (JMSException | IOException e) {
            e.printStackTrace();
        } finally {
            unSubscribe();
        }
    }

    @Override
    public void unSubscribe() throws JMSException, IOException {
        if (consumer != null) {
            consumer.close();
        }
        if (session != null) {
            session.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    public static void main(String[] args) throws JMSException, IOException {
        reader = new BufferedReader(new InputStreamReader(System.in));
        MailboxConsumer consumer = new MailboxConsumer();
        try {
            while (true) {
                System.out.println("Do you want to subscribe the MailBox service? (Please input yes or no)");
                String answer = reader.readLine();
                if (answer.toLowerCase().equals("yes")) {
                    consumer.subscribe();
                } else {
                    consumer.unSubscribe();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
