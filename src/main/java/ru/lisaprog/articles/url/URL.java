package ru.lisaprog.articles.url;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import ru.lisaprog.sql.SQLQuery;

/**
 * Created with IntelliJ IDEA.
 * User: Юлиан
 * Date: 27.11.13
 * Time: 14:56
 * To change this template use File | Settings | File Templates.
 */
public class URL {
	final static WebClient webClient = new WebClient();

	public static void parseURL(){
		//Проблеменый 7856
		webClient.getBrowserVersion().setBrowserVersion(BrowserVersion.FIREFOX_17.getBrowserVersionNumeric());
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setAppletEnabled(false);
		webClient.getOptions().setGeolocationEnabled(false);
		webClient.getOptions().setPopupBlockerEnabled(false);
		webClient.getOptions().setDoNotTrackEnabled(false);
		webClient.getOptions().setRedirectEnabled(false);
		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.http").setLevel(java.util.logging.Level.OFF);
		HtmlPage page;
		String url;
		for(int i = 18718; i < SQLQuery.getURLCount(); i++){
			try{
				url = SQLQuery.getURL(i);
				if(url.startsWith("http://")){
					page = webClient.getPage(url);
					SQLQuery.setURLText(url, page.asText().toString());
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
