var eddie2 = (function(context, louSettings) {

    var loc; //Current URL of the page
    var wsLoc; //URL to connect to websocket!
    var listeners = {};
    
    function init() {
        wsLoc = getWsURI(context.location);
        
        register();
    }
    
    function getScreenId(){
    	return new Promise(function(resolve, reject){
    		var interval = setInterval(function(){
    			if(eddie.getScreenId()){
    				clearInterval(interval);
    				resolve(eddie.getScreenId());
    			}
    		});
    	});
    }
    
    var register = function(){
    	getScreenId().then(function(){
    		return getServerConnection();
    	}).then(function(conn){
    		var message = {
    			command: 'register',
    			app: louSettings.fullapp,
    			screenId: eddie.getScreenId()
    		}
    		conn.send(JSON.stringify(message));
    	});
    };
    
    var subscribe = function(action, callback){
    	if(!listeners[action]){
    		listeners[action] = [];
    	}
    	listeners[action].push(callback);
    }

    var getServerConnection = (function(){
			var serverConnection;

			return function(){
        if (!serverConnection) {
            serverConnection = new Promise(function(resolve, reject) {
                var conn = new WebSocket(wsLoc);
                conn.onopen = function(event) {
                    resolve(conn);
                };
                conn.onmessage = receiveMessage;
            });
        }

        return serverConnection;
    	};
		})();

    function getWsURI(loc) {
        var newURI;
        newURI = loc.protocol === 'https:'
            ? 'wss:'
            : 'ws:';
        newURI += "//" + loc.host + ':8080/lou/ws';
        return newURI;
    }
    
    init();

		function sendMessage(target, message){
			message = Object.assign({}, message, {app: louSettings.fullapp, screenId: eddie.getScreenId()});
			getServerConnection().then(function(conn){
				conn.send(JSON.stringify(message));
			})
		}

		function receiveMessage(message){
			var data = JSON.parse(message.data);
			var action = data.action;
			var data = data.data;
			var target = data.target;
			
			listeners[action].forEach(function(callback){
				callback.apply(this, [data]);
			});
			
		};

		return {
			sendMessage: sendMessage,
			receiveMessage: receiveMessage,
			subscribe: subscribe
		};
})(window, LouSettings);
