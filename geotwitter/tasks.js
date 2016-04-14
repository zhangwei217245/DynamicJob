/**
 * Created by zhangwei on 4/14/16.
 */

module.exports = {
    'UserCountExtractor' : {
        'extractFromTweet' : function (tweet, redis, key) {
            redis.sadd(key, tweet.user.id)
        },
        'fillArray' : function (array, redis, key, xsize) {
            redis.scard(key, function (err, data) {
                var keyarr = key.split(',');
                var x = parseInt(keyarr[keyarr.length - 2]);
                var y = parseInt(keyarr[keyarr.length - 1]) % 1000;
                var index = y * xsize + x
                array[index] = data;
            })
        }
    },

    'InfoExtractor' : {
        'extractFromTweet' : function (tweet, redis, key) {
            
        },
        'fillArray' : function (array, redis, key, xsize) {

        }
    }
}