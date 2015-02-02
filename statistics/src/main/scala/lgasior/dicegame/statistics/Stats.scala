package lgasior.dicegame.statistics

case class Stats(rollsCounts: Map[Int, Int]) {

  def incRollsCount(rolledNumber: Int) = {
    val currentCount = rollsCounts.getOrElse(rolledNumber, 0)
    copy(rollsCounts = rollsCounts + (rolledNumber -> (currentCount + 1)))
  }

}
