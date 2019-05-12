package ru.ndb.testcontainers;

import com.github.dockerjava.api.model.Network.Ipam;
import com.github.dockerjava.api.model.Network.Ipam.Config;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Map;
import java.util.stream.Stream;

public abstract class AbstractIntegrationTest {

    private static Ipam getIpam() {
        Ipam ipam = new Ipam();
        ipam.withDriver("default");
        Config config = new Config();
        config.withSubnet("192.168.0.0/16");
        ipam.withConfig(config);
        return ipam;
    }

    private static Network network = Network.builder()
            .createNetworkCmdModifier(createNetworkCmd -> createNetworkCmd.withIpam(getIpam()))
            .build();

    static GenericContainer ndbMgmd = new GenericContainer<>("mysql/mysql-cluster")
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
            .waitingFor(Wait.forLogMessage(".*MySQL Cluster Management Server mysql-5.7.25 ndb-7.6.9 started.*", 1));

    private static GenericContainer ndbd1 = new GenericContainer<>("mysql/mysql-cluster")
            .withNetwork(network)
            .withClasspathResourceMapping("mysql-cluster.cnf",
                    "/etc/mysql-cluster.cnf",
                    BindMode.READ_ONLY)
            .withClasspathResourceMapping("my.cnf",
                    "/etc/my.cnf",
                    BindMode.READ_ONLY)
            .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withIpv4Address("192.168.0.3"))
            .withCommand("ndbd");

    static GenericContainer ndbMysqld = new GenericContainer<>("mysql/mysql-cluster")
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
            .withEnv(Map.of("MYSQL_DATABASE", "NDB_DB",
                    "MYSQL_USER", "sys",
                    "MYSQL_PASSWORD", "qwerty"))
            .withExposedPorts(3306)
            .waitingFor(Wait.forListeningPort());


    static {
        Stream.of(ndbMgmd, ndbd1, ndbMysqld)
                //.parallel()
                .forEach(GenericContainer::start);
    }
}
