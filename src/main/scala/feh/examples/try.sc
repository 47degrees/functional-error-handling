import scala.util.Try

case class Nuke()

case class Target()

case class Impacted()

def armNukes: Try[Nuke] = Try(throw new RuntimeException("SystemOffline"))

def aim: Try[Target] = Try(throw new RuntimeException("RotationNeedsOil"))

def launchNukes(target: Target, nuke: Nuke): Try[Impacted] = Try(throw new RuntimeException("MissedByMeters"))

def attackImperative: Try[Impacted] = {
  var impact: Try[Impacted] = null
  var ex: Throwable = null
  val tryNuke = armNukes
  if (tryNuke.isSuccess) {
    val tryTarget = aim
    if (tryTarget.isSuccess) {
      impact = launchNukes(tryTarget.get, tryNuke.get)
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
    nuke <- armNukes
    target <- aim
    impact <- launchNukes(target, nuke)
  } yield impact

attackImperative

attackMonadic