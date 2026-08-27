/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, which was derived from Akka.
 */

/*
 * Copyright (C) 2023 Lightbend Inc. <https://www.lightbend.com>
 */

package jdocs;

import org.apache.pekko.actor.typed.ActorSystem;
//#other-dispatcher
import org.apache.pekko.diagnostics.StarvationDetector;

//#other-dispatcher

public class StarvationDetectorDocSample {

    static void illustrateOtherDispatcher(ActorSystem<?> system) {
        //#other-dispatcher
        String dispatcherConfigPath = "my-dispatcher";
        StarvationDetector.checkDispatcher(system, dispatcherConfigPath);
        //#other-dispatcher
    }
}
