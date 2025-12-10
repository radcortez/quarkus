package io.quarkus.test.junit;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;

import io.quarkus.test.common.ListeningAddress;
import io.quarkus.test.common.TestResourceManager;

public class IntegrationTestExtensionState extends QuarkusTestExtensionState {

    public IntegrationTestExtensionState(TestResourceManager testResourceManager, Closeable resource, Runnable clearCallbacks,
            Optional<ListeningAddress> listeningAddress) {
        super(testResourceManager, resource, clearCallbacks, listeningAddress);
    }

    @Override
    protected void doClose() throws IOException {
        testResourceManager.close();
        resource.close();
    }
}
