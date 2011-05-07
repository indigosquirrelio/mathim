package mathim.lib

import java.util.Date

import net.liftweb._
import http._
import actor._
import util.Helpers._
import js._
import JsCmds._
import JE._

import java.io._

import java.util.Date
import net.liftweb.common.SimpleActor

import collection.immutable.HashMap

class Channel(val name: String) {
  var log : Vector[Message] = Vector.empty
  var listeners: Set[SimpleActor[Any]] = Set.empty
  
  var nicks : Vector[String] = Vector.empty
  
  def writeLogFile() = if(!log.isEmpty) {
    val logStrings = log.map(_.toString)
    val fileName = "%s - %s.log".format(log.head.timestampLong, name)
    val filePath = "/var/log/mathim/channels/" + fileName
    
    try {
      val out = new BufferedWriter(new FileWriter(filePath));
      logStrings.foreach(s => out.write(s + "\n"))
      out.close();
    } catch {
      case _ => Unit
    }
  }
}

object ChatServer extends LiftActor {
  var channels: HashMap[String, Channel] = new HashMap()
  
  def getChannel(channelName: String) = channels.get(channelName) match {
    case Some(channel) => channel
    case _ => {
      // add channel to map
      val newChan = new Channel(channelName)
      channels = channels + (channelName->newChan)
      newChan
    }
  }
  
  override def messageHandler = {
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
          chan.nicks = chan.nicks.filter(_ != nick)
          val leaveMsg = SysMessage(nick + " has left the channel.")
          chan.log = chan.log :+ leaveMsg
          chan.listeners.map(_ ! leaveMsg)
          chan.listeners.map(_ ! ChannelNicks(chan.nicks))
        }
        case _ => None
      }
      
      chan.listeners = chan.listeners - listener
      
      if(chan.listeners.isEmpty) {
        chan.writeLogFile()
        channels = channels - channelName
      }
    }
    case RequestNick(listener, channelName, nick) => {
      val chan = getChannel(channelName)
      
      if(chan.nicks contains nick) {
        listener ! NickTaken(nick)
      } else {
        chan.nicks = chan.nicks :+ nick
        listener ! NickAssignment(nick)
        
        // announce new member
        val joinMsg = SysMessage(nick + " has joined the channel.")
        chan.log = chan.log :+ joinMsg
        chan.listeners.map(_ ! joinMsg)
        chan.listeners.map(_ ! ChannelNicks(chan.nicks))
      }
    }
    case message: ChatMessage => {
      val chan = getChannel(message.channelName)
      chan.log = chan.log :+ message
      chan.listeners.map(_ ! message)
    }
  }
}

case class Subscribe(listener: SimpleActor[Any], channelName: String)
case class Unsubscribe(listener: SimpleActor[Any], channelName: String, 
                       nickOpt: Option[String])

case class RequestNick(listener: SimpleActor[Any], channelName: String, 
                       nick: String)

trait Message {
  val message: String
  val timestamp: Date
  def toString: String
  
  def timestampShort = Message.shortFormat.format(timestamp)
  def timestampLong = Message.longFormat.format(timestamp)
}

object Message {
  val shortFormat = new java.text.SimpleDateFormat("HH:mm:ss")
  val longFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")
}
                       
case class ChatMessage(
  channelName: String, nick: String, message: String, 
  timestamp: Date = new Date()) 
  extends Message
{
  override def toString = "%s <%s> %s".format(timestampLong, nick, message)
}

case class SysMessage(
  message: String, timestamp: Date = new Date()) 
  extends Message
{
  override def toString = "%s * %s".format(timestampLong, message)
}

case class NickTaken(nick: String)
case class NickAssignment(nick: String)

case class ChannelLog(log: Vector[Message])
case class ChannelNicks(nicks: Vector[String])
