import org.junit.Test;

import play.test.UnitTest;
import utils.ErrorInfo;
import business.BackstageSet;
import business.StationLetter;

public class BasicTest extends UnitTest {
	
	public static void main(String[] args) {
		BackstageSet backstageSet = (BackstageSet)null;
		
		System.out.println(backstageSet);
	}
	
	@Test
	public void aaa(){
		ErrorInfo error = new ErrorInfo();
		System.out.println("======"+StationLetter.queryWaitReplyMessageCount(error));
	}
	

	
}
