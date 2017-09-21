package gov.usdot.cv.common.model;

public enum VsmType {
	Fund(1), VehStat(2), Weather(4), Env(8), ElVeh(16);
		
	private static final int All = 
		Fund.getValue() + 
		VehStat.getValue() + 
		Weather.getValue() + 
		Env.getValue() + 
		ElVeh.getValue();
		
	private int value;
		
	private VsmType(int value) { 
		this.value = value; 
	}
		
	public int getValue() { return this.value; }
		
	public static boolean isValid(int type) {
		return (type >= Fund.getValue() && type <= All);
	}
}