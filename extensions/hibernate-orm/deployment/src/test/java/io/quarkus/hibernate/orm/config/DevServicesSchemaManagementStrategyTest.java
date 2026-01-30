package io.quarkus.hibernate.orm.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.config.Config;

public class DevServicesSchemaManagementStrategyTest {

    // A simple runner like this will trigger Dev Services
    @RegisterExtension
    static QuarkusUnitTest runner = new QuarkusUnitTest()
            .withEmptyApplication();

    @Test
    public void testDevServices() {
        String value = Config.get().getValue("quarkus.hibernate-orm.schema-management.strategy", String.class);
        assertThat(value).isEqualTo("drop-and-create");
    }

}
