import feh.PlayListPresenter.View

package object feh {

  /** DataSource */
  class DataSource[I, T](database: Map[I, T]) {
    def get(id: I): Option[T] = database.get(id)
  }

  /** Repository */
  class Repository[I, T](dataSource: DataSource[I, T]) {
    def getWithIds(ids: I*): List[T] = ids.toList.flatMap(dataSource.get)
  }

  /** Model */
  type SongId = Int

  case class Song(title: String)

  class Playlist(cr: Repository[SongId, Song]) {
    def songs(songsIds: List[SongId]): List[Song] =
      cr.getWithIds(songsIds:_*)
  }

  /** UseCase */
  class PlaySongsUseCase(playlist: Playlist) {
    def execute(songsIds: List[SongId]): List[Song] =
      playlist.songs(songsIds)
  }

  /** Presenter */
  class PlayListPresenter(view: PlayListPresenter.View, useCase: PlaySongsUseCase) {
    def onUserRequestsPlayList(songsIds: List[SongId]): Unit = {
      val songs = useCase.execute(songsIds)
      view.showPlayList(songs)
    }
  }
  object PlayListPresenter {
    trait View {
      def showPlayList(songs: List[Song]): Unit
    }
  }

}

object Application {

  import feh._

  /** Application */
  def main(args: Array[String]): Unit = {
    val database = Map(
      1 -> Song("Song 1"),
      2 -> Song("Song 2"),
      //3 -> Song("Song 3"),
      4 -> Song("Song 4")
    )
    val dataSource = new DataSource[SongId, Song](database)
    val repository = new Repository[SongId, Song](dataSource)
    val playlist = new Playlist(repository)
    val useCase = new PlaySongsUseCase(playlist)
    val view = new View {
      override def showPlayList(songs: List[Song]): Unit = println(songs)
    }
    val presenter = new PlayListPresenter(view, useCase)
    presenter.onUserRequestsPlayList(List(1,2,3,4))
  }
}
