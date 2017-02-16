case class Nuke()

case class Target()

case class Impacted()

def arm: Option[Nuke] = None

def aim: Option[Target] = None

def launch(target: Target, nuke: Nuke): Option[Impacted] = Some(Impacted())

def attackImperative: Option[Impacted] = {
  var impact: Option[Impacted] = None
  val optionNuke = arm
  if (optionNuke.isDefined) {
    val optionTarget = aim
    if (optionTarget.isDefined) {
      impact = launch(optionTarget.get, optionNuke.get)
    }
  }
  impact
}

def attackMonadic: Option[Impacted] =
  for {
    nuke <- arm
    target <- aim
    impact <- launch(target, nuke)
  } yield impact

attackImperative

attackMonadic