package ru.ndb.testcontainers;

import com.mysql.clusterj.ClusterJHelper;
import com.mysql.clusterj.Session;
import com.mysql.clusterj.SessionFactory;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.DockerComposeContainer;
import ru.ndb.testcontainers.model.User;

import javax.sql.DataSource;
import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
public class NdbClusterJTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    //-Djava.library.path=/usr/lib/x86_64-linux-gnu/

    private static final String MANAGEMENT_NODE_SERVICE_NAME = "ndb_mgmd";

    private static final int CLUSTERJ_NDB_PORT = 1186;

    private static final String MYSQL_NODE_SERVICE_NAME = "ndb_mysqld";

    private static final int MYSQL_PORT = 3306;

    private static final String DATABASE_NAME = "NDB_DB";

    @ClassRule
    public static DockerComposeContainer compose =
            new DockerComposeContainer(
                    new File("src/test/resources/docker-compose.yml"))
                    .withLocalCompose(true)
                    .withExposedService(MANAGEMENT_NODE_SERVICE_NAME, CLUSTERJ_NDB_PORT)
                    .withExposedService(MYSQL_NODE_SERVICE_NAME, MYSQL_PORT);


    @Before
    public void setUp() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `user` (id INT NOT NULL PRIMARY KEY," +
                "     firstName VARCHAR(64) DEFAULT NULL," +
                "     lastName VARCHAR(64) DEFAULT NULL) ENGINE=NDBCLUSTER;");
    }

    @Test
    public void shouldGetUserViaClusterJ() {
        String address = compose.getServiceHost(MANAGEMENT_NODE_SERVICE_NAME, CLUSTERJ_NDB_PORT) + ":" +
                compose.getServicePort(MANAGEMENT_NODE_SERVICE_NAME, CLUSTERJ_NDB_PORT);
        Properties props = new Properties();

        props.put("com.mysql.clusterj.connectstring", address);
        props.put("com.mysql.clusterj.database", "NDB_DB");

        SessionFactory factory = ClusterJHelper.getSessionFactory(props);
        Session session = factory.getSession();

        User newUser = session.newInstance(User.class);
        newUser.setId(1);
        newUser.setFirstName("John");
        newUser.setLastName("Jonson");

        session.persist(newUser);

        User userFromDb = session.find(User.class, 1);

        assertEquals(userFromDb.getId(), 1);
        assertEquals(userFromDb.getFirstName(), "John");
        assertEquals(userFromDb.getLastName(), "Jonson");
    }

    @TestConfiguration
    public static class DataSourceConfig {

        private final String url = compose.getServiceHost(MYSQL_NODE_SERVICE_NAME, MYSQL_PORT) + ":" +
                compose.getServicePort(MYSQL_NODE_SERVICE_NAME, MYSQL_PORT) + "/" + DATABASE_NAME;

        @Bean
        public DataSource dataSource() {
            SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setUrl("jdbc:mysql://" + url + "?useSSL=false");
            dataSource.setUsername("sys");
            dataSource.setPassword("qwerty");
            dataSource.setSuppressClose(true);
            return dataSource;
        }
    }
}
