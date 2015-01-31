
var url = "http://proyecto2.cloudapp.net:8080/"

function agregarLink(){
	var name = $("#name")[0].value;
	var cat = $("#category")[0].value;
	var link = $("#url")[0].value;
	var page = $("#page")[0].value;
	var lang = $("#language")[0].value;
	
	if (name != "" && cat != "" && link != "" && page != "" && lang != ""){
		var feed = {'url': link, 'urlpagina': page, 'nombre': name, 'categoria': cat, 'idioma': lang};
		guardarFeed(feed);
	}
	else{
		alert("Información incompleta");
	}
	
}


//Guardar link en DB
function guardarFeed(feed){
	var v_url = url+"guardarfeed/";
	console.log(v_url);	
	var xhr = createCORSRequest('POST', v_url);
	if (!xhr) {
		throw new Error('CORS not supported');
	}
	
	xhr.setRequestHeader("Content-Type", "application/json");
	xhr.onload = function() {
		var responseText = xhr.responseText;
		alert(responseText);
	};

	xhr.onerror = function() {
		console.log('There was an error!');
	};
	
	xhr.send(JSON.stringify(feed));
	
}


function agregarRecomendacion(){
	var name = $("#r_name")[0].value;
	var cat = $("#r_category")[0].value;
	var link = $("#r_url")[0].value;
	var page = $("#r_page")[0].value;
	var lang = $("#r_language")[0].value;
	
	if (name != "" && cat != "" && link != "" && page != "" && lang != ""){
		var feed = {'url': link, 'urlpagina': page, 'nombre': name, 'categoria': cat, 'idioma': lang};
		guardarRecomendacion(feed);
	}
	else{
		alert("Información incompleta");
	}
	
}


//Guardar recomendacion en DB
function guardarRecomendacion(feed){
	var v_url = url+"guardarrecomendacion/";
	console.log(v_url);	
	var xhr = createCORSRequest('POST', v_url);
	if (!xhr) {
		throw new Error('CORS not supported');
	}
	
	xhr.setRequestHeader("Content-Type", "application/json");
	xhr.onload = function() {
		var responseText = xhr.responseText;
		alert(responseText);
	};

	xhr.onerror = function() {
		console.log('There was an error!');
	};
	
	xhr.send(JSON.stringify(feed));
	
}


function agregarVideo(){
	var cat = $("#v_category")[0].value;
	var link = $("#v_url")[0].value;
	
	var v_id = link.substring(link.indexOf('watch?v=')+8);
	
	if (cat != "" && link != ""){
		var video = {'video_id': v_id, 'categoria': cat};
		guardarVideo(video);
	}
	else{
		alert("Información incompleta");
	}
	
}


//Guardar link en DB
function guardarVideo(video){
	var v_url = url+"guardarvideo/";
	console.log(v_url);	
	var xhr = createCORSRequest('POST', v_url);
	if (!xhr) {
		throw new Error('CORS not supported');
	}
	
	xhr.setRequestHeader("Content-Type", "application/json");
	xhr.onload = function() {
		var responseText = xhr.responseText;
		alert(responseText);
	};

	xhr.onerror = function() {
		console.log('There was an error!');
	};
	
	xhr.send(JSON.stringify(video));
	
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