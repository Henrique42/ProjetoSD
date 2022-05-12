import java.util.Scanner;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class CentralControl {
	
	private static final String EXCHANGE_NAME = "geral";
	private static final String QUEUE_NAME = "central";
	
	public static void main(String[] args) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("127.0.0.1");
		factory.setUsername("guest");
		factory.setPassword("guest");
		factory.setVirtualHost("/");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		
		channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		
		Scanner scan = new Scanner(System.in);
		
		receptor(channel);
		emissor(channel, scan);
		
		channel.close();
		connection.close();
	}
	
	private static void imprimirMenu() {
		System.out.println("-------------------");
		System.out.println("1. Enviar alerta.");
		System.out.println("2. Enviar Mensagem para um veiculo.");
		System.out.println("0. Sair.");
		System.out.println("-------------------");
		System.out.println("Escolha uma opção: ");
		System.out.println("-------------------");
	}
	
	// Metodo que recebe o fanout e direct
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
	
	// Método que envia mensagens para todos os consumidores
	private static void enviarAlerta(Channel channel, String mensagem) throws Exception{
		channel.basicPublish(EXCHANGE_NAME, "", null, mensagem.getBytes("UTF-8"));
	}
	
	// Método que envia mensagem para um veiculo especifico
	private static void mandaZap(Channel channel, Scanner scan) throws Exception {
		System.out.println("Informe a placa ==> ");
		String placa = scan.nextLine();
		System.out.println("Digite a mensagem ==> ");
		String mensagem = " [@] Central: "+ scan.nextLine();
		channel.basicPublish(EXCHANGE_NAME, placa, null, mensagem.getBytes("UTF-8"));
	}
		
	// Metodo principal que envia mensagens do tipo fanout ou direct
	private static void emissor(Channel channel, Scanner scan) throws Exception {
		String mensagem;
				
		while(true) {
			imprimirMenu();
			String op = scan.nextLine();
			
			// add algo pra limpar buffer
					
			switch(op){
				case "1":
					mensagem = " [x] Alerta da Central!";
					enviarAlerta(channel, mensagem);
					break;
				case "2":
					mandaZap(channel, scan);
					break;
				case "0":
					mensagem = " [x] Central saiu do sistema!";
					enviarAlerta(channel, mensagem);
					break;
				default:
					System.out.println("Número inválido!");
					break;
			}
					
			if(op.equals("0")) break;
		}
	}

}