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

# Boot Arguments:

* For running data aggregator:
```node --max_old_space_size=2048 index.js conf=default```

* For running image generator:
```node --max_old_space_size=2048 GTiffGen.js conf=default```