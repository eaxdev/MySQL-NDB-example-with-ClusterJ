[![Build Status](https://travis-ci.com/eaxdev/MySQL-NDB-example-with-ClusterJ.svg?branch=master)](https://travis-ci.com/eaxdev/MySQL-NDB-example-with-ClusterJ)

# MySQL-NDB-with-TestContainers
Example for using `MySQL NDB Cluster` (via `ClusterJ` library) for test 
with `TestContainers`

* Install `docker`

* Download `NDB ClusterJ (Java Connector for NDB)` library and install 
it via package manager: https://dev.mysql.com/downloads/cluster/

* Download `ndbclient_7.6.9`

* add clusterj dependency via `Maven` to local repo: 
```bash
mvn install:install-file -DgroupId=com.mysql.ndb -DartifactId=clusterj -Dversion=7.6.9 -Dpackaging=jar -Dfile=clusterj-7.6.9.jar -DgeneratePom=true
```

* Run tests via `Maven`:

```bash
mvn -DargLine="-Djava.library.path=/usr/lib/x86_64-linux-gnu/" clean test
```

when `/usr/lib/x86_64-linux-gnu/` path to `ndbclient_7.6.9.so`

For example, see `.travis.yml` for more details.
