from osgeo import gdal, ogr, osr
import numpy as np


class InfoExtractor:
    def __init__(self, scale_service, redis):
        self.scale_service = scale_service
        self.redis = redis
        return

    def extract(self, tweet):
        print(tweet)

    def init_write(self, file_path):
        print(file_path, self.scale_service.size())

    def write_row(self, row_id, row_data, data_set):
        print(row_id, row_data, data_set)

    def flush_write(self, data_set):
        print(data_set, self.scale_service.size())

    @staticmethod
    def factory(name, scale_service, redis):
        for subclass in InfoExtractor.__subclasses__():
            if subclass.__name__.lower().find(name.lower()) >= 0:
                return subclass.get_instance(scale_service, redis)
        assert 0, "Bad InfoExtractor creation " + name

    @staticmethod
    def get_instance(scale_service, redis):
        return InfoExtractor(scale_service, redis)


class UserCountExtractor(InfoExtractor):
    pic_format = "GTiff"

    def extract(self, tweet):
        if tweet['coordinates'] is not None:
            try:
                grid_idx = self.scale_service.getGridIndex(tweet['coordinates']['coordinates'])
                if grid_idx is not None and grid_idx[0] is not None and grid_idx[1] is not None:
                    user_id = tweet['user']['id']
                    self.redis.sadd(type(self).__name__ + "," + ('%.4f' % self.scale_service.getScale()) + ',' +
                                    (','.join(map(str, grid_idx))), user_id)
            except Exception as e:
                print(e)
        return

    def init_write(self, file_path):
        driver = gdal.GetDriverByName(self.pic_format)
        data_set = driver.Create(file_path, self.scale_service.size()[0], self.scale_service.size()[1], 1,
                                 gdal.GDT_Int32)
        data_set.SetGeoTransform(self.scale_service.getGeoTransform())
        band = data_set.GetRasterBand(1)
        band.SetNoDataValue(0)
        band.Fill(0, 0.0)

        out_rasters_rs = osr.SpatialReference()
        out_rasters_rs.ImportFromEPSG(self.scale_service.getESPG())
        data_set.SetProjection(out_rasters_rs.ExportToWkt())

        return data_set

    def write_row(self, row_id, row_data, data_set):
        band = data_set.GetRasterBand(1)
        try:
            band.WriteArray(row_data, 0, row_id)
        except Exception as e:
            print(type(row_id), row_id, type(row_data), len(row_data), e)
        return data_set

    def flush_write(self, data_set, file_path):
        band = data_set.GetRasterBand(1)
        band.FlushCache()
        data_set.FlushCache()

    @staticmethod
    def get_instance(scale_service, redis):
        return UserCountExtractor(scale_service, redis)
