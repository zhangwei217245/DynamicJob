/**
 * Created by zhangwei on 3/24/16.
 */
const config = require('node-yaml-config');
var conf = config.load('./config/geotwitter.yaml', 'production');
const scale = require('./scale/scale').scale(500);
const redis = require('redis').createClient(conf.database);
const S=require('string');

var size = scale.size();

for (var x = 0; x < size[0]; x++) {
    for (var y = 0; y < size[1]; y++){
        (function (i,j) {
            var key = i+','+j;
            redis.scard(key, function (err, data) {
                if (j==0) {
                    console.log("\n")
                }
                console.log(data+",");
                console.log(key, data);
            })
        })(x,y)
    }
}
