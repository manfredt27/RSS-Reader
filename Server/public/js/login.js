
var url = "http://proyecto2.cloudapp.net:8080/"

function login(){
	var username = $("#username")[0].value;
	var password = $("#password")[0].value;
	
	//Encriptar usando MD5 con librería CryptoJS https://code.google.com/p/crypto-js/
	var hashPassword = CryptoJS.MD5(password);
	var hashUsername = CryptoJS.MD5(username);
	

	if (username != "" && password != ""){
		var user = {'username': hashUsername, 'password': hashPassword};
		validar(user);
	}
	else{
		alert("Información incompleta");
	}
}



//Validar usuario
function validar(user){
	var v_url = url+"validateAdmin/";
	console.log(v_url);	
	var xhr = createCORSRequest('POST', v_url);
	if (!xhr) {
		throw new Error('CORS not supported');
	}
	
	xhr.setRequestHeader("Content-Type", "application/json");
	xhr.onload = function() {
		var responseText = xhr.responseText;
		alert(responseText);
		if (responseText == "Usuario validado" ){
			window.location.href = "/agregar.html";
		}
	};

	xhr.onerror = function() {
		console.log('There was an error!');
	};
	
	xhr.send(JSON.stringify(user));
	
}

function createCORSRequest(method, url) {
	var xhr = new XMLHttpRequest();
	
	if ("withCredentials" in xhr) {

		// Check if the XMLHttpRequest object has a "withCredentials" property.
		// "withCredentials" only exists on XMLHTTPRequest2 objects.
		xhr.open(method, url, true);

	} else if (typeof XDomainRequest != "undefined") {

		// Otherwise, check if XDomainRequest.
		// XDomainRequest only exists in IE, and is IE's way of making CORS requests.
		xhr = new XDomainRequest();
		xhr.open(method, url);

	} else {

		// Otherwise, CORS is not supported by the browser.
		xhr = null;

	}
	return xhr;
}