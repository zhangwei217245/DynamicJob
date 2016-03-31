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

console.log(dataSet);

// dataSet.bands.create(gdal.GDT_Byte)
dataSet.bands.forEach(function (item, i) {
  item.noDataValue=0// The entire picture should feature a white background.
  console.log(item)
     redis.KEYS('*', function(err, keylist){
         keylist.forEach(function (key, i) {
             redis.scard(key, function (err1, data) {
                 var x = parseInt(key.split(',')[0]);
                 var y = parseInt(key.split(',')[1]);
                 // The more the people were posting tweets, the darker the color should be.
                 // Then the picture should be easily to observe.
                 var gr_val = parseInt(data);
                 item.pixels.write(x, y, 1, 1, Int8Array.of(gr_val))
                 // Flush every pixel's change onto disk.
                 item.flush();
             })
         })
         dataSet.flush();
     })
})

// When the program exits, it should flush the data onto the disk.
process.on('exit', function(code){
    dataSet.bands.forEach(function (item, i) {
	item.flush();	
    })
    dataSet.flush();
    dataSet.close();
    console.log("exit on code ", code)
})

//The program should exit in 10 min.
setTimeout(function(){
	process.exit(0);
}, 10 * 60 * 1000)
