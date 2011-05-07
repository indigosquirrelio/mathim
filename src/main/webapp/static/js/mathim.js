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
}
