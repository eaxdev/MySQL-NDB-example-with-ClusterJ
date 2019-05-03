# MySQL-NDB-with-TestContainers
Example for using MySQL NDB Cluster for test with TestContainers

* Download `NDB ClusterJ (Java Connector for NDB)` library and install 
it via package manager: https://dev.mysql.com/downloads/cluster/

* Download `ndbclient_7.6.10`

* add clusterj dependency via `Maven` to local repo: 
```bash
mvn install:install-file -DgroupId=com.mysql.ndb -DartifactId=clusterj -Dversion=7.6.9 -Dpackaging=jar -Dfile=clusterj-7.6.9.jar -DgeneratePom=true
```
