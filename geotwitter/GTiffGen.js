/**
 * Created by zhangwei on 3/24/16.
 *
 * Caution: More memory may be needed : --max_old_space_size=2048(2GB)
 *
 * --max_new_space_size and/or --max_old_space_size
 */
const gdal = require('gdal');
const config = require('node-yaml-config');
var conf = config.load('./config/geotwitter.yaml', 'default');
const scale = require('./scale/scale').scale(500);
const redis = require('redis').createClient(conf.database);
const S=require('string');

var format = "GTiff"
var GDALDriver = null;
gdal.drivers.forEach(function (driver, i) {
    if (driver.description == format) {
        GDALDriver = driver;
    }
})

var size = scale.size();
var dataSet = GDALDriver.create("./pic.tif", size[0], size[1], 1, gdal.GDT_Byte)


// dataSet.bands.create(gdal.GDT_Byte)
dataSet.bands.forEach(function (item, i) {
    for (var x = 0; x < item.size.x; x++) {
        for (var y = 0; y < item.size.y; y++) {
            (function (i,j) {
                var key = i+','+j;
                redis.scard(key, function (err, data) {
                    item.pixels.set(x, y, S(data).toInt())
                })
            })(x,y)

            //item.pixels.set(x, y, Math.floor((Math.random() * 1000) + 1))
            // redis.scard(x+","+y, function (err, replies) {
            //     if (S(replies).isNumeric()) {
            //         item.pixels.set(x, y, S(replies).toInt())
            //     }
            // })
        }
    }

})

dataSet.close();

process.exit(0);

//driver = gdal.GetDriverByName( format )