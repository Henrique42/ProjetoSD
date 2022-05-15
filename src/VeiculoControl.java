import java.util.Random;
import java.util.Scanner;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class VeiculoControl extends Control{
	
	public static void main(String[] args) throws Exception{
		// Inicializa conex�o
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("127.0.0.1");
		factory.setUsername("guest");
		factory.setPassword("guest");
		factory.setVirtualHost("/");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		
		Scanner scan = new Scanner(System.in);
		
		System.out.print("> Informe a placa do ve�culo: ");
		String placa = scan.nextLine();
		
		// Declara��o da queue e da exchange
		channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
		channel.queueDeclare(placa, false, false, false, null);
		
		// chama m�todo que realiza as opera��es de recep��o
		receptor(channel, placa);
		// chama m�todo que realiza as opera��es de recep��o
		emissor(channel, scan, placa);
		
		channel.close();
		connection.close();
		scan.close();
	}
	
	// Imprime o menu de op��es para o ve�culo
	private static void imprimirMenu() {
		System.out.println("--------------------------------------");
		System.out.println("1. Enviar alerta.");
		System.out.println("2. Enviar mensagem para a Central.");
		System.out.println("3. Enviar mensagem para outro ve�culo.");
		System.out.println("4. Informar posi��o e velocidade.");
		System.out.println("5. Informar parada programada.");
		System.out.println("0. Sair.");
		System.out.println("--------------------------------------");
		System.out.println("Escolha uma op��o: ");
		System.out.println("--------------------------------------");
	}
	
	private static void emissor(Channel channel, Scanner scan, String placa) throws Exception {
		String mensagem;
		// Para pegar a velocidade e a posi��o de forma aleat�ria 
		Random rand;
		int vel, pos[];
		
		while(true) {
			imprimirMenu();
			String op = scan.nextLine();
			
			switch(op){
				// enviar alerta global
				case "1":
					mensagem = " [!] Ve�culo " + placa +" enviou um alerta!";
					envioGlobal(channel, mensagem);
					break;
				// Enviar mensagem privada para a central
				case "2":
					System.out.println("> Digite a mensagem ==> ");
					mensagem = scan.nextLine();
					envioPrivado(channel, mensagem, placa, "central", scan);
					break;
				// Enviar mensagem privada para outro ve�culo
				case "3":
					mandaZap(channel, "Ve�culo " + placa, scan);
					break;
				// Envia velocidade e posi��o para a central
				case "4":
					pos = new int[2];
					rand = new Random();
					
					vel = rand.nextInt(300);
					pos[0] = rand.nextInt(100); pos[1] = rand.nextInt(100);
					mensagem = "[INFO] Velocidade atual = " + vel + " km/h. Coordenadas = (" + pos[0] + ", " + pos[1] + ").";
					envioPrivado(channel, mensagem, placa, "central", scan);
					break;
				// Informa parada programada para a central
				case "5":
					pos = new int[2];
					rand = new Random();
					
					pos[0] = rand.nextInt(100); pos[1] = rand.nextInt(100);
					mensagem = "[INFO] Parada programada nas coordenadas (" + pos[0] + ", " + pos[1] + ").";
					envioPrivado(channel, mensagem, placa, "central", scan);
					break;
				// sair
				case "0":
					mensagem = " [x] Ve�culo " + placa +" saiu do sistema!";
					envioGlobal(channel, mensagem);
					break;
				default:
					System.out.println("[x] Op��o inv�lida!");
					break;
			}
			
			if(op.equals("0")) break;
		}
	}
	
	// M�todo que permite que a central receba por fanout e direct
	private static void receptor(Channel channel, String placa) throws Exception {
		String nomeFila = channel.queueDeclare().getQueue();
		channel.queueBind(nomeFila, EXCHANGE_NAME, "");
		
		String mensagem;
		
		mensagem = " [!] Ve�culo " + placa +" foi cadastrado no sistema!";
		channel.basicPublish(EXCHANGE_NAME, "", null, mensagem.getBytes("UTF-8"));
		
		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
			String message = new String(delivery.getBody(), "UTF-8");
			System.out.println(message);
		};
		
		// fanout
		channel.basicConsume(nomeFila, true, deliverCallback, consumerTag -> {});
		// direct
		channel.basicConsume(placa, true, deliverCallback, consumerTag -> {});
	}

}
