package ru.example.clusterj.junit5;

import com.github.dockerjava.api.model.Network;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.Extension;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.time.Duration;
import java.util.stream.Stream;

@Slf4j
class MySQLClusterTcExtension implements Extension {

    private static final String MYSQL_USER = "sys";

    private static final String MYSQL_PASSWORD = "qwerty";

    private static final String CLUSTERJ_DATABASE = "NDB_DB";

    private static final String IMAGE_NAME = "mysql/mysql-cluster:8.0.23";

    private static Network.Ipam getIpam() {
        Network.Ipam ipam = new Network.Ipam();
        ipam.withDriver("default");
        Network.Ipam.Config config = new Network.Ipam.Config();
        config.withSubnet("192.168.0.0/16");
        ipam.withConfig(config);
        return ipam;
    }

    private static org.testcontainers.containers.Network network = org.testcontainers.containers.Network.builder()
            .createNetworkCmdModifier(createNetworkCmd -> createNetworkCmd.withIpam(getIpam()))
            .build();

    private static GenericContainer ndbMgmd = new GenericContainer<>(IMAGE_NAME)
            .withNetwork(network)
            .withClasspathResourceMapping("mysql-cluster.cnf",
                    "/etc/mysql-cluster.cnf",
                    BindMode.READ_ONLY)
            .withClasspathResourceMapping("my.cnf",
                    "/etc/my.cnf",
                    BindMode.READ_ONLY)
            .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withIpv4Address("192.168.0.2"))
            .withCommand("ndb_mgmd")
            .withExposedPorts(1186)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(150)));

    private static GenericContainer ndbd1 = new GenericContainer<>(IMAGE_NAME)
            .withNetwork(network)
            .withClasspathResourceMapping("mysql-cluster.cnf",
                    "/etc/mysql-cluster.cnf",
                    BindMode.READ_ONLY)
            .withClasspathResourceMapping("my.cnf",
                    "/etc/my.cnf",
                    BindMode.READ_ONLY)
            .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withIpv4Address("192.168.0.3"))
            .withCommand("ndbd");

    private static GenericContainer ndbMysqld = new GenericContainer<>(IMAGE_NAME)
            .withNetwork(network)
            .withCommand("mysqld")
            .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withIpv4Address("192.168.0.10"))
            .withClasspathResourceMapping("mysql-cluster.cnf",
                    "/etc/mysql-cluster.cnf",
                    BindMode.READ_ONLY)
            .withClasspathResourceMapping("my.cnf",
                    "/etc/my.cnf",
                    BindMode.READ_ONLY)
            .waitingFor(Wait.forListeningPort())
            .withEnv(ImmutableMap.of("MYSQL_DATABASE", CLUSTERJ_DATABASE,
                    "MYSQL_USER", MYSQL_USER,
                    "MYSQL_PASSWORD", MYSQL_PASSWORD))
            .withExposedPorts(3306)
            .waitingFor(Wait.forListeningPort());


    static {
        log.info("Start MySQL Cluster testcontainers extension...\n");
        Stream.of(ndbMgmd, ndbd1, ndbMysqld).forEach(GenericContainer::start);

        String ndbUrl = ndbMgmd.getContainerIpAddress() + ":" + ndbMgmd.getMappedPort(1186);
        String mysqlUrl = ndbMysqld.getContainerIpAddress() + ":" + ndbMysqld.getMappedPort(3306);
        String mysqlConnectionString = "jdbc:mysql://" + mysqlUrl + "/" + CLUSTERJ_DATABASE + "?useUnicode=true" +
                "&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true";

        System.setProperty("clusterj.connectString", ndbUrl);
        System.setProperty("clusterj.dataBaseName", CLUSTERJ_DATABASE);
        System.setProperty("spring.datasource.username", MYSQL_USER);
        System.setProperty("spring.datasource.password", MYSQL_PASSWORD);
        System.setProperty("spring.datasource.url", mysqlConnectionString);
    }

}
