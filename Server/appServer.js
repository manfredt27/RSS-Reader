var MongoClient = require('mongodb').MongoClient
	, ObjectID = require('mongodb').ObjectID
    , format = require('util').format;

var http = require('http'),
	express = require('express'),
	path = require('path');
	fs = require('fs');

var uri = 'mongodb://admin:Proyecto2@ds043947.mongolab.com:43947/proyecto2'
	
var app = express();
app.set('port', process.env.PORT || 3000);
app.set('view engine', 'jade');



app.use(express.json());       // to support JSON-encoded bodies
app.use(express.urlencoded()); // to support URL-encoded bodies


//Web service get feeds
app.get('/feeds', function (req, res) {
	getFeeds(function(results){
	res.send({'source': results});
	})
});


//Get feeds from DB
function getFeeds(fn){
	MongoClient.connect(uri, function(err, db) {
	
		if(err) throw err;
		
		//gets all available feeds from DB
		var collection = db.collection('feed');
		collection.find().toArray(function(err, results) {
			//sends feeds to user
			fn(results);
			db.close();
		});
		
	});
}


//Web service get editor recommendations
app.get('/recommendations', function (req, res) {
	getRecommendations(function(results){
	res.send({'source': results});
	})
});


//Get recommendations from DB
function getRecommendations(fn){
	MongoClient.connect(uri, function(err, db) {
	
		if(err) throw err;
		
		//gets all available recommendations
		var collection = db.collection('recommendation');
		collection.find().toArray(function(err, results) {
			//sends recommendations to user
			fn(results);
			db.close();
		});
		
	});
}


//Web service get videos
app.get('/videos', function (req, res) {
	getVideos(function(results){
	res.send({'source': results});
	})
});


//Get videos from DB
function getVideos(fn){
	MongoClient.connect(uri, function(err, db) {
	
		if(err) throw err;
		
		var collection = db.collection('video');
		//gets all available videos and sends them to user
		collection.find().toArray(function(err, results) {
			fn(results);
			db.close();
		});
		
	});
}


//Web service get categories
app.get('/cats', function (req, res) {
	getCats(function(results){
	res.send({'categorias': results});
	})
});


//Get categories from DB
function getCats(fn){
	MongoClient.connect(uri, function(err, db) {
	
		if(err) throw err;
		
		var collection = db.collection('feed');
		//gets all different categories and sends them to user
		collection.distinct('categoria', function(err, docs){
			fn(docs);
			db.close();
		});
		
	});
}



//Web service get feeds by category
app.get('/feeds/:search', function (req, res) {
	//gets category for search
	var params = req.params.search.split(",");
	
	//formats text to eliminate blank spaces
	for (var i = 0; i < params.length; i++){
		params[i] = params[i].replace("+", " ");
		params[i] = params[i].trim();
	}
	
	//search feeds for given category
	searchFeeds(params, function(results){
	res.send({'source': results});
	})
});


//Search feeds from DB for specific category
function searchFeeds(params, fn){
	MongoClient.connect(uri, function(err, db) {
		if(err) throw err;
		
		var collection = db.collection('feed');
		//gets results and sends them to user
		collection.find( {'categoria': {'$in': params}}).toArray(function(err, results) {
			fn(results);
			db.close();
		});
		
	});
}


//Web service get user's subscriptions
app.get('/subscriptions/:search', function (req, res) {
	//gets user id from request body
	var id = req.params.search;
	
	//gets user's subscriptions
	getSubscriptions(id, function(results){
		if (results != "user not found" && results != "no subscriptions found"){
			//send subscriptions to user
			res.send({'subscriptions': results});
		}
		else{
			res.send(results);
		}
	})
});


//Search user's subscriptions
function getSubscriptions(id, fn){
	MongoClient.connect(uri, function(err, db) {
		if(err) throw err;
		
		var collection = db.collection('user');
		//gets user from DB
		collection.findOne({'id': id}, function(err, result) {
			if (result){
				if (result.subscriptions){
					//if subscriptions exist, send them
					fn(result.subscriptions);
				}
				else{
					fn("no subscriptions found");
				}
			}
			else{
				fn("user not found");
			}
			db.close();
		});
		
	});
}


//Web service user login
app.post('/login', function (req, res) {
	//gets user from request {id, name, email}
	var user = req.body;
	login(user, function(results){
	res.send(results);
	})
});



//user login
function login(user, fn){
	MongoClient.connect(uri, function(err, db) {
		if(err) throw err;
		
		var collection = db.collection('user');
		//search user in DB
		collection.findOne(user,function(err, result) {
			if (result){
				//if user exists log in
				fn("logged in");
				console.log(format("User %s logged in", result.name));
			}
			else{
				//user not found, give feedback to user
				fn("user not found");
				console.log(format("User %s not found, log in aborted", user.name));
			}
			db.close();
		});
		
	});
}


//Web service user sign in
app.post('/signin', function (req, res) {
	//gets user from request {id, name, email}
	var user = req.body;
	signin(user, function(results){
	res.send(results);
	})
});



//register user
function signin(user, fn){
	MongoClient.connect(uri, function(err, db) {
		if(err) throw err;
		
		var collection = db.collection('user');
		//Search user in database
		collection.findOne(user,function(err, result) {
			if (result){
				//if user exists, abort sign in
				fn("user already exists");
				console.log(format("User %s already exists, sign in aborted", result.name));
			}
			else{
				//register new user
				register(user, fn);
				//write log
				console.log(format("User %s signed in", user.name));
			}
			db.close();
		});
		
	});
}


//register user in DB
function register(user, fn){
	MongoClient.connect(uri, function(err, db) {
	
		if(err) throw err;
		
		var collection = db.collection('user');
		//insert new user in DB
		collection.insert(user, function(err, docs) {
			if (err) fn("Ha ocurrido un error: " + err);
			collection.count(function(err, count) {
				console.log(format("New user signed in. count = %s", count));
				fn("signed in");
				db.close();
			});
		});
		
	});
}



//Web service update subscriptions
app.post('/subscribe', function (req, res) {
	//get subscriptions from request {subscriptions: [name1, name2, ...]}
	var subscriptions = req.body;
	subscribe(subscriptions, function(results){
	res.send(results);
	})
});



//register subscriptions
function subscribe(subscriptions, fn){
	MongoClient.connect(uri, function(err, db) {
		if(err) throw err;
		
		var collection = db.collection('user');
		//updates users subscriptions
		collection.update({'id':subscriptions.id}, {$set:{'subscriptions':subscriptions.subscriptions}}, function(err, result) {
			if (err) fn("Ha ocurrido un error: " + err);
			else{ 
				//writes log and responds to user
				fn("User subscribed");
				console.log("User " + subscriptions.id + " subscriptions updated");
			}
		});
	});
}



http.createServer(app).listen(app.get('port'), function(){
  console.log('Express server listening on port ' + app.get('port'));
});



