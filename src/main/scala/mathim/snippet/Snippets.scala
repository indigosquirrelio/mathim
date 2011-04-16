package mathim.snippet

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.java.util.Date

import _root_.net.liftweb.http._
import Helpers._

class Snippets {
  
  def chatRoom(in: NodeSeq) : NodeSeq = S.param("channelName") match {
    case Full(channelName) => {
      val id = Helpers.nextFuncName // unique comet actor per page load
      <lift:comet type="ChatClientActor" name={ id }>
        <comet:message />
      </lift:comet>
    }
    case _ => S.redirectTo("/") // must have accessed "/chatRoom/"
  }
  
}
