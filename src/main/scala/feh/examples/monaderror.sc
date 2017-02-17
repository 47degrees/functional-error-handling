import cats._
import cats.implicits._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

case class Nuke()

case class Target()

case class Impacted()

sealed trait NukeException

case class SystemOffline() extends NukeException

case class RotationNeedsOil() extends NukeException

case class MissedByMeters(meters: Int) extends NukeException

type NukeMonadError[M[_]] = MonadError[M, NukeException]

def arm[M[_] : NukeMonadError]: M[Nuke] = Nuke().pure[M]

def aim[M[_] : NukeMonadError]: M[Target] = Target().pure[M]

def launch[M[_] : NukeMonadError](target: Target, nuke: Nuke): M[Impacted] =
  (MissedByMeters(5000): NukeException).raiseError[M, Impacted]

def attack[M[_] : NukeMonadError]: M[Impacted] =
  (aim[M] |@| arm[M]).tupled.flatMap((launch[M] _).tupled)

attack[Either[NukeException, ?]]


type Result[A] = Future[Either[NukeException, A]]

implicit val resultMonadError: MonadError[Result, NukeException] =
  new MonadError[Result, NukeException] {
    override def raiseError[A](e: NukeException): Result[A] = Future.successful(Left(e))

    override def handleErrorWith[A](fa: Result[A])(f: (NukeException) => Result[A]): Result[A] =
      fa flatMap {
        case Left(nukeException) => f(nukeException)
        case Right(value) => pure(value)
      }

    override def pure[A](x: A): Result[A] = Future.successful(Right(x))

    override def flatMap[A, B](fa: Result[A])(f: (A) => Result[B]): Result[B] =
      fa flatMap {
        case Left(nukeException) => raiseError(nukeException)
        case Right(value) => f(value)
      }

    override def tailRecM[A, B](a: A)(f: (A) => Result[Either[A, B]]): Result[B] = {
      f(a).flatMap {
        case Left(ex) => raiseError(ex)
        case Right(c) => c match {
          case Left(cont) => tailRecM(cont)(f)
          case Right(a) => pure(a)
        }
      }
    }

    override def ap[A, B](ff: Result[(A) => B])(fa: Result[A]): Result[B] =
      fa.zip(ff).map { case (a, f) =>
        for {
          fr <- f
          ar <- a
        } yield fr(ar)
      }

  }

val parallelizableAttack = attack[Result]
val x = Await.result(parallelizableAttack, 10.seconds)
