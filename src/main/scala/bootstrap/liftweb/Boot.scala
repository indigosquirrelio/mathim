package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._

import common._
import http._
import sitemap._
import Loc._


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    // where to search snippet
    LiftRules.addToPackages("mathim")

    // Build SiteMap
    val entries = List(
      Menu.i("Home") / "index",
      Menu(Loc("Static", Link(List("static"), true, "/static/index"), 
	       "Static Content")),
      Menu("Chatroom") / "chatroom" / "index",
      Menu("Quickie") / "quickie" / "index",
      Menu("Mail") / "mail" / "index"
    )
    
    val realFolders = List("static", "chatroom", "quickie", "mail")
    
    // redirect all "/channelName" to "/chatroom/" except real folders
    val rewrites = NamedPF[RewriteRequest, RewriteResponse]("Chatrooms") {
      case RewriteRequest(ParsePath(channelName :: Nil, _, _, _), _, _) 
        if channelName != "index" && !realFolders.contains(channelName) =>
          RewriteResponse(
            "chatroom" :: "index" :: Nil, Map("channelName"->channelName))
    }
    
    LiftRules.statelessRewrite.prepend(rewrites)

    // redirect all "/channelName/" to "/channelName"
    // redirect all 'real folders' i.e. 'mail' to 'mail/'    
    LiftRules.statelessDispatchTable.prepend({
      case Req(folderName :: "index" :: Nil, _,_) 
        if !realFolders.contains(folderName) => 
          () => Full(RedirectResponse("/"+folderName))
      case Req(folderName :: Nil, _,_) 
        if realFolders.contains(folderName) => 
          () => Full(RedirectResponse("/"+folderName+"/"))
    })    

    LiftRules.setSiteMap(SiteMap(entries:_*))

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

  }
}
