To run the program just enter the training1 folder and run the following command:

```
mvn compile exec:java -Dexec.mainClass=training1.<application-name> -Dexec.args="--runner=DirectRunner --parallelism=4"
```

With these flags maven will be started.