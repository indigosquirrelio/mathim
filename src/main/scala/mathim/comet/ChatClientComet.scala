package mathim.comet

import net.liftweb._
import http._
import http.js._
import http.js.JE._
import http.js.JqJsCmds._
import http.js.JsCmds._
import http.SHtml._

import net.liftweb.actor._
import net.liftweb.common.{Box, Full, Loggable}

import scala.xml._
import scala.util.Random

import mathim.lib._

class ChatClientComet extends CometActor with Loggable {
  val channelName = S.param("channelName").get
  
  val server = ChatServer
  
  var nickOpt : Option[String] = None
  
  override def localSetup() = {
    server ! Subscribe(this, channelName)
  }
  
  override def localShutdown() = {
    if(nickOpt.isDefined) server ! DeregisterNick(this, channelName) 
    server ! Unsubscribe(this, channelName)
  }
  
  override def lowPriority = {
    case NickTaken(nick) => {
      this.error("chatError", "Nick '" + nick + "' taken. Choose another.")
      reRender
    }
    case NickAssignment(nick) = {
      nickOpt = Some(nick)
      reRender
    }
    case ChannelLog(log) = {
      PrependHtml("chatLog", log.map(html).reduceLeft(_ ++ _))
    }
    case ChannelNicks(nicks) = {
      SetHtml("chatUserlist", nicks.map(n => <p>{n}</p>).reduceLeft(_ ++ _))
    }
    case message: Message = {
      AppendHtml("chatLog", html(message))
    }
    
    case x => 
      logger.error("StarGameComet unknown message: %s".format(x.toString))
  }
  
  def html(msg: Message) : NodeSeq= {
    <p>&lt;{msg.nick}&gt; {msg.message}</p>
  }
  
  def showPanes(panes: List[String]) = {
    val cmd = "$('.pane').hide();" :: panes.map("$('#" + _ + "').show();") 
    OnLoad(JsRaw(cmd.mkString("\n")))
  }
  
  def render = {
    if(nickOpt.isDefined) {
      renderCompose
      showPanes("chatInputCompose" :: Nil)
    } else {
      renderAskName
      showPanes("chatInputAskName" :: Nil)
    }
  }
  
  def renderCompose = OnLoad(SetHtml("chatInputCompose", 
    S.runTemplate("chatInput" :: Nil )
  
  def renderAskName = OnLoad(SetHtml("chatInputAskName", nickopt match {
    case Some(name) => ""
    case x => {
      var nickChoice = "";
      
      def processForm() = {
        server ! RequestNick(this, nickChoice)
      }
      
      ajaxForm(
        <div>
          "Choose a nickname"
          <br/>
          {text("", nickChoice = _)}
          {ajaxSubmit("Join", processForm)} 
        </div>
      )
    )
  }
}

object StarGameComet = {
  val chatInputXml = TemplateFinder.findAnytemplate("chatInput")
}
