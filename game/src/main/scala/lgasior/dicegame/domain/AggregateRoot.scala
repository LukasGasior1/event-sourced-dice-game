package lgasior.dicegame.domain

trait AggregateRoot[T <: AggregateRoot[T, E], E] {
  self: T =>

  def id: Id[T]

  def uncommittedEvents: List[E]

  def applyEvents(events: E*): T =
    events.foldLeft(this)(_ applyEvent _)

  def applyEvent: PartialFunction[E, T]

  def markCommitted: T
}
