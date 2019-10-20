package ru.example.clusterj;

import com.mysql.clusterj.Query;
import com.mysql.clusterj.Session;
import com.mysql.clusterj.SessionFactory;
import com.mysql.clusterj.query.PredicateOperand;
import com.mysql.clusterj.query.QueryBuilder;
import com.mysql.clusterj.query.QueryDomainType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.example.clusterj.junit5.EnableMySQLClusterContainer;
import ru.example.clusterj.model.User;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;


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

    @Test
    void queryBuilderTest() {
        User newUser1 = session.newInstance(User.class);
        newUser1.setId(1);
        newUser1.setFirstName("John");
        newUser1.setLastName("Jonson");

        User newUser2 = session.newInstance(User.class);
        newUser2.setId(2);
        newUser2.setFirstName("Alex");
        newUser2.setLastName("Jonson");

        session.persist(newUser1);
        session.persist(newUser2);

        QueryBuilder builder = session.getQueryBuilder();
        QueryDomainType<User> userQueryDomainType = builder.createQueryDefinition(User.class);
        // parameter
        PredicateOperand propertyIdParam = userQueryDomainType.param("lastName");
        // property
        PredicateOperand propertyEntityId = userQueryDomainType.get("lastName");
        userQueryDomainType.where(propertyEntityId.equal(propertyIdParam));

        Query<User> query = session.createQuery(userQueryDomainType);
        query.setParameter("lastName", "Jonson");
        List<User> foundEntities = query.getResultList();
        Optional<User> firstUser = foundEntities.stream().filter(u -> u.getId() == 1).findFirst();
        Optional<User> secondUser = foundEntities.stream().filter(u -> u.getId() == 2).findFirst();

        assertAll(
                () -> assertEquals(foundEntities.size(), 2),
                () -> assertTrue(firstUser.isPresent()),
                () -> assertTrue(secondUser.isPresent()),
                () -> assertThat(firstUser.get(),
                        allOf(
                                hasProperty("firstName", equalTo("John")),
                                hasProperty("lastName", equalTo("Jonson"))
                        )
                ),
                () -> assertThat(secondUser.get(),
                        allOf(
                                hasProperty("firstName", equalTo("Alex")),
                                hasProperty("lastName", equalTo("Jonson"))
                        )
                )
        );
    }

    @Test
    void andOrNotImplemented() {
        QueryBuilder builder = session.getQueryBuilder();
        QueryDomainType<User> userQueryDomainType = builder.createQueryDefinition(User.class);

        // parameter
        PredicateOperand firstNameParam = userQueryDomainType.param("firstName");
        // property
        PredicateOperand firstName = userQueryDomainType.get("firstName");

        // parameter
        PredicateOperand lastNameParam = userQueryDomainType.param("lastName");
        // property
        PredicateOperand lastName = userQueryDomainType.get("lastName");

        // parameter
        PredicateOperand idParam = userQueryDomainType.param("id");
        // property
        PredicateOperand id = userQueryDomainType.get("id");

        Executable executable = () -> userQueryDomainType.where(firstNameParam.equal(firstName)
                .and(lastNameParam.equal(lastName))
                .or(idParam.equal(id)));

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, executable);

        assertEquals("Not implemented.", exception.getMessage());
    }

    @AfterEach
    void tearDown() {
        session.deletePersistentAll(User.class);
        session.close();
    }
}
