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

import java.lang.reflect.Modifier

import scala.concurrent.Future
import scala.concurrent.duration._

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.dispatch.Dispatchers
import org.apache.pekko.event.Logging
import org.apache.pekko.event.LoggingAdapter
import org.apache.pekko.testkit.DeadLettersFilter
import org.apache.pekko.testkit.TestEvent.Mute
import org.apache.pekko.testkit.TestKit
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.scalactic.CanEqual
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

object PekkoSpec {
  val testConf: Config = ConfigFactory.parseString("""
      pekko {
        use-slf4j = false # This is needed to avoid overriding below conf `loggers`
                          #  with `["org.apache.pekko.event.slf4j.Slf4jLogger"]` when importing `pekko-persistence-testkit`
        loggers = ["org.apache.pekko.testkit.TestEventListener"]
        loglevel = "WARNING"
        stdout-loglevel = "WARNING"
        actor {
          default-dispatcher {
            executor = "fork-join-executor"
            fork-join-executor {
              parallelism-min = 8
              parallelism-max = 8
            }
          }
        }
      }
                                                    """)

  def mapToConfig(map: Map[String, Any]): Config = {
    import scala.collection.JavaConverters._
    ConfigFactory.parseMap(map.asJava)
  }

  def testNameFromCallStack(classToStartFrom: Class[_]): String = {

    def isAbstractClass(className: String): Boolean = {
      try {
        Modifier.isAbstract(Class.forName(className).getModifiers)
      } catch {
        case _: Throwable => false // yes catch everything, best effort check
      }
    }

    val startFrom = classToStartFrom.getName
    val filteredStack = Thread.currentThread.getStackTrace.iterator
      .map(_.getClassName)
      // drop until we find the first occurrence of classToStartFrom
      .dropWhile(!_.startsWith(startFrom))
      // then continue to the next entry after classToStartFrom that makes sense
      .dropWhile {
        case `startFrom`                            => true
        case str if str.startsWith(startFrom + "$") => true // lambdas inside startFrom etc
        case str if isAbstractClass(str)            => true
        case _                                      => false
      }

    if (filteredStack.isEmpty)
      throw new IllegalArgumentException(s"Couldn't find [${classToStartFrom.getName}] in call stack")

    // sanitize for actor system name
    scrubActorSystemName(filteredStack.next())
  }

  /**
   * Sanitize the `name` to be used as valid actor system name by replacing invalid characters. `name` may for example
   * be a fully qualified class name and then the short class name will be used.
   */
  def scrubActorSystemName(name: String): String = {
    name
      .replaceFirst("""^.*\.""", "") // drop package name
      .replaceAll("""\$\$?\w+""", "") // drop scala anonymous functions/classes
      .replaceAll("[^a-zA-Z_0-9-]", "_")
      .replaceAll("""MultiJvmNode\d+""", "") // drop MultiJvm suffix
  }

}

abstract class PekkoSpec(_system: ActorSystem)
    extends TestKit(_system)
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with ScalaFutures {

  implicit val patience: PatienceConfig = PatienceConfig(testKitSettings.DefaultTimeout.duration)

  def this(config: Config) = this(
    ActorSystem(
      PekkoSpec.testNameFromCallStack(classOf[PekkoSpec]),
      ConfigFactory.load(config.withFallback(PekkoSpec.testConf))))

  def this(s: String) = this(ConfigFactory.parseString(s))

  def this(configMap: Map[String, _]) = this(PekkoSpec.mapToConfig(configMap))

  def this() = this(ActorSystem(PekkoSpec.testNameFromCallStack(classOf[PekkoSpec]), PekkoSpec.testConf))

  val log: LoggingAdapter = Logging(system, Logging.simpleName(this))

  override val invokeBeforeAllAndAfterAllEvenIfNoTestsAreExpected = true

  final override def beforeAll(): Unit = {
    // TODO not published startCoroner
    atStartup()
  }

  final override def afterAll(): Unit = {
    beforeTermination()
    shutdown()
    afterTermination()
    // TODO not published stopCoroner()
  }

  protected def atStartup(): Unit = {}

  protected def beforeTermination(): Unit = {}

  protected def afterTermination(): Unit = {}

  def spawn(dispatcherId: String = Dispatchers.DefaultDispatcherId)(body: => Unit): Unit =
    Future(body)(system.dispatchers.lookup(dispatcherId))

  def expectedTestDuration: FiniteDuration = 60.seconds

  def muteDeadLetters(messageClasses: Class[_]*)(sys: ActorSystem = system): Unit =
    if (!sys.log.isDebugEnabled) {
      def mute(clazz: Class[_]): Unit =
        sys.eventStream.publish(Mute(DeadLettersFilter(clazz)(occurrences = Int.MaxValue)))
      if (messageClasses.isEmpty) mute(classOf[AnyRef])
      else messageClasses.foreach(mute)
    }

  // for ScalaTest === compare of Class objects
  implicit def classEqualityConstraint[A, B]: CanEqual[Class[A], Class[B]] =
    new CanEqual[Class[A], Class[B]] {
      def areEqual(a: Class[A], b: Class[B]) = a == b
    }

  implicit def setEqualityConstraint[A, T <: Set[_ <: A]]: CanEqual[Set[A], T] =
    new CanEqual[Set[A], T] {
      def areEqual(a: Set[A], b: T) = a == b
    }
}
