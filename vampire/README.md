# Compile

```
mvn clean package
```


# Deploy

Go to the target directory, copy the vampire-bin.tar.gz to the directory 
where you want to deploy your program, and execute the command below:

```
tar zxvf vampire-bin.tar.gz
cd vampire
cd bin
./vampire <keywords1 keywords2 ... keywordsn> buffersize
```

