import java.util.Scanner;

public class SU extends Thread {
	
	public SU() {
		suPrint("Superuser module invoked.");
		suPrint("Type su --<cmd> to invoke an administrator command.");
	}
	
	@Override
	public void run() {
		Scanner scanner;
	}
	
	private static void suPrint(String phrase) {
		System.out.println("SU: " + phrase);
	}

}
