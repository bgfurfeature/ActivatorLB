package sample.cluster.stats

import java.util.concurrent.ThreadLocalRandom

import akka.actor._
import akka.cluster.ClusterEvent._
import akka.cluster.{Cluster, MemberStatus}

import scala.concurrent.duration._

object StatsSampleClient {
  def main(args: Array[String]): Unit = {
    // note that client is not a compute node, role not defined
    val system = ActorSystem("ClusterSystem")
    system.actorOf(Props(classOf[StatsSampleClient], "/user/statsService"), "client")
  }
}

class StatsSampleClient(servicePath: String) extends Actor {

  val cluster = Cluster(context.system)
  val servicePathElements = servicePath match {
    case RelativeActorPath(elements) => elements   // judge path format right or not
    case _ => throw new IllegalArgumentException(
      "servicePath [%s] is not a valid relative actor path" format servicePath)
  }
  import context.dispatcher
  val tickTask = context.system.scheduler.schedule(2.seconds, 10.seconds, self, "tick")

  var nodes = Set.empty[Address]

  override def preStart(): Unit = {
    println("Enter SampleClient preStart")
    cluster.subscribe(self, classOf[MemberEvent], classOf[ReachabilityEvent])
  }
  override def postStop(): Unit = {
    cluster.unsubscribe(self)
    tickTask.cancel()
  }

  def receive = {
    case "tick" if nodes.nonEmpty =>
      // just pick any on
      val address = nodes.toIndexedSeq(ThreadLocalRandom.current.nextInt(nodes.size))
      println("Member address: " + address.toString)
      val service = context.actorSelection(RootActorPath(address) / servicePathElements)
      service ! StatsJob("this is the text that will be analyzed")
    case result: StatsResult =>
      println(result)
    case failed: JobFailed =>
      println(failed)
    case state: CurrentClusterState =>
      nodes = state.members.collect {
        case m if m.hasRole("compute") && m.status == MemberStatus.Up => m.address
      }
    case MemberUp(m) if m.hasRole("compute")        =>
      println("==> Enter Member Up :" + m.address.toString)
      nodes += m.address
    case other: MemberEvent                         =>
      println("==> Enter MemberEvent:" + other.member.address.toString)
      nodes -= other.member.address
      println("---> nodes:" + nodes.mkString(":"))
    case UnreachableMember(m)                       =>
      println("==> Enter UnreachableMember:" + m.address.toString)
      nodes -= m.address
    case ReachableMember(m) if m.hasRole("compute") =>
      println("==> Enter ReachableMember:" + m.address.toString)
      nodes += m.address
  }

}
