case class Nuke()

case class Target()

case class Impacted()

sealed trait NukeException
case class SystemOffline() extends NukeException
case class RotationNeedsOil() extends NukeException
case class MissedByMeters(meters : Int) extends NukeException

def armNukes: Either[SystemOffline, Nuke] = Right(Nuke())
def aim: Either[RotationNeedsOil,Target] = Right(Target())
def launchNukes(target: Target, nuke: Nuke): Either[MissedByMeters, Impacted] = Left(MissedByMeters(5))

def attackImperative: Either[NukeException, Impacted] = {
  var result: Either[NukeException, Impacted] = null
  val eitherNuke = armNukes
  if (eitherNuke.isRight) {
    val eitherTarget = aim
    if (eitherTarget.isRight) {
      result = launchNukes(eitherTarget.toOption.get, eitherNuke.toOption.get)
    } else {
      result = Left(RotationNeedsOil())
    }
  } else {
    result = Left(SystemOffline())
  }
  result
}

def attackMonadic: Either[NukeException, Impacted] =
  for {
    nuke <- armNukes
    target <- aim
    impact <- launchNukes(target, nuke)
  } yield impact

attackImperative

attackMonadic