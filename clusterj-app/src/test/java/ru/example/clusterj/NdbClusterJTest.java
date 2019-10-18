package ru.example.clusterj;

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
import ru.example.clusterj.junit5.EnableMySQLClusterContainer;
import ru.example.clusterj.model.User;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@EnableMySQLClusterContainer
class NdbClusterJTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SessionFactory sessionFactory;

    private Session session;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `user` (id INT NOT NULL PRIMARY KEY," +
                "     firstName VARCHAR(64) DEFAULT NULL," +
                "     lastName VARCHAR(64) DEFAULT NULL) ENGINE=NDBCLUSTER;");
        session = sessionFactory.getSession();
    }

    @Test
    void shouldGetUserViaClusterJ() {
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
