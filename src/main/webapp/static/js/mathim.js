// initialize sound
soundManager.url = '/static/swf/soundmanager2.swf';
soundManager.useFlashBlock = false;

soundManager.onready(function() {
  soundManager.createSound({
    id: 'notify',
    url: '/static/snd/sound_1.mp3'
  });
});

var pageTitle = "MathIM: LaTeX web-based chat";
function setChannelName(cN) {
  pageTitle = "MathIM: " + cN;
  $('#channelName').text(cN);
}

function addToLog(html, prepend) {
  if(prepend) {
    $('#chatLog').prepend(html);
  } else {
    $('#chatLog').append(html);
  }
  
  $('#chatLog').scrollTop($('#chatLog')[0].scrollHeight);
  
  if(soundManager && soundManager.ok()) {
    soundManager.play('notify');
  }
}

var timestampHidden = false;
function timestampSpan() {
  if (timestampHidden) {
    return "<span class='timestamp' style='display:none;'>";
  } else {
    return "<span class='timestamp'>";
  }
}

function sysMessage(timestamp, message, prepend) {
  var html = "<p class='message sysMessage'>" +
             timestampSpan() + timestamp + "</span> " +
             "* " + message + "</p>\n";
  
  addToLog(html, prepend);
}

function chatMessage(timestamp, nick, message, prepend) {
  var mathedMsg = mathFilter(message);
  var html = "<p class='message chatMessage'>" +
             timestampSpan() + timestamp + "</span> " +
             "&lt;" + nick + "&gt; " + mathedMsg + "</p>\n";
  
  addToLog(html, prepend);
}

function initializeTopButtons() {
  $('#btnTimestamps').click(function() {
    $('.timestamp').toggle();
    timestampHidden = !timestampHidden;
  });
  
  var userlistHidden = false;
  $('#btnUserlist').click(function() {
    if(userlistHidden) {
      $('#chatUserlist').show();
      $('#chatLog').css("margin-right", 120);
    } else {
      $('#chatUserlist').hide();
      $('#chatLog').css("margin-right", 0);
    }
    userlistHidden = !userlistHidden;
  });
}

function initializeChatInput() { 
  $('#composeTextarea').autoResize();
  
  $('#composeTextarea').keydown(function(e) {
    if(e.which == KeyEvent.DOM_VK_RETURN && !e.shiftKey) {
      if($('#composeTextarea').val() != "") {
        $('#composeSubmitBtn').click();
      }
      return false;
    }
  });
  
  $('#composeSubmitBtn').click(function() {
    setTimeout("$('#composeTextarea').val('').focus();", 10);
  });
  
  $('#composeTextarea').keyup(function(e) {
    $('#previewArea').html(mathFilter($(this).val()));
  }).keyup(); // trigger to move input text over
  
  //$('#composeTextarea').focus().select();
  setTimeout("$('#composeTextarea').focus().select();", 50);
}

reDoubleDol = new RegExp('\\\$\\\$', 'mg');

// define mathFilter
function mathFilter(msg)
{
	// Process double dollar signs, then tokenize rest of string by dollar
  var msgary = msg.replace(reDoubleDol, '&#36;').split('$');
        
  var newmsg = "";
  // iterate through
  for(var i = 0; i < msgary.length; ++i)
  {
    // even elements are not latex
    if(i%2 == 0)
    {
      newmsg = newmsg + msgary[i];
    }
    else
    {
      // escape single quotes with slash since embedding it into html
      var tex = msgary[i].replace(/'/g,'&#39');
      var imgTag = "<img src='http://render.mathim.com/" + encodeURI(tex) + 
                   "' alt='" + tex + "' title='" + tex + 
                   "' class='tex' />";
      var linkedImg = 
        "<a href='http://mathim.com/s/#" + encodeURI(tex) + "'>" +
        imgTag + "</a>";
                   
      newmsg = newmsg + linkedImg;
    }
  }

	return newmsg;
};
