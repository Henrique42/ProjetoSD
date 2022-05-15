import java.util.Scanner;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class CentralControl extends Control {
	// Nome da queue
	private static final String QUEUE_NAME = "central";
	
	public static void main(String[] args) throws Exception {
		// Inicializa conexão
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("127.0.0.1");
		factory.setUsername("guest");
		factory.setPassword("guest");
		factory.setVirtualHost("/");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		
		// Declaração da queue e da exchange
		channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		
		Scanner scan = new Scanner(System.in);
		
		// chama método que realiza as operações de recepção
		receptor(channel);
		// chama método que realiza as operações de recepção
		emissor(channel, scan);
		
		channel.close();
		connection.close();
	}
	
	// Imprime o menu de opções para a central
	private static void imprimirMenu() {
		System.out.println("-----------------------------------");
		System.out.println("1. Enviar alerta.");
		System.out.println("2. Enviar Mensagem para um veiculo.");
		System.out.println("0. Sair.");
		System.out.println("-----------------------------------");
		System.out.println("Escolha uma opção: ");
		System.out.println("-----------------------------------");
	}
		
	// Metodo principal que envia mensagens do tipo fanout ou direct
	private static void emissor(Channel channel, Scanner scan) throws Exception {
		String mensagem;
				
		while(true) {
			imprimirMenu();
			String op = scan.nextLine();
					
			switch(op){
				// Enviar alerta global
				case "1":
					mensagem = " [!] Central enviou um alerta!";
					envioGlobal(channel, mensagem);
					break;
				// Enviar mensagem privada
				case "2":
					mandaZap(channel, QUEUE_NAME, scan);
					break;
				// Sair
				case "0":
					mensagem = " [x] Central saiu do sistema!";
					envioGlobal(channel, mensagem);
					break;
				default:
					System.out.println("[x] Opção inválida!");
					break;
			}
					
			if(op.equals("0")) break;
		}
	}
	
	// Método que permite que a central receba por fanout e direct
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
			channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
		}

}