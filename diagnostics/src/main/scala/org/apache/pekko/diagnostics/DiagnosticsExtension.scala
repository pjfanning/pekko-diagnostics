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

package org.apache.pekko.diagnostics

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.actor.ClassicActorSystemProvider
import org.apache.pekko.actor.ExtendedActorSystem
import org.apache.pekko.actor.Extension
import org.apache.pekko.actor.ExtensionId
import org.apache.pekko.actor.ExtensionIdProvider

/**
 * The diagnostics extension enables the [[StarvationDetector]] and reports configuration issues with [[ConfigChecker]]
 * for an `ActorSystem`. This extension is automatically activated when the `pekko-diagnostics` dependency is included.
 */
object DiagnosticsExtension extends ExtensionId[DiagnosticsExtension] with ExtensionIdProvider {
  override def get(system: ActorSystem): DiagnosticsExtension = super.get(system)
  override def get(system: ClassicActorSystemProvider): DiagnosticsExtension = super.get(system)
  override def lookup: ExtensionId[_ <: Extension] = DiagnosticsExtension
  override def createExtension(system: ExtendedActorSystem): DiagnosticsExtension = new DiagnosticsExtension(system)
}

class DiagnosticsExtension(system: ExtendedActorSystem) extends Extension {
  StarvationDetector.checkSystemDispatcher(system)
  StarvationDetector.checkInternalDispatcher(system)
  ConfigChecker.reportIssues(system)
}
