import java.util.List;
import java.util.Scanner;

import Exception.LibraryException;
import Library.Library;
import Manager.Manager;
import Member.Member;
import Member.Student;
import Member.Teacher;

public class SystemMain {

	private Scanner sc;
	private Library library;
	private Manager manager;

	public SystemMain(Scanner sc, Library library, Manager manager) {
		this.sc = sc;
		this.library = library;
		this.manager = manager;

	}

	public void print() {

		System.out.println("システムを終了する場合は -1");

		boolean found = true;
		Member member;

		//次は、どうやって　一日を動かすかをかんがえる
		while (found) {

			try {

				System.out.println("今日は" + library.getToday() + "です");

				while (true) {
					System.out.println("あなたは、生徒か先生ですか\n"
							+ "1,先生\n"
							+ "2,生徒");
					System.out.println("同じ、人物なら再ログイン");

					if (!sc.hasNextInt()) {
						System.out.println("数値を入力");
						sc.next();
						continue;
					}

					int index = sc.nextInt();
					member = getMember(index);

					if (member == null) {
						System.out.println("もう一度やり直してください");
						continue;
					}
					break;
				}

				loginLoop: while (true) {
					String textBlock = """
							----入力----
							1,レンタル
							2,返却
							3,検索
							4,退館
							5,現在の状態を確認する
							6,一日が終了する
							-1,システム終了
							""";
					System.out.println(textBlock);
					int index = sc.nextInt();
					switch (index) {

					case 1 -> {
						try {
							member.borrow();
						} catch (LibraryException e) {
							System.out.println(e.getMessage());
						}
					}

					/*use-aの関係、MemberがManagerをフィールドに持たず、returnBook(Manager manager)の
					引数として一時期的に使うだけだから。フィールドに持つ場合は　has-aになる*/
					case 2 -> {
						try {
							member.returnBook(manager);
						} catch (LibraryException e) {
							System.out.println(e.getMessage());
						}
					}
					case 3 -> member.search();

					case 4 -> {
						System.out.println("利用ありがとうございました");
						break loginLoop;
					}

					case 5 -> {
						System.out.println(member.getId() + ":" + member.getName() + "の----確認----");
						//enumの連結はできない
						System.out.println("ランク:" + member.getRank() + "/状態" + member.getStatus());
						member.display();

					}

					case 6 -> library.nextDay();

					case -1 -> {
						System.out.println("終了します");
						return;
					}
					}

				}

			} catch (LibraryException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	public Member getMember(int index) throws LibraryException {

		Member member;
		if (index == 1) {
			System.out.println("---先生---");

			System.out.println("名前とidを設定してください");

			if (!sc.hasNext()) {
				throw new LibraryException("名前を入力してください");
			}
			String name = sc.next();

			List<Member> memberList = manager.getMemberList();
			for (Member mem : memberList) {
				if (name.equals(mem.getName())) {
					System.out.println("おかえりなさい:" + mem.getName());
					return mem;
				}
			}

			int id = sc.nextInt();

			member = new Teacher(name, id, sc, library);
			manager.setMemberList(member);//managerが先生を格納する

			//	member.borrow();

			return member;

		} else if (index == 2) {
			System.out.println("---生徒---");

			System.out.println("名前とidを設定してください");

			String name = sc.next();

			List<Member> memberList = manager.getMemberList();
			for (Member mem : memberList) {

				if (name.equals(mem.getName())) {
					System.out.println("おかえりなさい:" + mem.getName());
					return mem;
				}
			}

			int id = sc.nextInt();
			member = new Student(name, id, sc, library);
			manager.setMemberList(member);//managerが生徒を格納する

			return member;

		} else {
			System.out.println("もう一度、やり直してください");
			return null;
		}

	}
}
