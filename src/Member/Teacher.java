package Member;

import java.util.Scanner;

import Exception.LibraryException;
import Library.Library;
import Manager.CalCulator;
import Manager.Manager;

public class Teacher extends Member {

	private Cupon cupon = Cupon.TeacherCupon;

	public Teacher(String name, int id, Scanner sc, Library library) {
		super(name, id, sc, library);
	}

	@Override
	protected int getMaxBooks() {
		return 5;
	}

	@Override
	protected String getCallName() {
		return "先生";
	}

	@Override
	public void cal(Manager manager, boolean cuponUsed) throws LibraryException {

		//try catchは書かない

		if (getRank() == MemberRank.NORMAL) {
			System.out.println("rankが" + getRank() + "なので、割引されません");

			int price = CalCulator.NORMAL.getPrice();
			if (cuponUsed) {
				price = useCupon(price);
			} else {
				System.out.println("使用されています");
			}

			manager.setAssets(price);
		} else if (getRank() == MemberRank.GOLD) {
			System.out.println("rankが" + getRank() + "なので" + getRank().getPoint() + "円引きされます");

			int price = CalCulator.NORMAL.getPrice();
			if (cuponUsed) {
				price = useCupon(price);
			} else {
				System.out.println("使用されています");
			}

			manager.setAssets(price - getRank().getPoint());

		}
	}

	public int useCupon(int price) throws LibraryException {

		System.out.println("クーポンを使用しますか？yesを入力してください");

		String s = sc.next().trim().toLowerCase();
		if (!s.equals("yes")) {
			throw new LibraryException("やり直し");
		}

		super.consumeCupon();

		return price * (100 - cupon.getOff()) / 100;

	}

}
