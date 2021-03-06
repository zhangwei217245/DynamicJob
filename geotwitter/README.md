# Configuration File:

* Name: geotwitter.yaml
* Field: 
``` 
     server:
       port: 3000  # express server port (if any)
     database:
       host: '127.0.0.1' # redis server ip
       port: 6379   # redis server port
       db: 0  # redis db number
     filedir: '/home/wesley/Data'  # file directory
     scale: 500  # scale in meters.
```

# Initializing the Project:

```
     npm install
```

# Boot Arguments:

* For running data aggregator:
     * Initiating the master instance.
     
     ```
     node --max_old_space_size=4096 index.js -c default -s 0.5 -t UserCountExtractor -d /home/wesley/Data
     ```
     
     * Initiating the slave instances.
     
     ```
     node --max_old_space_size=4096 index.js -p false -c default -s 0.5 -t UserCountExtractor -d /home/wesley/Data
     ```

* For running image generator:
```
node --max_old_space_size=4096 GTiffGen.js -o pic.tif -c default -t UserCountExtractor
```
