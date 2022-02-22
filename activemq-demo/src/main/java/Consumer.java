import javax.jms.JMSException;
import java.io.IOException;

public interface Consumer {
    public void subscribe() throws JMSException, IOException;
    public void unSubscribe() throws JMSException, IOException;
}
