import math


class ScaleService:
    def __init__(self, config, scale=1.0):
        self.xmin=config['extent']['xmin']
        self.xmax=config['extent']['xmax']
        self.ymin=config['extent']['ymin']
        self.ymax=config['extent']['ymax']

        self.horizontal_span = self.xmax - self.xmin
        self.vertical_span = self.ymax - self.ymin

        self.scale = scale
        self.cell_xsize=config['resolution']['x'] * scale
        self.cell_ysize=config['resolution']['y'] * scale

        self.ncol = math.ceil(self.horizontal_span / self.cell_xsize)
        self.nrow = math.ceil(self.vertical_span / self.cell_ysize)
        self.ncell = self.nrow * self.ncol

        self.EPSG = config['EPSG']

    def size(self):
        return [self.ncol, self.nrow];

    def boundings(self):
        return [self.xmin, self.xmax, self.ymin, self.ymax]

    def getGeoTransform(self):
        return self.xmin, self.cell_xsize, 0, self.ymax, 0, 0 - self.cell_ysize

    def getESPG(self):
        return self.EPSG

    def getScale(self):
        return self.scale

    def getGridIndex(self, coordinates):
        grid_horz = 0
        grid_vert = 0

        horz_found = False
        vert_found = False

        if coordinates is None:
            return None

        if self.xmin <= coordinates[0] <= self.xmax:
            grid_horz = math.floor((coordinates[0] - self.xmin) / self.cell_xsize)
            horz_found = True

        if self.ymin <= coordinates[1] <= self.ymax:
            grid_vert = math.floor((self.ymax - coordinates[1]) / self.cell_ysize)
            vert_found = True

        result = [grid_horz if horz_found else None, grid_vert if vert_found else None]
        return result
