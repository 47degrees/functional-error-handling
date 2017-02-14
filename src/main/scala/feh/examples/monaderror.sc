

import cats._
import cats.implicits._

import scala.util.Try

case class Nuke()

case class Target()

case class Impacted()

sealed trait NukeException

case class SystemOffline() extends NukeException

case class RotationNeedsOil() extends NukeException

case class MissedByMeters(meters: Int) extends NukeException

type NukeMonadError[M[_]] = MonadError[M, NukeException]

def armNukes[M[_] : NukeMonadError]: M[Nuke] = Nuke().pure[M]

def aim[M[_] : NukeMonadError]: M[Target] = Target().pure[M]

def launchNukes[M[_] : NukeMonadError](target: Target, nuke: Nuke): M[Impacted] =
  (MissedByMeters(5) : NukeException).raiseError[M, Impacted]

def attack[M[_] : NukeMonadError]: M[Impacted] = {
  (aim[M] |@| armNukes[M]).tupled.flatMap((launchNukes[M] _).tupled)
}

attack[Either[NukeException, ?]]


