/**
 * Created by zhangwei on 3/24/16.
 *
 * Caution: More memory may be needed : --max_old_space_size=2048(2GB)
 * --max_new_space_size and/or --max_old_space_size
 * Example :
 * node --max_old_space_size=4096 GTiffGen.js -o pic.tif -c default -t UserCountExtractor
 */

const commandLineArgs = require('command-line-args');

var cli = commandLineArgs([
    {name: 'help', alias: 'h', type: Boolean},
    {name: 'output', alias: 'o', type: String, multiple: false, defaultValue: "./pic.tif"},
    {name: 'config', alias: 'c', type: String, multiple: false, defaultValue: "default"},
    {name: 'task', alias: 't', type: String, multiple: false, defaultValue: "UserCountExtractor"},
    {name: 'scale', alias: 's', type: Number, multiple: false, defaultValue: 1.0},
])

var options = cli.parse();

if (options.help) {
    cli.getUsage();
    process.exit(0);
}

const gdal = require('gdal');
const async = require('async');
const config = require('node-yaml-config');
var conf = config.load('./config/geotwitter.yaml', options.config);

const redis = require('redis').createClient(conf.redis);
const scale = require('./scale/scale').scale(conf, options.scale);


var format = "GTiff"
var GDALDriver = null;
gdal.drivers.forEach(function (driver, i) {
    if (driver.description == format) {
        GDALDriver = driver;
    }
})

var size = scale.size();
var dataSet = GDALDriver.create(options.output, size[0], size[1], 1, gdal.GDT_Int32)

//console.log(dataSet);
dataSet.geoTransform = scale.getGeoTransform();
dataSet.srs = gdal.SpatialReference.fromEPSGA(scale.getEPSG());

var maxValue = 0;

function writeUserCount(key, item) {
    redis.scard(key, function (err1, data) {
        var keyarr = key.split(',');
        var x = parseInt(keyarr[keyarr.length - 2]);
        var y = parseInt(keyarr[keyarr.length - 1]);
        // The more the people were posting tweets, the darker the color should be.
        // Then the picture should be easily to observe.
        var gr_val = parseInt(data);
        if (gr_val > maxValue) {
            maxValue = gr_val;
        }
        item.pixels.write(x, y, 1, 1, Int32Array.of(gr_val))
        // Flush every pixel's change onto disk.
        item.flush();
    })
}


// dataSet.bands.create(gdal.GDT_Byte)
dataSet.bands.forEach(function (item, i) {
    item.noDataValue = 0// The entire picture should feature a white background.
    console.log(item);

    var key_pattern_prefix = options.task + ',' + scale.getScale().toFixed(4) + ',*';
    var append = '';
    for (r = 0; r < size[1]; r += 1000) {
        row_num = size[1] - r < 1000 ? size[1] - r: 1000;
        data_arr = new Int32Array(row_num);
        for (i = 0; i < data_arr.length; i++) {
            data_arr[i] = new Int32Array(size[0])
            for (j = 0; j < data_arr[i].length; j++) {
                data_arr[i][j] = 0
            }
        }
        append = ',' + parseInt(r / 1000) + '???';
        console.log('r < ', 1000);
        async.waterfall([
                function (callback) {
                    if (r >= 1000) {
                        return;
                    }
                    redis.KEYS(key_pattern_prefix + ',?', function (err, keylist) {
                        keylist.forEach(function (entry) {
                            //keys.push(entry)
                        })
                        console.log(keylist.length, keys.length)
                        callback(err)
                    })
                },
                function (callback) {
                    if (r >= 1000) {
                        return;
                    }
                    redis.KEYS(key_pattern_prefix + ',??', function (err, keylist) {
                        keylist.forEach(function (entry) {
                            // keys.push(entry)
                        })
                        console.log(keylist.length, keys.length)
                        callback(err)
                    })
                },
                function (callback) {
                    if (r >= 1000) {
                        return;
                    }
                    redis.KEYS(key_pattern_prefix + ',???', function (err, keylist) {
                        keylist.forEach(function (entry) {
                            // keys.push(entry)
                        })
                        console.log(keylist.length, keys.length)
                        callback(err)
                    })
                },
                function (callback) {
                    if (r < 1000) {
                        return;
                    }
                    redis.KEYS(key_pattern_prefix + append, function (err, keylist) {
                        keylist.forEach(function (entry) {
                            // keys.push(entry)
                        })
                        console.log(keylist.length, keys.length)
                        callback(err)
                    })
                }
            ],
            function (err, result) {
                console.log(err, result)
            })
    }

    // keys.forEach(function (key, i) {
    //     if (options.task == 'UserCountExtractor') {
    //         writeUserCount(key, item)
    //     }
    // })

    // redis.KEYS(key_pattern_prefix + append, function (err, keylist) {
    //     keylist.forEach(function (key, i) {
    //         if (options.task == 'UserCountExtractor') {
    //             writeUserCount(key, item)
    //         }
    //     })
    //     dataSet.flush();
    // })
})

// When the program exits, it should flush the data onto the disk.
process.on('exit', function (code) {
    dataSet.bands.forEach(function (item, i) {
        item.flush();
    })
    dataSet.flush();
    dataSet.close();

    console.log("maxValue= ", maxValue)
    console.log("exit on code ", code)
})

//The program should exit in 10 min.
setTimeout(function () {
    process.exit(0);
}, 10 * 60 * 1000)
