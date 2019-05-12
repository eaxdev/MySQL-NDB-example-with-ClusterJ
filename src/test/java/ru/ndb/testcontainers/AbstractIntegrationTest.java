package ru.ndb.testcontainers;

import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;

public abstract class AbstractIntegrationTest {
    private static final String MANAGEMENT_NODE_SERVICE_NAME = "ndb_mgmd";

    private static final int CLUSTERJ_NDB_PORT = 1186;

    private static final String MYSQL_NODE_SERVICE_NAME = "ndb_mysqld";

    private static final int MYSQL_PORT = 3306;

    public static DockerComposeContainer compose =
            new DockerComposeContainer(
                    new File("src/test/resources/docker-compose.yml"))
                    .withLocalCompose(true)
                    .withExposedService(MANAGEMENT_NODE_SERVICE_NAME, CLUSTERJ_NDB_PORT)
                    .withExposedService(MYSQL_NODE_SERVICE_NAME, MYSQL_PORT);

    static {
        compose.start();
    }
}
