import scala.util.Try

case class Nuke()

case class Target()

case class Impacted()

def arm: Try[Nuke] = Try(throw new RuntimeException("SystemOffline"))

def aim: Try[Target] = Try(throw new RuntimeException("RotationNeedsOil"))

def launch(target: Target, nuke: Nuke): Try[Impacted] = Try(throw new RuntimeException("MissedByMeters"))

def attackImperative: Try[Impacted] = {
  var impact: Try[Impacted] = null
  var ex: Throwable = null
  val tryNuke = arm
  if (tryNuke.isSuccess) {
    val tryTarget = aim
    if (tryTarget.isSuccess) {
      impact = launch(tryTarget.get, tryNuke.get)
    } else {
      ex = tryTarget.failed.get
    }
  } else {
    ex = tryNuke.failed.get
  }
  if (impact != null) impact else Try(throw ex)
}

def attackMonadic: Try[Impacted] =
  for {
    nuke <- arm
    target <- aim
    impact <- launch(target, nuke)
  } yield impact

attackImperative

attackMonadic