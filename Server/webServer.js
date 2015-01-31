var MongoClient = require('mongodb').MongoClient
	, ObjectID = require('mongodb').ObjectID
    , format = require('util').format;

var http = require('http'),
	express = require('express'),
	path = require('path');
	fs = require('fs');

var uri = 'mongodb://admin:Proyecto2@ds043947.mongolab.com:43947/proyecto2'
	
var app = express();
app.set('port', process.env.PORT || 8080);
app.set('view engine', 'jade');
app.use(express.static(path.join(__dirname, 'public')));



app.use(express.json());       // to support JSON-encoded bodies
app.use(express.urlencoded()); // to support URL-encoded bodies


//Web service post feed
app.post('/guardarfeed', function (req, res) {
	//gets feed from request {name, category, URL, webpage, language}
	var feed = req.body;
	postFeed(feed, function(results){
	res.send(results);
	})
});


//Post feed in DB
function postFeed(feed, fn){
	MongoClient.connect(uri, function(err, db) {
	
		if(err) throw err;
		
		var collection = db.collection('feed');
		//inserts feed in DB
		collection.insert(feed, function(err, docs) {
			if (err) fn("Ha ocurrido un error: " + err);
			collection.count(function(err, count) {
				//writes log and responds to user
				fn("El feed " + feed.nombre + " ha sido creado satisfactoriamente.");
				console.log(format("New feed inserted. count = %s", count));
				db.close();
			});
		});
		
	});
}


//Web service post recommendation
app.post('/guardarrecomendacion', function (req, res) {
	//gets feed from request {name, category, URL, webpage, language}
	var feed = req.body;
	postRecommendation(feed, function(results){
	res.send(results);
	})
});


//Post recommendation en DB
function postRecommendation(feed, fn){
	MongoClient.connect(uri, function(err, db) {
	
		if(err) throw err;
		
		var collection = db.collection('recommendation');
		//inserts recommendation in DB
		collection.insert(feed, function(err, docs) {
			if (err) fn("Ha ocurrido un error: " + err);
			collection.count(function(err, count) {
				//writes log and responds to user
				fn("La recomendación " + feed.nombre + " ha sido creada satisfactoriamente.");
				console.log(format("New recommendation inserted. count = %s", count));
				db.close();
			});
		});
		
	});
}


//Web service post video
app.post('/guardarvideo', function (req, res) {
	//get video from request {URL, category}
	var video = req.body;
	postVideo(video, function(results){
	res.send(results);
	})
});


//Post video in DB
function postVideo(video, fn){
	MongoClient.connect(uri, function(err, db) {
	
		if(err) throw err;
		
		var collection = db.collection('video');
		//inserts video in DB
		collection.insert(video, function(err, docs) {
			if (err) fn("Ha ocurrido un error: " + err);
			collection.count(function(err, count) {
				//writes log and responds to user
				fn("El video " + video.video_id + " ha sido agregado satisfactoriamente.");
				console.log(format("New video inserted. count = %s", count));
				db.close();
			});
		});
		
	});
}



//Web service validate user
app.post('/validateAdmin', function (req, res) {
	//get user from request {username, password}
	var user = req.body;
	validateAdminUser(user, function(results){
	res.send(results);
	})
});



//validate admin user
function validateAdminUser(user, fn){
	MongoClient.connect(uri, function(err, db) {
		if(err) throw err;
		
		var collection = db.collection('adminUser');
		//search users credentials in DB
		collection.findOne(user,function(err, result) {
			if (result){
				//If credentials are valid, log in
				fn("Usuario validado");
				console.log(format("User %s logged in", result.username));
			}
			else{
				//Invalid credentials
				console.log("Login refused to " + user.username);
				fn("Credenciales inválidos")
			}
			db.close();
		});
		
	});
}


http.createServer(app).listen(app.get('port'), function(){
  console.log('Express server listening on port ' + app.get('port'));
});