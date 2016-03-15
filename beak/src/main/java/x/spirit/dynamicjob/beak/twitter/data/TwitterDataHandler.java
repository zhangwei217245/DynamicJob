/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package x.spirit.dynamicjob.beak.twitter.data;

import twitter4j.Status;
import x.spirit.dynamicjob.beak.twitter.util.Tuple;

/**
 *
 * @author zhangwei
 */
public class TwitterDataHandler {

    TweetsCountByUsers tweetsCountByUsers;

    public void handleTwitterData (Tuple<Status, Long> data){
        tweetsCountByUsers.incrementGridCount(data.getFirst());
    }

    public void setTweetsCountByUsers(TweetsCountByUsers tweetsCountByUsers) {
        this.tweetsCountByUsers = tweetsCountByUsers;
    }
}
