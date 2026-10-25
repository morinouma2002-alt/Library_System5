package Member;

public enum MemberRank {

	NORMAL(0),
	GOLD(50);
	
	private int payPoint;
	
	MemberRank(int payPoint){
		this.payPoint=payPoint;
	}
	
	public int getPoint() {
		return payPoint;
	}
}
