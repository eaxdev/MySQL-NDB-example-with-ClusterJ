package ru.ndb.testcontainers;

import com.mysql.clusterj.ClusterJHelper;
import com.mysql.clusterj.Session;
import com.mysql.clusterj.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.ndb.testcontainers.model.User;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
public class NdbClusterJTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    //-Djava.library.path=/usr/lib/x86_64-linux-gnu/

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `user` (id INT NOT NULL PRIMARY KEY," +
                "     firstName VARCHAR(64) DEFAULT NULL," +
                "     lastName VARCHAR(64) DEFAULT NULL) ENGINE=NDBCLUSTER;");
    }

    @Test
    void shouldGetUserViaClusterJ() {
        Integer mappedPort = ndbMgmd.getMappedPort(1186);
        String containerIpAddress = ndbMgmd.getContainerIpAddress();
        Properties props = new Properties();

        props.put("com.mysql.clusterj.connectstring", containerIpAddress + ":" + mappedPort);
        props.put("com.mysql.clusterj.database", "NDB_DB");

        SessionFactory factory = ClusterJHelper.getSessionFactory(props);
        Session session = factory.getSession();

        User newUser = session.newInstance(User.class);
        newUser.setId(1);
        newUser.setFirstName("John");
        newUser.setLastName("Jonson");

        session.persist(newUser);

        User userFromDb = session.find(User.class, 1);

        assertAll(
                () -> assertEquals(userFromDb.getId(), 1),
                () -> assertEquals(userFromDb.getFirstName(), "John"),
                () -> assertEquals(userFromDb.getLastName(), "Jonson"));
    }

    @TestConfiguration
    public static class DataSourceConfig {

        @Bean
        public DataSource dataSource() {
            Integer mappedPort = ndbMysqld.getMappedPort(3306);
            String containerIpAddress = ndbMysqld.getContainerIpAddress();
            SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setUrl("jdbc:mysql://" + containerIpAddress + ":" + mappedPort + "/NDB_DB?useSSL=false");
            dataSource.setUsername("sys");
            dataSource.setPassword("qwerty");
            dataSource.setSuppressClose(true);
            return dataSource;
        }
    }
}
