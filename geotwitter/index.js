/**
 * Created by zhangwei on 3/23/16.
 *
 * Initiating the master instance.
 * node --max_old_space_size=4096 index.js -c default -s 0.5 -t UserCountExtractor -d /home/wesley/Data
 *
 * Initiating the slave instances.
 * node --max_old_space_size=4096 index.js -p false -c default -s 0.5 -t UserCountExtractor -d /home/wesley/Data
 *
 */
const commandLineArgs = require('command-line-args');

var cli = commandLineArgs([
    { name: 'help', alias: 'h', type: Boolean },
    { name: 'config', alias: 'c', type: String, multiple: false, defaultValue: "default" },
    { name: 'purge', alias: 'p', type: Boolean, multiple: false, defaultValue: true },
    { name: 'task', alias: 't', type: String, multiple: false, defaultValue: 'UserCountExtractor' },
    { name: 'scale', alias: 's', type: Number, multiple: false, defaultValue: 1.0 },
    { name: 'dir', alias: 'd', type: Boolean, multiple: false, defaultValue: '/home/wesley/Data' },
])

var options = cli.parse();

if (options.help) {
    cli.getUsage();
    process.exit(0);
}

const fs=require('fs');
const readline = require('readline');
const zlib = require('zlib');
const walk=require('walk');
const config = require('node-yaml-config');
const path = require('path');
const S=require('string');
const sync = require('sync');

var conf = config.load('./config/geotwitter.yaml', options.config);

const scale = require('./scale/scale').scale(conf, options.scale);

const redis = require('redis').createClient(conf.redis);

var wk = walk.walk(options.dir, {followlinks : false});

function startWalkingThroughFiles() {
    //Everytime when the data is imported into the database, we flush all the keys in the current db
    //in order to get rid of being effected by the data that is previously imported.
    if (options.purge) {
        redis.flushdb();
    }
    redis.set(options.task+'.scale', options.scale.toString())

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
                input : ins.pipe(zlib.createGunzip({windowBits: 15, memLevel: 8 })).on('error', function (e) {
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
                            var grid_idx = scale.gridIndex(tweet.coordinates.coordinates);
                            if (grid_idx[0]!=null && grid_idx[1]!=null) {
                                var vl = options.task+','+scale.getScale().toFixed(4)+','+grid_idx
                                if (options.task == 'UserCountExtractor') {
                                    redis.sadd(vl, tweet.user.id)
                                }
                            }
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

