package ch.mno.copper;

import ch.mno.copper.collect.connectors.HttpConnectorTestController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        CopperApplication.class,
        HttpConnectorTestController.class
}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public abstract class AbstractWebPortSpringTest {

    @LocalServerPort
    protected int port;

    @BeforeAll
    public static void beforeClass() {
        System.setProperty("copper.properties", "src/test/resources/tests.properties");
    }
}
