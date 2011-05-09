function convTextToHtml(input) {
  return  mathFilter(input).replace(/\n/g,"<br />");
}

function clearMailTo() {
  $('.mailTo').val('');
}

$(document).ready(function() {
  $("textarea.mailHtmlBody").hide();
  
  function updatePreview() {
    var input = $('textarea.mailTextBody').val();
    var html = convTextToHtml(input);
    $('#previewArea').html(html);
    $('textarea.mailHtmlBody').val(html);
    location.hash = encodeURI(input);
  }
  
  $('textarea.mailTextBody').keyup(function(e) {
    updatePreview();
  }).keyup();
  
  
  setTimeout("$('input.mailFrom').focus().select();", 50);
    
  initTexbar(true, 'textarea.mailTextBody');
  
});
