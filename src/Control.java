import java.util.Scanner;

import com.rabbitmq.client.Channel;

public class Control {
	// Nome da exchange
	protected static final String EXCHANGE_NAME = "geral";
	
	// Método que envia algo para todos os envolvidos;
	protected static void envioGlobal(Channel channel, String mensagem) throws Exception{
		channel.basicPublish(EXCHANGE_NAME, "", null, mensagem.getBytes("UTF-8"));
	}
	
	// Método que envia algo para algum usuário específico
	protected static void envioPrivado(Channel channel, String mensagem, String emissor, String receptor, Scanner scan) throws Exception {
		mensagem = " [@] Veículo " + emissor + ": "+ mensagem;
		channel.basicPublish("", receptor, null, mensagem.getBytes("UTF-8"));
		System.out.println(" [*] Mensagem enviada!");
	}
		
	// Método que envia uma mensagem para algum veiculo especifico
	protected static void mandaZap(Channel channel, String emissor, Scanner scan) throws Exception {
		System.out.println("> Informe a placa ==> ");
		String placa = scan.nextLine();
		System.out.println("> Digite a mensagem ==> ");
		String mensagem = scan.nextLine();
		mensagem = " [@] " + emissor + ": "+ mensagem;
		channel.basicPublish("", placa, null, mensagem.getBytes("UTF-8"));
		System.out.println(" [*] Mensagem enviada!");
	}
}
