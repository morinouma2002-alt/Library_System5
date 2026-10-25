package Manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Member.Member;


public class Manager {
	
	private long assets =0;
	private List<Member> memberList = new ArrayList<>();


	public List<Member> getMemberList() {
		return Collections.unmodifiableList(memberList);
	}

	public void setMemberList(Member member) {
		memberList.add(member);
	}
	
	public void setAssets(int price) {
		assets+=price;
	}
	
	
}
