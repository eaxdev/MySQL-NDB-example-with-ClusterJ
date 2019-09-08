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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.ndb.testcontainers.junit5.EnableMySQLClusterContainer;
import ru.ndb.testcontainers.model.User;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@EnableMySQLClusterContainer
class NdbClusterJTest {

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
        Properties props = new Properties();
        props.put("com.mysql.clusterj.connectstring", System.getProperty("clusterj.connectString"));
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
}
