package mathim.comet

import net.liftweb._
import http._
import http.js._
import http.js.JE._
import http.js.jquery.JqJsCmds._
import http.js.JsCmds._
import http.SHtml._

import util.Helpers._
import util.ActorPing

import net.liftweb.actor._
import net.liftweb.common.{Box, Full, Loggable}

import scala.xml._
import scala.util.Random

import mathim.lib._

object KeepAlive

class ChatClientComet extends CometActor with Loggable {
  val channelName = S.param("channelName").get
  
  val server = ChatServer
  
  var nickOpt : Option[String] = None
  
  override def lifespan = Full(20 seconds)
  
  ActorPing.schedule(this, KeepAlive, 10000L)
  
  override def localSetup() = {
    server ! Subscribe(this, channelName)
  }
  
  override def localShutdown() = { 
    logger.info("localShutdown")
    server ! Unsubscribe(this, channelName, nickOpt)
  }
  
  override def lowPriority = {
    case NickTaken(nick) => {
      this.error("chatError", "Nick '" + nick + "' taken. Choose another.")
      reRender
    }
    case NickAssignment(nick) => {
      nickOpt = Some(nick)
      reRender
    }
    case ChannelLog(log) => {
      logger.info("ChannelLog received")
      if(!log.isEmpty)
        partialUpdate(log.reverse.map(m => jsCall(m, true)).reduceLeft(_+_))
      else
        Noop
    }
    case ChannelNicks(nicks) => {
      logger.info("ChannelNicks received")
      partialUpdate(SetHtml("chatUserlist", nicks.map(n => 
        <p class='message'>{n}</p>).toSeq))
    }
    case message: Message => {
      logger.info("Message received")
      partialUpdate(jsCall(message))
    }
    case KeepAlive => {
      ActorPing.schedule(this, KeepAlive, 10000L);
      partialUpdate(JsCmds.Noop)
    } 
    case x => 
      logger.error("StarGameComet unknown message: %s".format(x.toString))
  }
  
  def jsCall(message: Message, prepend: Boolean = false) : JsExp = { 
    message match {
      case msg: ChatMessage => 
        Call("chatMessage", msg.timestampShort, msg.nick, msg.message, prepend) 
      case msg: SysMessage =>
        Call("sysMessage", msg.timestampShort, msg.message, prepend)
    }
  }
  
  def showPanes(panes: List[String]) = {
    val cmd = "$('.pane').hide();" :: panes.map("$('#" + _ + "').show();") 
    OnLoad(JsRaw(cmd.mkString("\n")))
  }
  
  def render = {
    if(nickOpt.isDefined) {
      renderCompose & showPanes("chatInputCompose" :: Nil)
    } else {
      renderAskName & showPanes("chatInputAskName" :: Nil)
    } & Call("setChannelName", channelName)
  }
  
  def renderCompose = OnLoad(SetHtml("chatInputCompose", 
    S.runTemplate("templates-hidden" :: "chatCompose" :: Nil, 
      "composeTextarea" -> SHtml.onSubmit(msg => {
        logger.debug("Comet sent message " + msg)
        server ! ChatMessage(channelName, nickOpt.get, msg)
      })
    ) match {
      case Full(x) => x
      case _ => <p>Problem rendering compose</p>
    }
  ) & Call("initializeChatInput"))
  
  def renderAskName = OnLoad(SetHtml("chatInputAskName", nickOpt match {
    case Some(name) => <p>Nick registered</p>
    case x => {
      var nickChoice = "";
      
      def processForm() = {
        server ! RequestNick(this, channelName, nickChoice)
        Noop
      }
      
      ajaxForm(
        <div>
          Choose a nickname
          <br/>
          {text("", nickChoice = _, "class"->"askNameTextField")}
          {ajaxSubmit("Join", processForm)} 
        </div>
      )
    }
  }) & JsRaw("$('.askNameTextField').focus();"))
  
}

