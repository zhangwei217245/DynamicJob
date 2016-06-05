#!/bin/bash

echo "Please execute this as sudoer."

packages=(pyyaml redis numpy gdal)

python3 -m pip install --upgrade pip

for pack in ${packages[@]};
do
    echo "Installing ${pack} ... "
    python3 -m pip install --upgrade ${pack}
    echo "============================================"
done

exit 0;