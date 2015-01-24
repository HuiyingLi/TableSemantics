// Pick memes
var PORTS = [6999]
//var TOP_BOT_NAME = __dirname + '/../client/memeTopBottom.html';
var child_process = require('child_process');

var express = require("express");
var app = express();
var fs = require("fs");
//var memePicker = require('./memePicker.js');

var carrier = require('carrier');


function run_cmd(cmd, args, str, callBack ) {
    var spawn = require('child_process').spawn;
    var child = spawn(cmd, args);
    var resp = "";
    child.stdin.write(str + "\n");
    child.stdout.on('data', function (buffer) { resp += buffer.toString() });
    child.stdout.on('end', function() { callBack (resp) });
} 


app.use(express.bodyParser());


app.post("/user/add", function (req, res) {
    res.send("OK");
});

function pad0(n, len) { // pad an integer to be length n
    n = n + "";
    while (n.length < len) n = "0" + n;
    return n;
}


//memePicker.stdout.on("data", function (data) { callback(data); });

app.get("/TopBottom.html", function (req, res) {
    try {
        var top = (req.query.topText || "").trim(); 
        console.log(top);
        run_cmd("java", ["-jar", "search.jar"], top, function (data){ console.log(data); res.send(data);} )
      
    } catch (e) {
        console.error(e+ "  DARN!!!!!")
    }
});

/* serves all the static files */

app.use(express.errorHandler())

app.use('/', express.static(__dirname + '/public'));

PORTS.forEach(function (port) {
    try {
        app.listen(port, function () {
            console.log("Listening on port " + port);
        });
    } catch (e) {
        console.log("Failed to listen on port " + port)
    }
});
