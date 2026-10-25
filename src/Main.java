import java.util.Scanner;

import Library.Library;
import Manager.Manager;

public class Main {

	public static void main(String[]args) {
		Scanner sc =new Scanner(System.in);
		
		Library lib =new Library();
		Manager manager =new Manager();
		
		new SystemMain(sc,lib,manager).print();
	}
}
