var Requester = function(){
        var self = {};
        var running = true;
        var websocket = null;

        self.init = function(args){
			console.log('started ws worker');
        };

        var request = function(args){
			var uri = ((window.location.protocol === "https:") ? "wss://" : "ws://") + "beta.mupop.net/lou/ws";			
        	function openConnection() {
            	websocket = new WebSocket(uri);
          	  	websocket.onmessage = function (event) {
            	};
         	}
        };

        return self;
};

onmessage = function(e){
        var message = JSON.parse(e.data);
        var r = Requester();
        if(message.fn){
                r[message.fn](message.args);
        }
};
