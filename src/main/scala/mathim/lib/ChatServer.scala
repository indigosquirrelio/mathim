package mathim.lib

import java.util.Date

import net.liftweb._
import http._
import actor._
import util.Helpers._
import js._
import JsCmds._
import JE._

import java.util.Date
import net.liftweb.common.SimpleActor

class Channel { // It doesn't even need to know it's own name!
  var log : Vector[Message]
  var listeners: Set[SimpleActor]
  
  var nicks : Vector[String]
  
  def addListener(
}

object ChatServer extends LiftActor {
  var channels: HashMap[String, Channel]
  
  def getChannel(channelName: String) = channels.get(channelName) match {
    case Some(channel) => channel
    case _ => {
      // add channel to map
      val newChan = new Channel
      channels = channels + channelName->newChan
      newChan
    }
  }
    if(channels.contains(channelName)) channels(channelName)
  }
  
  override def lowPriority = {
    case Subscribe(listener, channelName) => {
      val chan = getChannel(channelName)
      chan.listeners = chan.listeners + listener
      listener ! ChannelLog(chan.log.takeRight(100))
      listener ! ChannelNicks(chan.nicks)
    }
    case Unsubscribe(listener, channelName, nickOpt) => {
      val chan = getChannel(channelName)
      
      nickOpt match {
        case Some(nick) if(chan.nicks.contains(nick)) => {
          chan.nicks = chan.nicks - nick
          chan.listeners.map(_ ! SysMessage(nick + " has left the channel."))
          chan.listeners.map(_ ! ChannelNicks(chan.nicks))
        }
        case _ => None
      }
      
      if(chan.listeners.isEmpty && chan.nicks.isEmpty)
        channels = channels - channelName
      
      chan.listeners = chan.listeners - listener
      
      if(chan.listeners.isEmpty)
        channels = channels - channelName
    }
    case RequestNick(listener, channelName, nick) => {
      val chan = getChannel(channelName)
      
      if(chan.nicks contains nick) {
        listener ! NickTaken(nick)
      } else {
        chan.nicks = chan.nicks + nick
        listener ! NickAssignment(nick)
        
        // announce new member
        chan.listeners.map(_ ! SysMessage(nick + " has joined the channel."))
        chan.listeners.map(_ ! ChannelNicks(chan.nicks))
      }
    }
    case message: Message => {
      val chan = getChannel(channelName)
      chan.log = chan.log :+ message
      chan.listeners.map(_ ! message)
    }
    case _ => {
      
    }
  }
}

case class Subscribe(listener: SimpleActor, channelName: String)
case class Unsubscribe(listener: SimpleActor, channelName: String, 
                       nickOpt: Option[String])

case class RequestNick(listener: SimpleActor, channelName: String, nick: String)

case class Message(channelName: String, nick: String, message: String, 
                   timestamp: Date = new Date())

case class SysMessage(message: String, timestamp: Date = new Date())

object NickTaken(nick: String)
object NickAssignment(nick: String)

case class ChannelLog(log: Vector[Message])
case class ChannelNicks(nicks: Vector[String])
