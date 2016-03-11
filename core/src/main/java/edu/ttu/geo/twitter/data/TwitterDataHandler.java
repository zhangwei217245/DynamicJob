/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ttu.geo.twitter.data;

import edu.ttu.geo.twitter.util.Tuple;
import twitter4j.Status;

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
