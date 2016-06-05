import sys
import os
import argparse
import yaml  # pip install pyyaml
import numpy as np

parent_dir = os.path.abspath(os.path.pardir)
sys.path.insert(0, parent_dir)

from lib.DirWalker import *
from lib.ScaleService import *


def load_config(conf):
    try:
        config = yaml.load(open(parent_dir + "/etc/geotwitter.yaml", 'r'))[conf]
    except yaml.YAMLError as exc:
        print("Error in configuration file:", exc)
    else:
        return config


def process_console_args():
    parser = argparse.ArgumentParser('TwitterExtractor.py')
    parser.add_argument('-c', '--config', metavar='<default/...>', default='default',
                        help='The file path of the configuration file.')
    parser.add_argument('-t', '--task', metavar='<usercount/...>', help='Specify the task you wanna execute.',
                        required=True)
    parser.add_argument('-s', '--scale', metavar='<0.5/1.0/2.0/2.5/...>',
                        help='The scale ratio of both x resolution and y resolution', type=float, default=None)
    parser.add_argument('-o', '--output', metavar='<./pic.tif>', help='The file name of the output', default=None)
    parser.add_argument('-f', '--format', metavar='<gz/txt>', help='The format of the input file, by default, '
                                                                   'it is "gz"', default='gz')
    parser.add_argument('-n', '--thread_num', metavar='<4/8/12/16/...>', type=int, help='The number of threads',
                        default=8)
    parser.add_argument('-p', '--purge', metavar='<Y/N>', default='N', help='If Y, purge the data in redis')
    parser.add_argument('dir', metavar='<root_dir>',
                        help='The path of the root directory of the data files. '
                             'This directory should contain several sub-directories named by the date on which the data'
                             ' is collected. \nIf you only pass an empty directory as the argument, then the program'
                             ' will do nothing but only generate the image, in this case, you can specify the -o'
                             ' argument for the name of the output file, but if you omit -o argument, the output file'
                             ' will be named as "output.tif"')

    args = parser.parse_args()
    return args


def fill_row(scale_service, extractor, rds, data_set, r):
    row_num = 1000
    if scale_service.size()[1] - r < 1000:
        row_num = scale_service.size()[1] - r
    arr = np.full((row_num, scale_service.size()[0]), 0).astype(np.int32)

    append = str(int(r/1000)) + '???'
    keys = []
    if r < 1000:
        append = '???'
        key_pattern = type(extractor).__name__ + ',' + ('%.4f' % scale_service.getScale()) + ',*,?'
        keys.extend(rds.keys(key_pattern))
        key_pattern = type(extractor).__name__ + ',' + ('%.4f' % scale_service.getScale()) + ',*,??'
        keys.extend(rds.keys(key_pattern))

    key_pattern = type(extractor).__name__ + ',' + ('%.4f' % scale_service.getScale()) + ',*,' + append
    keys.extend(rds.keys(key_pattern))

    for k in keys:
        key_arr = eval(k)
        col_idx = key_arr[len(key_arr) - 2]
        row_idx = key_arr[len(key_arr) - 1] % 1000
        arr[row_idx][col_idx] = rds.scard(k)

    extractor.write_row(r, arr, data_set)
    print('r=', r, 'arr=', len(arr))


def main():
    args = process_console_args()

    file_suffix = args.format
    worker_count = args.thread_num
    root_dir = args.dir
    scale = args.scale if args.scale is not None else None
    config = load_config(args.config)
    task = args.task
    output = args.output
    purge = args.purge

    print('scale = ', scale)
    # initiate scale service
    scale_service = ScaleService(config, scale=scale)

    print(scale_service.nrow, scale_service.ncol, scale_service.ncell)

    # Redis connection Pool
    pool = redis.ConnectionPool(host=config['redis']['host'], port=config['redis']['port'], db=config['redis']['db'])
    rds = redis.Redis(connection_pool=pool)

    if purge == 'Y':
        # clean redis db
        rds.flushdb()

    # DirWaker
    walker = DirWalker(root_dir, file_suffix, task, scale_service, pool, worker_count)
    file_count = walker.walk()

    executor = concurrent.futures.ThreadPoolExecutor(max_workers=worker_count)

    print(file_count, ' files has been processed.')

    if output is not None:
        print('DrawImageNow')
        futures = []
        extractor = InfoExtractor.factory(task, scale_service, rds)
        data_set = extractor.init_write(output)

        for r in range(0, scale_service.size()[1], 1000):
            fill_row(scale_service, extractor, rds, data_set, r)
            #futures.append(executor.submit(fill_row, scale_service, extractor, rds, data_set, r))

        while len(concurrent.futures.wait(futures)[1]) > 0:
            time.sleep(2)
        extractor.flush_write(data_set, output)

if __name__ == "__main__":
    main()
