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
  var initialTextCleared = false;
  
  $('#composeTextarea').autoResize();
  
  $('#composeTextarea').keydown(function(e) {
    initialTextCleared = true;
      
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
  
  function updatePreview() {
    $('#previewArea').html(mathFilter($('#composeTextarea').val()));
  }
  
  $('#composeTextarea').keyup(function(e) {
    updatePreview();
  }).keyup(); // trigger to move input text over
  
  //$('#composeTextarea').focus().select();
  setTimeout("$('#composeTextarea').focus().select();", 50);
  
  $('img.texbar').hover(
    function(e) {
      $(this).css('{ background-color: #f5f5f5; }');
    },
    function(e) {
      $(this).css('{ background-color: #eeeeee; }');
    }
  ).click(
    function(e) {
      var textArea = $('#composeTextarea'); 
      
      if(!initialTextCleared) {
        textArea.val("");
        initialTextCleared = true;
      }
      
      var nDols = $('#composeTextarea').val().split("$").length - 1;
      
      var inLatex = nDols % 2 == 1;
      
      var toAppendStr = "";
      
      if(!inLatex) {
        var curStr = textArea.val();
        // add a space if our last character is not a space
        if(curStr != "" && curStr[curStr.length-1] != " ") {
          toAppendStr += " ";
        }
        toAppendStr += "$";
      }
      toAppendStr += $(this).attr('title');
      if(!inLatex) {
        toAppendStr += "$";
      }
      
      textArea.val(textArea.val() + toAppendStr);
      textArea.focus();
      updatePreview();
    }
  );
}

