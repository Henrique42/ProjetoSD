import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class CentralControl {
	
	private static final String EXCHANGE_NAME = "geral";
	private final static String QUEUE_NAME = "central";
	
	public static void main(String[] args) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("127.0.0.1");
		factory.setUsername("guest");
		factory.setPassword("guest");
		factory.setVirtualHost("/");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		
		channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
		
		receptor(channel);
		
		//channel.close();
		//connection.close();
	}
	
	private static void receptor(Channel channel) throws Exception {
		String nomeFila = channel.queueDeclare().getQueue();
		channel.queueBind(nomeFila, EXCHANGE_NAME, "");
		
		String mensagem;
		mensagem = " [!] A central foi inicializada!";
		channel.basicPublish(EXCHANGE_NAME, "", null, mensagem.getBytes("UTF-8"));
		
		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
			String message = new String(delivery.getBody(), "UTF-8");
			System.out.println(message);
		};
		
		// fanout
		channel.basicConsume(nomeFila, true, deliverCallback, consumerTag -> {});
		// direct
		//channel.basicConsume(username, true, deliverCallback, consumerTag -> {});
	}

}