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

var conf = config.load('./config/geotwitter.yaml', 'development');

var wk = walk.walk(conf.filedir, {followlinks : false})


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
                            console.log(tweet.user.id, tweet.coordinates.coordinates);
                            
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
    })

});






