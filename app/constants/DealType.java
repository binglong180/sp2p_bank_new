package constants;
/**
 * 交易类型规范
 * @author PC
 *
 */
public class DealType {
	//满标放款-银行服务费入账
    public static final int INVESTFEE = 1;
    public static final int INVEST = 2;
    public static final int SETTLE =3;
    public static final int SETTLEFEE = 4;
    public enum DealStatus{
    	INIT(1),FAIL(2),SECCUSS(3);
    	private int value;
    	private DealStatus(int value){
    		this.setValue(value);
    	}
		public int getValue() {
			return value;
		}
		public void setValue(int value) {
			this.value = value;
		}
    }
}
