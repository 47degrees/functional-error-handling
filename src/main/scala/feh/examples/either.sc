case class Nuke()

case class Target()

case class Impacted()

sealed trait NukeException
case class SystemOffline() extends NukeException
case class RotationNeedsOil() extends NukeException
case class MissedByMeters(meters : Int) extends NukeException

def arm: Either[SystemOffline, Nuke] = Right(Nuke())
def aim: Either[RotationNeedsOil,Target] = Right(Target())
def launch(target: Target, nuke: Nuke): Either[MissedByMeters, Impacted] = Left(MissedByMeters(5))

def attackImperative: Either[NukeException, Impacted] = {
  var result: Either[NukeException, Impacted] = null
  val eitherNuke = arm
  if (eitherNuke.isRight) {
    val eitherTarget = aim
    if (eitherTarget.isRight) {
      result = launch(eitherTarget.toOption.get, eitherNuke.toOption.get)
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
    nuke <- arm
    target <- aim
    impact <- launch(target, nuke)
  } yield impact

attackImperative

attackMonadic