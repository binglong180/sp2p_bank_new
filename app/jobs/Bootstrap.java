package jobs;
import java.io.File;
import java.util.Properties;

import org.apache.log4j.xml.DOMConfigurator;

import payment.api.system.PaymentEnvironment;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import business.BackstageSet;
import constants.Constants;
//import payment.newr.PaymentProxy; //2016 1 12 mzq and 第三方支付接口

@OnApplicationStart
public class Bootstrap extends Job {
	public static String VERSION = null;
	 final Properties p = Play.configuration;
    public void doJob() {
    	
	    new BackstageSet();
	     
	    BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
	     
	    Play.configuration.setProperty("mail.smtp.host",backstageSet.emailWebsite);
	    Play.configuration.setProperty("mail.smtp.user",backstageSet.mailAccount);
	    Play.configuration.setProperty("mail.smtp.pass",backstageSet.mailPassword);
	    
	    if(Constants.IPS_ENABLE){
	    	this.initPayment();
	    }
    }
    
    /**
     * 初始化支付网关
     */
    private void initPayment(){
    	 head();
        String systemConfigPath =  p.getProperty("system.config.path");
        String paymentConfigPath =  p.getProperty("payment.config.path");
        // Log4j
        String log4jConfigFile = systemConfigPath + File.separatorChar + "log4j.xml";
        System.out.println(log4jConfigFile);
        DOMConfigurator.configure(log4jConfigFile);

        try {
			// 初始化支付环境
			PaymentEnvironment.initialize(paymentConfigPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
    
    }
    private void head() {
        System.out.println("==========================================");
        System.out.println("China Payment & Clearing Network Co., Ltd.");
        System.out.println("Payment and Settlement System");
		System.out.println("Institution Simulator " + VERSION);
        System.out.println("==========================================");
    }
}