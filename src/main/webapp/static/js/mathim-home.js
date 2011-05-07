$(document).ready(function() {
  var defaultRoom = "Lobby";
  
  $("#fm-roomname").val(defaultRoom);
  
  $("#fm-chat").submit(function() {
    rn = $('#fm-roomname').val();
    if(rn == "") {
      rn = defaultRoom;
    }
    
    location.href = "/" + encodeURI(rn);
    return false;
  });
  
  setTimeout('$("#fm-roomname").focus().select();', 50);
});
