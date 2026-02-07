package io.quarkus.test.junit;

import java.util.Optional;

import io.quarkus.bootstrap.app.AugmentAction;
import io.quarkus.bootstrap.app.CuratedApplication;

public record PrepareResult(
        AugmentAction augmentAction,
        CuratedApplication curatedApplication,
        Optional<QuarkusTestProfile> profile) {
}
