import javax.jms.JMSException;

public interface Producer {
    public void publish() throws JMSException;
}
