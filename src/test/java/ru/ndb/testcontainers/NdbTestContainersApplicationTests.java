package ru.ndb.testcontainers;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore(value = "Revise DataSource bean definition")
public class NdbTestContainersApplicationTests {

    @Test
    public void contextLoads() {
    }

}
