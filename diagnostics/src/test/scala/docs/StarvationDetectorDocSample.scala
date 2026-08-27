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

package docs

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors

object StarvationDetectorDocSample {

  val system: ActorSystem[_] = ActorSystem[Nothing](Behaviors.empty[Nothing], "Doc")

  // #other-dispatcher
  import org.apache.pekko.diagnostics.StarvationDetector

  StarvationDetector.checkDispatcher(system, dispatcherConfigPath = "my-dispatcher")
  // #other-dispatcher

}
