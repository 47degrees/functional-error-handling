import feh.PlayListPresenter.View
import fs2.Task

import scala.util.Try

package object feh {

  type Result[A] = List[Either[PlayListError, A]]

  /** DataSource */
  class DataSource[I, T](database: Map[I, T]) {
    def get(id: I): Either[Throwable, T] = Try(database(id)).toEither
  }

  /** Repository */
  class Repository[I, T](dataSource: DataSource[I, T]) {
    def getWithIds(ids: I*): List[Either[Throwable, T]] =
      ids.toList.map(dataSource.get)
  }

  /** Model */
  type SongId = Int

  sealed trait PlayListError
  case class SongNotFound(id: SongId) extends PlayListError
  case class SubscriptionRequired(song: Song) extends PlayListError
  case class UnknownDataSourceError(e : Throwable) extends PlayListError

  case class Song(title: String, requiresSubscription: Boolean = false)

  class Playlist(cr: Repository[SongId, Song]) {
    def songs(songsIds: List[SongId]): Result[Song] =
      for {
        id <- songsIds
        eitherSong <- cr.getWithIds(id)
      } yield eitherSong match {
        case Right(s) => if (s.requiresSubscription) Left(SubscriptionRequired(s)) else Right(s)
        case Left(e : NoSuchElementException) => Left(SongNotFound(id))
        case Left(e : Throwable) => Left(UnknownDataSourceError(e))
      }
  }

  /** UseCase */
  class PlaySongsUseCase(playlist: Playlist) {
    def execute(songsIds: List[SongId]): Result[Song] =
      playlist.songs(songsIds)
  }

  /** Presenter */
  class PlayListPresenter(view: PlayListPresenter.View, useCase: PlaySongsUseCase) {
    def onUserRequestsPlayList(songsIds: List[SongId]): Task[Unit] = Task.delay {
      val songs = useCase.execute(songsIds)
      val found = songs.collect {
        case Right(s) => s
      }
      val errors = songs.collect {
        case Left(e) => e
      }
      view.showPlayList(found)
      view.showErrors(errors)
      view.callToUnprincipledJavaLib()
    }
  }
  object PlayListPresenter {
    trait View {
      def showPlayList(songs: List[Song]): Unit
      def showErrors(errors: List[PlayListError]): Unit
      def callToUnprincipledJavaLib(): Unit = throw new RuntimeException("Boom!")
    }
  }

}

object Application {

  import feh._

  /** Application */
  def main(args: Array[String]): Unit = {
    val database = Map(
      1 -> Song("Song 1"),
      2 -> Song("Song 2", requiresSubscription = true),
      //3 -> Song("Song 3"),
      4 -> Song("Song 4")
    )
    val dataSource = new DataSource[SongId, Song](database)
    val repository = new Repository[SongId, Song](dataSource)
    val playlist = new Playlist(repository)
    val useCase = new PlaySongsUseCase(playlist)
    val view = new View {
      override def showPlayList(songs: List[Song]): Unit = songs.foreach(println)
      override def showErrors(errors: List[PlayListError]): Unit = errors.foreach(println)
    }
    val presenter = new PlayListPresenter(view, useCase)
    presenter
      .onUserRequestsPlayList(List(1,2,3,4))
      .unsafeAttemptRun()
      .left
      .foreach(println)
  }
}
