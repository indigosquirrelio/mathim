function initTexbar(dollarSigns, textareaSelector) {
  $('img.texbar').hover(
    function(e) {
      $(this).css('{ background-color: #f5f5f5; }');
    },
    function(e) {
      $(this).css('{ background-color: #eeeeee; }');
    }
  ).click(
    function(e) {
      var textArea = $(textareaSelector);
      
      var nDols = textArea.val().split("$").length - 1;
      
      var inLatex = nDols % 2 == 1;
      
      var toAppendStr = "";
      
      if(!inLatex) {
        var curStr = textArea.val();
        // add a space if our last character is not a space
        if(curStr != "" && curStr[curStr.length-1] != " ") {
          toAppendStr += " ";
        }
        if(dollarSigns) {
          toAppendStr += "$";
        }
      }
      toAppendStr += $(this).attr('title');
      if(!inLatex && dollarSigns) {
        toAppendStr += "$";
      }
      
      textArea.val(textArea.val() + toAppendStr);
      textArea.focus().keyup();
    }
  );
};
