package mathim.snippet

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.java.util.Date

import _root_.net.liftweb.http
import http._
import http.SHtml._
import http.js.JE._
import http.js.jquery.JqJsCmds._
import http.js.JsCmds._
import Helpers._

import net.liftweb.util.Mailer

class Snippets {
  
  def chatRoom(in: NodeSeq) : NodeSeq = S.param("channelName") match {
    case Full(channelName) => {
      val id = Helpers.nextFuncName // unique comet actor per page load
      <lift:comet type="ChatClientComet" name={ id }>
        <comet:message></comet:message>
      </lift:comet>
    }
    case _ => S.redirectTo("/") // must have accessed "/chatRoom/"
  }
  
  val EmailRE = """(?i)\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}\b""".r
  def mail(in: NodeSeq) : NodeSeq = {
    var from = "";
    var to = "";
    var subject = "";
    var htmlBody = "";
    
    def isEmailValid(email: String) =
      EmailRE.pattern.matcher(email).matches
    
    def processForm() = {
      var allOkay = true;
      
      if(!isEmailValid(from)) {
        val fromFormatted = if(from.isEmpty) "(blank)" else from
        S.error("From e-mail address invalid: %s".format(fromFormatted))
        allOkay = false
      }
      
      if(!isEmailValid(to)) {
        val toFormatted = if(to.isEmpty) "(blank)" else to
        S.error("To e-mail address invalid: %s".format(toFormatted))
        allOkay = false
      }
      
      if(subject.isEmpty && htmlBody.isEmpty) {
        S.error("Both the subject and email body are empty")
        allOkay = false
      }
      
      import Mailer._ 
      
      val totalBody = htmlBody + "<hr/>" +
        "Sent with <a href='http://mathim.com/'>MathIM</a>"
      
      val restArgs = 
        List(XHTMLMailBodyType(scala.xml.Unparsed(totalBody)), To(to)) ++ 
        (if(from==to) None else Some(BCC(from))).toList
        
      sendMail(From(from), Subject(subject), restArgs : _*) // expand arg list 
      
      
      if(allOkay) {
        S.notice("Mail sent to: %s.".format(to))
        S.notice("Check the \"From\" mailbox for a copy to verify.")
        Run("clearMailTo();")
      } else Noop
    }
    
    // We have two textareas assuming the mailTextBody will be transformed
    // and copied to mailHtmlBody by the javascript pre submission
    ajaxForm(
      <div class="mailForm">
        <p><b>From:</b><br/>
        {text("", from = _, "class"->"mailFrom")}
        </p>
        
        <p><b>To:</b><br/>
        {text("", to = _, "class"->"mailTo")}
        </p>
        
        <p><b>Subject:</b><br/>
        {text("", subject = _, "class"->"mailSubject")}
        </p>
        
        <p><b>Body:</b>
        <lift:embed what="texbar"></lift:embed>
        {textarea("", ignore=>() ,  "class"->"mailTextBody", 
          "rows"->"16")}
        {textarea("", htmlBody = _, "class"->"mailHtmlBody")}
        </p>
        
        <p>{ajaxSubmit("Send", processForm)}</p> 
      </div>
    )
  }
}
