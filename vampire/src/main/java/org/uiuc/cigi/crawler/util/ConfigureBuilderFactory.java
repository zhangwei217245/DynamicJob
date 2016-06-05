package org.uiuc.cigi.crawler.util;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.client.methods.HttpPost;

import twitter4j.conf.ConfigurationBuilder;
/**
 * twitter stream built preparison
 * @author dawning zhzhang@cnic.cn
 *
 */
public class ConfigureBuilderFactory {
	private static ConfigurationBuilder cb;
	
	public static ConfigurationBuilder getInstance(){
		if(cb==null){
			 cb = new ConfigurationBuilder();
		     cb.setDebugEnabled(true)
		     .setOAuthConsumerKey("JeTxaSZtuw2i91BPAEMSg")
		     .setOAuthConsumerSecret("QcoYjMWVvdFbDu6b7Kv94fSwS5DkUxb0o7HF2ODyo")
		     .setOAuthAccessToken("396313231-az01b3HIOW7b4oaX5oBApLNjAWs6voqO8L51g06l")
		     .setOAuthAccessTokenSecret("B8VFEBlqOwc3QDcCq17wCpEVGUbGQIPId1urLMxbc0");
		     cb.setStreamBaseURL(SystemConstant.STREAMING_BASE_URL);
		     cb.setUseSSL(true);
		}
		return cb;
	}
	
	private static final String consumerKey = "Q91SEZUGDCkA4tFVVmgHfA";
	private static final String consumerSecret = "feMTvb7rn1kMQcFhxMhZ4BcycRU3OjlTdPO7bi26b4";
    private static final String token = "395572904-yLh7yxZU3lpHOQ9ckfiCPfx9Xdpzt1hScdSLbWr6";
    private static final String tokenSecret = "EquGJYUbDE5T6AKzWyrL2LFS39iuDFYvk8PcStVOH0";
    
    public static void getConsumer(HttpPost method){
    	CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(
    	        consumerKey, consumerSecret);
    	consumer.setTokenWithSecret(token, tokenSecret);
    	try {
			consumer.sign(method);
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		}
	}
}
