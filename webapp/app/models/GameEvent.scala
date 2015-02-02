package models

import play.api.libs.json.JsValue

case class GameEvent(gameId: String, eventType: String, data: JsValue)
