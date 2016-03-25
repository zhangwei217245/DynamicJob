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
    redis.KEYS('*', function(err, keylist){
        keylist.forEach(function (key, i) {
            redis.scard(key, function (err1, data) {
                var x = parseInt(key.split(',')[0]);
                var y = parseInt(key.split(',')[1]);
                var gr_val = parseInt(data);
                //console.log(x, y, gr_val)
                item.pixels.set(x, y, gr_val);
            })
        })
    })

})

process.on('exit', function(code){
    dataSet.clone();
})
//dataSet.close();


//process.exit(0);
