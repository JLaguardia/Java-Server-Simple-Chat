
import javax.swing.JFrame;

public class TestChat {

	public static void main(String[] args) {
		Server yo = new Server();
		yo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		yo.startRunning();
	}

}
