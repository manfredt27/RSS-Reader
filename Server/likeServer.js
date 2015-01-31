var MongoClient = require('mongodb').MongoClient
	, ObjectID = require('mongodb').ObjectID
    , format = require('util').format;

var http = require('http'),
	express = require('express'),
	path = require('path');
	fs = require('fs');

var uri = 'mongodb://admin:Proyecto2@ds043947.mongolab.com:43947/proyecto2'
	
var app = express();
app.set('port', process.env.PORT || 3100);
app.set('view engine', 'jade');



app.use(express.json());       // to support JSON-encoded bodies
app.use(express.urlencoded()); // to support URL-encoded bodies



//Web service get user's recommendations
app.get('/recommendations/:search', function (req, res) {
	//get user id
	var id = req.params.search;
	
	//get recommendations for user
	getRecommendations(id, function(results){
		if (results != "user not found" && results != "no recommendations available"){
			//send recommendation list
			res.send({'recommendations': results});
		}
		else{
			//user not found OR no recommendations available
			res.send(results);
		}
	})
});


//Get user's recommendations
function getRecommendations(id, fn){
	MongoClient.connect(uri, function(err, db) {
		if(err) throw err;
		
		var collection = db.collection('user');
		//Find user that matches given id
		collection.findOne({'id': id}, function(err, result) {
			if (result){
				if (result.recommendations){
					//if user is found and recommendations exist
					fn(result.recommendations);
				}
				else{
					//user found but has no recommendations
					fn("no recommendations available");
				}
			}
			else{
				//user not found
				fn("user not found");
			}
			db.close();
		});
		
	});
}



//Web service like
app.post('/like', function (req, res) {
	//get like information {userID, category, link}
	var like = req.body;
	//save like
	saveLike(like, function(results){
	res.send(results);
	})
});



//Save like in DB
function saveLike(like, fn){
	MongoClient.connect(uri, function(err, db) {
		if(err) throw err;
		
		var collection = db.collection('like');
		//inserts like in DB
		collection.insert(like, function(err, docs) {
			if (err) fn("Ha ocurrido un error: " + err);
			collection.count(function(err, count) {
				//writes in log and responds to user
				console.log(format("Like inserted. count = %s", count));
				fn("like saved");
				db.close();
				//check user's likes to see if recommendations are in order
				checkLikes({'id': like.id, 'categoria': like.categoria});
			});
		});
		
	});
}


//Check likes to see if recommendations are in order
function checkLikes(like){
	MongoClient.connect(uri, function(err, db) {
		if(err) throw err;
		
		var collection = db.collection('like');
		//counts user's likes for given category
		collection.distinct('_id', like, function(err, results) {
			if (results.length % 5 == 0){
				//if a recommendation is in order, execute it
				getRecommendation(like);
			}
			db.close();
		});
	});	
}


//Get recommendation of a specific category
function getRecommendation(like){
	MongoClient.connect(uri, function(err, db) {
		if(err) throw err;
		
		//gets user's current subscriptions
		getSubscriptions(like.id, function(results){
			if (results != "user not found"){
				var collection = db.collection('feed');
				//finds a recommendation different than current subscriptions
				collection.findOne({'categoria': like.categoria, 'nombre': {'$nin': results}}, function(err, result) {
					if (result){
						//if a result is found, recommend to user
						recommend(like.id, result);
					}
				});
			}
		});
	});	
}


//Recommend a new feed to a specific user
function recommend(id, feed){
	MongoClient.connect(uri, function(err, db) {
		if(err) throw err;
		
		var collection = db.collection('user');
		//adds new feed to user's recommendations
		collection.update({'id': id}, {'$push': {'recommendations': feed.nombre}}, function(err, result) {
			if (err) console.log("Ha ocurrido un error: " + err);
			else{ 
				//writes in log
				console.log("User " + id + " recommendations updated");
			}
		});
	});
}



//Get user's subscriptions
function getSubscriptions(id, fn){
	MongoClient.connect(uri, function(err, db) {
		if(err) throw err;
		
		var collection = db.collection('user');
		//gets user from DB
		collection.findOne({'id': id}, function(err, result) {
			if (result){
				//returns subscription in callback function
				fn(result.subscriptions);
			}
			else{
				fn("user not found");
			}
			db.close();
		});
		
	});
}


//Web service dislike
app.post('/dislike', function (req, res) {
	//get dislike information {userID, category, link}
	var dislike = req.body;
	//save dislike in DB
	saveDislike(dislike, function(results){
	res.send(results);
	})
});



//Save dislike in DB
function saveDislike(dislike, fn){
	MongoClient.connect(uri, function(err, db) {
		if(err) throw err;
		
		var collection = db.collection('dislike');
		//inserts dislike in DB
		collection.insert(dislike, function(err, docs) {
			if (err) fn("Ha ocurrido un error: " + err);
			collection.count(function(err, count) {
				//writes log and responds to user
				console.log(format("Dislike inserted. count = %s", count));
				fn("dislike saved");
				db.close();
			});
		});
		
	});
}


//Web service update subscriptions (used when user subscribes to a recommended feed)
app.post('/subscribe', function (req, res) {
	//gets subscriptions from request {subscriptions: [name1, name2, ...]}
	var subscriptions = req.body;
	//subscribe and delete from recommendations
	subscribe(subscriptions, function(results){
	res.send(results);
	})
});



//register subscriptions
function subscribe(subscriptions, fn){
	MongoClient.connect(uri, function(err, db) {
		if(err) throw err;
		
		var collection = db.collection('user');
		//adds new subscriptions to user
		collection.update({'id':subscriptions.id}, {'$addToSet':{'subscriptions':subscriptions.subscriptions}}, function(err, result) {
			if (err) fn("Ha ocurrido un error: " + err);
			else{ 
				//responds to user and writes log
				fn("User subscribed");
				console.log("User " + subscriptions.id + " subscriptions updated");
			}
		});
		
		var collection = db.collection('user');
		//removes feeds from recommendations
		collection.update({'id':subscriptions.id}, {'$pullAll':{'recommendations':subscriptions.subscriptions}}, function(err, result) {
			if (err) fn("Ha ocurrido un error: " + err);
			else{ 
				//writes log
				console.log("User " + subscriptions.id + " recommendations updated");
			}
		});
		
		db.close();
	});
}


http.createServer(app).listen(app.get('port'), function(){
  console.log('Express server listening on port ' + app.get('port'));
});