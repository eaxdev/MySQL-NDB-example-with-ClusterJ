package ru.ndb.testcontainers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Disabled(value = "Revise DataSource bean definition")
class NdbTestContainersApplicationTests {

    @Test
    void contextLoads() {
    }

}
