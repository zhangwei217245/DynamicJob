/**
 * Created by zhangwei on 3/23/16.
 */

const fs=require('fs');
const readline = require('readline');
const zlib = require('zlib');
const walk=require('walk');
const config = require('node-yaml-config');
const path = require('path');
const S=require('string');
const sleep = require('sleep');
const sync = require('sync');

const scale = require('./scale/scale').scale(500);


var conf = config.load('./config/geotwitter.yaml', 'default');

const redis = require('redis').createClient(conf.database);

var wk = walk.walk(conf.filedir, {followlinks : false});

function startWalkingThroughFiles() {
    redis.flushdb();
    wk.on('directory', function (dirRoot, dirStat, dirNext) {
        var dirPath = path.join(dirRoot, dirStat.name);
        walk.walk(dirPath).on('file', function (fileRoot, fileStat, fileNext) {
            var fileName = path.join(fileRoot, fileStat.name);
            if (S(fileName).endsWith('.gz')) {
                var count = 0;
                console.log(fileName);
                var ins = fs.createReadStream(fileName);

                var rdline = readline.createInterface({
                    input : ins.pipe(zlib.createGunzip({windowBits: 15, memLevel: 7 })).on('error', function (e) {
                        console.log("zipErrorOccured at file", fileName, e)
                    })
                });

                rdline.on('line', function (line) {
                    if (line.length > 0) {
                        try{
                            var tweet = JSON.parse(S(line).split("|")[1]);
                            if (tweet.coordinates != null) {
                                //redis.setnx(tweet.user.id + scale.gridIndex(tweet.coordinates.coordinates).stringify);
                                var vl = ""+scale.gridIndex(tweet.coordinates.coordinates)
                                redis.sadd(vl, tweet.user.id)

                            }
                            count++
                        } catch(e) {
                            //console.log(e)
                        }
                    }
                }).on('close', function () {
                    fileNext();
                });
            } else {
                fileNext()
            }
        }).on('end', function () {
            dirNext()
        }).on('end', function () {
            // scale.walkGrids(function(i, j){
            //     var vl = i+","+j
            //     // redis.scard(vl, function (err, replies) {
            //     //     console.log(vl, " -> ", replies);
            //     // })
            //
            // })
            process.exit(0)
        })

    });
};

startWalkingThroughFiles();






// var redisRead = require('redis').createClient(conf.database);
// redisRead.scard("1000,1000", function (err, data) {
//     console.log(data)
// })
// redisRead.quit()

