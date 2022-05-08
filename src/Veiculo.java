import java.io.Serializable;

public class Veiculo implements Serializable{
	private String placa;
	private float velocidade;
	private int[] posicao;
	
	Veiculo(String placa){
		this.placa = placa;
	}
	
	public String getPlaca() {
		return placa;
	}
	public void setPlaca(String placa) {
		this.placa = placa;
	}
	public float getVelocidade() {
		return velocidade;
	}
	public void setVelocidade(float velocidade) {
		this.velocidade = velocidade;
	}
	public int[] getPosicao() {
		return posicao;
	}
	public void setPosicao(int[] posicao) {
		this.posicao = posicao;
	}
}
