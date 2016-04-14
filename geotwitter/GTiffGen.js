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
const tasks = require('./tasks');

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

// When the program exits, it should flush the data onto the disk.
process.on('exit', function (code) {
    dataSet.bands.forEach(function (item, i) {
        item.flush();
    })
    dataSet.flush();
    dataSet.close();

    console.log("exit on code ", code)
})

// dataSet.bands.create(gdal.GDT_Byte)
dataSet.bands.forEach(function (item, i) {
    item.noDataValue = 0// The entire picture should feature a white background.
    console.log(item);

    var key_pattern_prefix = options.task + ',' + scale.getScale().toFixed(4) + ',*';
    var append = '';

    var offset_arr=[];
    for (var r = 0; r < size[1]; r += 1000) {
        offset_arr.push(r)
    }
    console.log(offset_arr)
    async.eachSeries(offset_arr, function (offset, general_callback) {
        console.log(offset)
        var patterns = [];
        if (offset < 1000) {
            patterns.push(key_pattern_prefix + ",?", key_pattern_prefix + ",??", key_pattern_prefix + ",???");
        } else {
            append = ',' + parseInt(offset / 1000) + '???';
            patterns.push(key_pattern_prefix + append)
        }

        var row_num = size[1] - offset < 1000 ? size[1] - offset : 1000;
        var array = new Int32Array(row_num * size[0]);

        async.each(patterns,
            function (pattern, redis_callback) {
                redis.KEYS(pattern, function (err, keylist) {
                    if (err) return redis_callback(err);
                    async.each(keylist, function (key, key_callback) {
                        tasks[options.task].fillArray(array, redis, key, size[0], key_callback)
                    }, function (err) {
                        redis_callback(null);
                    })
                })
            },
            function (err) {
                item.pixels.write(0, offset, size[0], row_num, array);
                item.flush();
                general_callback(null);
            })
    
    }, function (err) {
        console.log('about to exit');
        process.exit(0);
    })
})

