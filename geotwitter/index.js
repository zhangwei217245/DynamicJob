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
    //Everytime when the data is imported into the database, we flush all the keys in the current db
    //in order to get rid of being effected by the data that is previously imported.
    redis.flushdb();
    //Walk every directory under the root directory.
    wk.on('directory', function (dirRoot, dirStat, dirNext) {
        var dirPath = path.join(dirRoot, dirStat.name);
        // On every sub-directory, we walk each data file.
        walk.walk(dirPath).on('file', function (fileRoot, fileStat, fileNext) {
            var fileName = path.join(fileRoot, fileStat.name);
            // make sure the data file is ended with .gz
            if (!S(fileName).endsWith('.gz')) {
                fileNext();
            }
            var count = 0;
            // we need to which file are we processing.
            console.log(fileName);
            var ins = fs.createReadStream(fileName);
            // the readline lib is embedded in Node JS itself, just like fs library.
            var rdline = readline.createInterface({
                // The input would be a stream. Here we use zlib to inflate the gzipped stream.
                // On any error, we should let the user know which file results in a failure.
                input : ins.pipe(zlib.createGunzip({windowBits: 15, memLevel: 7 })).on('error', function (e) {
                    console.log("zipErrorOccured at file", fileName, e)
                    //Whenever an error happens, we should continue for the next file.
                    fileNext();
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
                        // Never expect that each line would be in the correct format.
                        // Because sometimes the error may emit an JS event which leads to a failure of
                        // the data processing, so print them out, and simply jump to next file.
                        //console.log("line =|"+line+ "|", e);
                        fileNext();
                    }
                }
            }).on('close', function () {
                //After each file is processed, continue for the next one.
                fileNext();
            });
        }).on('end', function () {
            // if the program is done with iterating files in a sub-directory, jump to the next
            // sub-directory.
            dirNext()
        })
    }).on('end', function () {
            // If all the sub-directories is visited, then we can finish the job now!
            process.exit(0)
        })

};

startWalkingThroughFiles();

