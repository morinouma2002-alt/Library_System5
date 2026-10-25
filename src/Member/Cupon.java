package Member;

public enum Cupon {

	//StudentCupon(true,20),
	StudentCupon(20),
	TeacherCupon(10);

	private int off;
	
	Cupon(int off){
		
		this.off=off;
	}
	
	public int getOff() {
		return off;
	}
}
