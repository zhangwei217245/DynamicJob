/**
 * Created by zhangwei on 3/24/16.
 */

(function () {
    "use strict";

    var YMIN = 24.15275;

    var YMAX = 49.751726;

    var XMIN = -125.714216667;

    var XMAX = -65.983272667;
    // 1km 30 Arc-Second in decimal degrees
    var DEGREE_1KM = 0.008333;

    var SCALE = 1.0

    var HORIZONTAL_SPAN = XMAX - XMIN;

    var VERTICAL_SPAN = YMAX - YMIN;

    var CELL_XSIZE = DEGREE_1KM;

    var CELL_YSIZE = DEGREE_1KM;

    var NROW = Math.ceil(HORIZONTAL_SPAN / CELL_XSIZE) + 1

    var NCOL = Math.ceil(VERTICAL_SPAN / CELL_YSIZE) + 1

    var EPSG = 4326;


    function ScaleService(conf, scale) {
        console.log("ScaleService is called. Scale = " + scale )
        XMIN=conf.extent.xmin;
        XMAX=conf.extent.xmax;
        YMIN=conf.extent.ymin;
        YMAX=conf.extent.ymax;

        SCALE = scale;

        HORIZONTAL_SPAN = XMAX - XMIN;
        VERTICAL_SPAN = YMAX - YMIN;

        CELL_XSIZE = conf.resolution.x * SCALE;
        CELL_YSIZE = conf.resolution.y * SCALE;

        NROW = Math.ceil(HORIZONTAL_SPAN / CELL_XSIZE) + 1
        NCOL = Math.ceil(VERTICAL_SPAN / CELL_XSIZE) + 1

        EPSG = conf.EPSG;
        console.log("CELL_NUM=",NROW * NCOL)
        console.log("GRID_HORIZONTAL_SIZE=",NROW)
        console.log("GRID_VERTICAL_SIZE=",NCOL)
    };


    ScaleService.prototype.size= function () {
        return [NCOL, NROW];
    }

    ScaleService.prototype.boundings= function () {
        return [XMIN, XMAX, YMIN, YMAX];
    }

    ScaleService.prototype.getGeoTransform=function(){
        return Array.of(XMIN, CELL_XSIZE, 0, YMAX, 0, 0 - CELL_YSIZE);
    }

    ScaleService.prototype.getEPSG=function(){
        return EPSG;
    }

    ScaleService.prototype.getScale=function(){
        return SCALE;
    }

    ScaleService.prototype.gridIndex=function (coordinates) {
        var grid_horz = 0;
        var grid_vert = 0;

        var horz_found = false;
        var vert_found = false;

        if (coordinates == null) {
            return null;
        }

        if (coordinates[0] >= XMIN && coordinates[0] <= XMAX) {
            grid_horz = Math.floor((coordinates[0] - XMIN) / CELL_XSIZE)
            horz_found = true;
        }

        if (coordinates[1] >= YMIN && coordinates[1] <= YMAX) {
            grid_vert = Math.floor((YMAX - coordinates[1]) / CELL_YSIZE)
            vert_found = true;
        }

        var result = [horz_found?grid_horz:null, vert_found?grid_vert:null];
        return result;
    };

exports.scale=function(conf, scale){
    return new ScaleService(conf, scale)
};


}())
