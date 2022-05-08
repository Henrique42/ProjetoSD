import java.util.Scanner;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class VeiculoControl {
	
	private static final String EXCHANGE_NAME = "geral";
	
	public static void main(String[] args) throws Exception{
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("127.0.0.1");
		factory.setUsername("guest");
		factory.setPassword("guest");
		factory.setVirtualHost("/");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		
		channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
		Scanner scan = new Scanner(System.in);
		
		System.out.print("Informe a placa do veículo: ");
		String id = scan.nextLine();
		
		Veiculo v = new Veiculo(id);

		receptor(channel, id);
		emissor(channel, scan, v);
		
		channel.close();
		connection.close();
		scan.close();
	}
	
	// Imprime o menu de opções para o veículo
	private static void imprimirMenu() {
		System.out.println("-------------------");
		System.out.println("1. Enviar alerta.");
		System.out.println("0. Sair.");
		System.out.println("-------------------");
		System.out.println("Escolha uma opção: ");
		System.out.println("-------------------");
	}
	
	// Método que envia mensagens para todos os consumidores;
	private static void envioGlobal(Channel channel, String mensagem) throws Exception{
		channel.basicPublish(EXCHANGE_NAME, "", null, mensagem.getBytes("UTF-8"));
	}
	
	private static void emissor(Channel channel, Scanner scan, Veiculo v) throws Exception {
		String mensagem;
		
		while(true) {
			imprimirMenu();
			String op = scan.nextLine();
			
			switch(op){
				case "1":
					mensagem = " [!] Veículo " + v.getPlaca() +" enviou um alerta!";
					envioGlobal(channel, mensagem);
					break;
				case "0":
					mensagem = " [x] Veículo " + v.getPlaca() +" saiu do sistema!";
					envioGlobal(channel, mensagem);
					break;
				default:
					System.out.println("Número inválido!");
					break;
			}
			
			if(op.equals("0")) break;
		}
	}
	
	private static void receptor(Channel channel, String id) throws Exception {
		String nomeFila = channel.queueDeclare().getQueue();
		channel.queueBind(nomeFila, EXCHANGE_NAME, "");
		
		String mensagem;
		
		mensagem = " [!] Veículo " + id +" foi cadastrado no sistema!";
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
