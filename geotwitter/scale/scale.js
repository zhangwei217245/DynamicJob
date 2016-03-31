/**
 * Created by zhangwei on 3/24/16.
 */

(function () {
    "use strict";

    const US_LOWER = 24.15275;

    const US_UPPER = 49.751726;

    const US_LEFT = -125.714216667;

    const US_RIGHT = -65.983272667;
    // 1km 30 Arc-Second in decimal degrees
    const DEGREE_1KM = 0.008333;

    const US_HORIZONTAL_SPAN = US_RIGHT - US_LEFT;

    const US_VERTICAL_SPAN = US_UPPER - US_LOWER;

    var multiple = 1;

    var CELL_DEGREE = DEGREE_1KM * multiple;

    var GRID_HORIZONTAL_SIZE = Math.ceil(US_HORIZONTAL_SPAN / CELL_DEGREE) + 1

    var GRID_VERTICAL_SIZE = Math.ceil(US_VERTICAL_SPAN / CELL_DEGREE) + 1


    function ScaleService(scale) {
        console.log("ScaleService is called. Scale = " + scale + " Meters.")
        multiple = scale / 1000;
        CELL_DEGREE = DEGREE_1KM * multiple
        GRID_HORIZONTAL_SIZE = Math.ceil(US_HORIZONTAL_SPAN / CELL_DEGREE) + 1
        GRID_VERTICAL_SIZE = Math.ceil(US_VERTICAL_SPAN / CELL_DEGREE) + 1
        console.log("CELL_LEN_SCALE=",CELL_DEGREE)
        console.log("GRID_HORIZONTAL_SIZE=",GRID_HORIZONTAL_SIZE)
        console.log("GRID_VERTICAL_SIZE=",GRID_VERTICAL_SIZE)
    };


    ScaleService.prototype.size= function () {
        return [GRID_HORIZONTAL_SIZE, GRID_VERTICAL_SIZE];
    }

    ScaleService.prototype.boundings= function () {
        return [US_UPPER, US_LOWER, US_LEFT, US_RIGHT];
    }

    ScaleService.prototype.getGeoTransform=function(){
        return Array.of(US_LEFT, CELL_DEGREE, 0, US_UPPER, 0, 0 - CELL_DEGREE);
    }

    ScaleService.prototype.walkGrids= function (callback) {
        for (var i= 0; i< GRID_HORIZONTAL_SIZE; i++) {
            for (var j = 0; j < GRID_VERTICAL_SIZE; j++) {
                callback(i,j);
            }
        }
    }

    ScaleService.prototype.gridIndex=function (coordinates) {
        var grid_horz = 0;
        var grid_vert = 0;

        var horz_found = false;
        var vert_found = false;

        if (coordinates == null) {
            return null;
        }

        if (coordinates[0] >= US_LEFT && coordinates[0] <= US_RIGHT) {
            grid_horz = Math.ceil((coordinates[0] - US_LEFT) / CELL_DEGREE)
            horz_found = true;
        }

        if (coordinates[1] >= US_LOWER && coordinates[1] <= US_UPPER) {
            grid_vert = Math.ceil((coordinates[1] - US_LOWER) / CELL_DEGREE)
            vert_found = true;
        }

        var result = [horz_found?grid_horz:null, vert_found?grid_vert:null];
        //console.log(result)
        return result;
    };

exports.scale=function(scale){
    return new ScaleService(scale)
};


}())
