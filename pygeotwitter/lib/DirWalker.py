import concurrent.futures
import os
import time
import redis

from lib.InfoExtractor import *
from lib.TweetsReader import *


class DirWalker:
    def __init__(self, root_dir, suffix, task, scale_service, redis_pool, worker_count):
        self.root_dir = root_dir
        self.suffix = suffix
        self.task = task
        self.scale_service = scale_service
        self.redis_pool = redis_pool
        self.executor = concurrent.futures.ThreadPoolExecutor(max_workers=worker_count)

    def process_file(self, file_path):
        if file_path.endswith(self.suffix):
            rds = redis.Redis(connection_pool=self.redis_pool)
            extractor = InfoExtractor.factory(self.task, self.scale_service, rds)
            reader = TweetsReader.factory(self.suffix, info_extractor=extractor)
            print('Processing file ', file_path)
            try:
                reader.readfile(file_path)
            except Exception as e:
                print(e)
            return
        else:
            return

    def walk(self):
        futures = []
        file_count = 0
        for root, dateDirs, files in os.walk(self.root_dir, topdown=True, onerror=None, followlinks=False):
            for file in sorted(files):
                file_count += 1
                file_path = root + '/' + file
                # for parallelism, use the line below:
                futures.append(self.executor.submit(self.process_file, file_path))
                # For simplicity, use the line below:
                # self.process_file(file_path)
        while len(concurrent.futures.wait(futures)[1]) > 0:
            time.sleep(2)
        return file_count
