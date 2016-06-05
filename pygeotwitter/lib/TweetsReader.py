import json
import gzip


class TweetsReader:
    jsonDecoder = json.JSONDecoder()

    def __init__(self, suffix, info_extractor):
        self.suffix = suffix
        self.infoExtractor = info_extractor

    def open_file_handle(self, file_path):
        return None

    def readfile(self, file_path):
        file_obj = self.open_file_handle(file_path)
        for line in file_obj:
            try:
                if len(line) > 0 and line.find('|') > 0:
                    json_str = line.split('|', 1)[1]
                    tweet = TweetsReader.jsonDecoder.decode(json_str)
                    self.infoExtractor.extract(tweet)
            except Exception as e:
                print(e)
        return

    @staticmethod
    def factory(suffix, info_extractor):
        if suffix == "txt": return TxtTweetsReader(suffix, info_extractor)
        if suffix == "gz": return GzipTweetsReader(suffix, info_extractor)
        assert 0, "Bad TweetsReader creation" + type


class TxtTweetsReader(TweetsReader):
    def open_file_handle(self, file_path):
        file_obj = open(file_path, 'rt')
        return file_obj


class GzipTweetsReader(TweetsReader):
    def open_file_handle(self, file_path):
        file_obj = gzip.open(file_path, 'rt')
        return file_obj
