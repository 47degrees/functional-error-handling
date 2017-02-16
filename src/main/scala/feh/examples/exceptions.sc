import scala.util.control.NonFatal

case class Nuke()

case class Target()

case class Impacted()

def arm: Nuke = throw new RuntimeException("SystemOffline")

def aim: Target = throw new RuntimeException("RotationNeedsOil")

def launch(target: Target, nuke: Nuke): Impacted = Impacted()

def attackImperative: Impacted = {
  var impact: Impacted = null
  try {
    val nuke = arm
    val target = aim
    impact = launch(target, nuke)
  } catch {
    case NonFatal(e) => //???
  }
  impact
}

attackImperative //null
