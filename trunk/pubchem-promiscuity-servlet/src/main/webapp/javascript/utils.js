function getXML(){
	var locateS = window.location.toString();
	theleft = locateS.indexOf("=") + 1;
	var fileS = locateS.substring(theleft, locateS.length);
	return(fileS);
}

function getUrlParams(){
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}

function getParams(url){
    var vars = [], hash;
    var hashes = url.slice(url.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}

function getServiceUrl(url){
    var urlSplit = url.split('?');
    var serviceUrl = urlSplit[0];
    return serviceUrl;
}

function getHiddenFields(params){
    var fields = [];
    for(var ii=0; ii < params.length; ii++){
        var param = params[ii];
        var value = params[param];
        fields.push({value: value, name: param, title: param, type:"hidden"});
   }
   return fields
}

function inflate(compresed_value){
    var b64decode = base64decode(compresed_value);
    var inflated = RawDeflate.inflate(b64decode);
    return inflated;
}

function getCookie(c_name){
	var i,x,y,ARRcookies=document.cookie.split(";");
	for (i=0;i<ARRcookies.length;i++){
		x=ARRcookies[i].substr(0,ARRcookies[i].indexOf("="));
		y=ARRcookies[i].substr(ARRcookies[i].indexOf("=")+1);
		x=x.replace(/^\s+|\s+$/g,"");
		if (x==c_name){
			return unescape(y);
		}
	}
}

function setCookie(c_name,value,exdays){
	var exdate=new Date();
	exdate.setDate(exdate.getDate() + exdays);
	var c_value=escape(value) + ((exdays==null) ? "" : "; expires="+exdate.toUTCString());
	document.cookie=c_name + "=" + c_value;
}

function checkEmailCookie(){
	var email = getCookie("pcpemail");
	if(email != null && email != ""){
		var emailElement = document.getElementsByName("email");
		emailElement.value = email;
	}		  
}

function formatNumber(value){
	if(value != null && value != ""){		
		var num = Number(value);
		num.toFixed(3);
		return num.toString();
	}
	return value;
	
}

