package Manager;


//料金や割引額の定数置き場として利用する
public enum CalCulator {

	
	
	NORMAL(100);
	
	private int price;
	
	CalCulator(int price){
		this.price=price;
	}
	
	public int getPrice() {
		return price;
	}
}
