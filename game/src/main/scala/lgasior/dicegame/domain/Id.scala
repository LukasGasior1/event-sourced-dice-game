package lgasior.dicegame.domain

trait Id[T] extends Any {
  def value: String
}
