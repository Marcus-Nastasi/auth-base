package com.auth.core.shared;

import java.time.Clock;
import java.time.ZoneId;

public final class Constants {

    public static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");
    public static final Clock CLOCK = Clock.systemDefaultZone().withZone(ZONE_ID);
}
