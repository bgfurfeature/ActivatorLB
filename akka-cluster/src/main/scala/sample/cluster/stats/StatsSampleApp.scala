package sample.cluster.stats

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * Created by C.J.YOU on 2017/2/10.
  */
object StatsSampleApp {

    def main(args: Array[String]): Unit = {
      if (args.isEmpty) {
        startup(Seq("2551", "2552", "0"))
        StatsSampleClient.main(Array.empty)
      } else {
        startup(args)
      }
    }

    def startup(ports: Seq[String]): Unit = {
      ports foreach { port =>
        // Override the configuration of the port when specified as program argument
        val config =
          ConfigFactory.parseString(s"akka.remote.netty.tcp.port=" + port).withFallback(
            ConfigFactory.parseString("akka.cluster.roles = [compute]")).
            withFallback(ConfigFactory.load("stats1"))

        val system = ActorSystem("ClusterSystem", config)

        system.actorOf(Props[StatsWorker], name = "statsWorker")
        system.actorOf(Props[StatsService], name = "statsService")
      }
    }

}
