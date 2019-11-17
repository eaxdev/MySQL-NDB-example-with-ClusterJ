[![Build Status](https://github.com/eaxdev/MySQL-NDB-example-with-ClusterJ/workflows/build/badge.svg)](https://github.com/eaxdev/MySQL-NDB-example-with-ClusterJ/actions)
[![codecov](https://codecov.io/gh/eaxdev/MySQL-NDB-example-with-ClusterJ/branch/master/graph/badge.svg)](https://codecov.io/gh/eaxdev/MySQL-NDB-example-with-ClusterJ)

# MySQL-NDB-example-with-ClusterJ
This is example for using `MySQL NDB Cluster` (via `ClusterJ` library).
 
Using:
* `SpringBoot`
* `ClusterJ`
* `TestContainers`
* `JUnit5`

Preparation for launch: 

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

For example, see `.github/workflows/maven-ci-build.yml` for more details.
