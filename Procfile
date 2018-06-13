web: java $JAVA_OPTS='-Xmx384m -Xms384m -Xss512k -XX:+UseCompressedOops' -jar web-service/target/web-service-0.0.1-SNAPSHOT.jar --server.port=$PORT
worker: java $JAVA_OPTS='-Xmx384m -Xms384m -Xss512k -XX:+UseCompressedOops' -jar worker-service/target/worker-service-0.0.1-SNAPSHOT.jar
