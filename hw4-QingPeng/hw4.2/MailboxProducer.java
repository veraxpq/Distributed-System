import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MailboxProducer implements Producer{
    static {
        System.out.println("MESSAGE CLASSLOADER IN JMSMANAGER:" +
                javax.jms.Message.class.getProtectionDomain().getCodeSource().getLocation());
    }

    private static Connection connection = null;
    private static Session session = null;
    private static BufferedReader reader = null;
    private String clientId = "";
    private MessageProducer producer = null;

    private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;

    private static String topicName = "MailBox";

    public void create() throws JMSException {
        this.clientId = clientId;
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("failover://tcp://localhost:61616");

        //create a connection
        connection = connectionFactory.createConnection();
//        connection.setClientID(clientId);

        //create a session
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        //create the destination
        Topic topic = session.createTopic(topicName);

        //create a message producer from the session to the topic
        producer = session.createProducer(topic);

        connection.start();
    }

    @Override
    public void publish() throws JMSException {
        try {
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            reader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                System.out.println("Enter the message you want to send: ");
                String str = reader.readLine();

                //create a messages
                TextMessage message = session.createTextMessage(str);

                //send the message
                System.out.println("Sent message: "+ message.hashCode() + " : " + Thread.currentThread().getName());
                producer.send(message);
            }

        } catch (JMSException | IOException e) {
            e.printStackTrace();
        } finally {
            //close the connection
            if (session != null) {
                session.close();
            }
            if(connection != null) {
                connection.close();
            }
        }
    }

    public static void main(String[] args) throws JMSException {
        MailboxProducer producer = new MailboxProducer();
        producer.create();
        producer.publish();
    }
}
