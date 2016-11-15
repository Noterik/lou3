var Eddie = function(options){

	var self = {};
	var callers = {};
	var callvars = {};
	var trackers = {};
	var trackervalues = {};
	var scripttypes = {};

	var settings = {
		lou_ip: "",
		lou_port: "",
		app: "",
		fullapp: "",
		postData: "<fsxml><screen><properties><screenId>-1</screenId></properties><capabilities id=\"1\"><properties>"+getCapabilities()+"</properties></capabilities></screen></fsxml>",
		screenId: "",
		timeoffset: -1,
		active: true,
		appparams: null,
		worker_location: '/eddie/js/eddie_worker.js',
		worker: null
	};
	$.extend(settings, options);

	settings.lou_port = (window.location.port === "") ? '80' : window.location.port;

	self.init = function(){
		responsetime = new Date().getTime();
		if(typeof(Worker) != "undefined"){
			settings.worker = new Worker(settings.worker_location);
  		}else{
  			console.log("Worker not supported");
  		}

		$(self).on('register-success', self.listen);
		register();
		addGestureEvents();

		interval = setInterval(function () {
		nowdate = new Date().getTime();
		delaydate = nowdate-responsetime;
		if (delaydate>(30*1000)) {
			clearInterval(interval);
			window.location.href=window.location.href;
		}
            for (var data in trackers){
   				var map = {};
				var tid = data;
				var tracks = trackers[data].split(",");
				for(i = 0; i < tracks.length; i++){
					var track = tracks[i];
					var trackp = track.split('(');
					if ($('#'+tid).length) {
            			   	    switch(trackp[0]){
            					case "vars":
							var tname = trackp[1].substring(0,trackp[1].length-1);
							var v = callvars[tid];
							var oldvalue = trackervalues[tid+"/"+track];
							var newvalue = v[tname];
							if (oldvalue!=newvalue) {
                                                              trackervalues[tid+"/"+track] = newvalue;
                                                              map[tname] = newvalue;
                                                        }
							break;
            					case "screenXPerc":
					    		var position = $('#'+tid).position();
                                			var oldvalue = trackervalues[tid+"/"+track];
                                			var newvalue = (position.left/window.innerWidth)*100;
					    		if (oldvalue!=newvalue) {
									trackervalues[tid+"/"+track] = newvalue;
									map['screenXPerc'] = newvalue;
					    		}
            				    		break;
            					case "screenYPerc":
					    		position = $('#'+tid).position();
                                			oldvalue = trackervalues[tid+"/"+track];
                             				newvalue = (position.top/window.innerHeight)*100;
					    		if (oldvalue!=newvalue) {
									trackervalues[tid+"/"+track] = newvalue;
									map['screenYPerc'] = newvalue;
					    		}
            						break;
            					case "mousemove":
                                	var send = trackervalues[tid+"/"+track+"_send"];
									if (send === 'true') {
											trackervalues[tid+"/"+track+"_send"] = 'false';
											parts = trackervalues[tid+"/"+track].split(',');
											map['screenX'] = parseFloat(parts[0]);
											map['screenY'] = parseFloat(parts[1]);
											map['clientX'] = parseFloat(parts[2]);
											map['clientY'] = parseFloat(parts[3]);																		
											map['screenXP'] = parseFloat(parts[4]);
											map['screenYP'] = parseFloat(parts[5]);
											map['clientXY'] = trackervalues[tid+"/"+track];
											map['width'] = $('#'+tid).width();
											map['height'] = $('#'+tid).height();
								
									}	
            						break;
            					case "devicemotion":
                                			var send = trackervalues["screen/devicemotion_send"];
							if (send === 'true') {
								trackervalues["screen/devicemotion_send"] = 'false';
								map['alpha'] = trackervalues["screen/devicemotion_alpha"];
								map['beta'] = trackervalues["screen/devicemotion_beta"];
								map['gamma'] = trackervalues["screen/devicemotion_gamma"];
							}
            						break;
            				case "location":
								navigator.geolocation.getCurrentPosition(getPosition);
					    		newvalue = trackervalues["screen/location"];
                                			oldvalue = trackervalues["screen/location_old"];
					    		if (oldvalue!=newvalue) {
									trackervalues[tid+"/location_old"] = newvalue;
									map["location"] = newvalue;
									console.log("gps info="+newvalue);
					    		}
            						break;
            					case "currentTime":
					    		newvalue = $('#'+tid)[0].currentTime;
                                			oldvalue = trackervalues[tid+"/"+track];
					    		if (oldvalue!=newvalue) {
									trackervalues[tid+"/"+track] = newvalue;
									map['currentTime'] = newvalue*1000;
					    		}
            						break;
            					default:
            						break;
					     }
				   	}
				}
                                var line = JSON.stringify(map);
				if (line!="{}") {
                                	self.putLou("","event("+tid+"/client,"+line+")");
				}
			}
		}, 30);

	};
	
	self.getTimeOffset = function() {
		return settings.timeoffset;
	};

	self.destroy = function() {
		/*
		$.each(components, function(key, comp){
			if(typeof comp.destroy === "function"){
				comp.destroy();
			}
		});
		*/
		var splits = settings.screenId.split('/');

		self.putLou('notification','show(user ' + splits[splits.length - 1] + ' left session!)');
		var postData = "stop(" + settings.screenId + ")";
		var args =
		self.doRequest({
			'type': 'POST',
			'url': "http://" + settings.lou_ip + ":" + settings.lou_port + "/lou/LouServlet" + settings.fullapp,
			'data': postData,
			'dataType': 'text',
			'async': false
		});
	};

	self.doRequest = function(args){
		$.ajax(args);
	};

	self.sendEvent = function(targetid,eventtype,data){
	    data['eventtype'] = eventtype;
            self.putLou("","event("+targetid+"/client,"+JSON.stringify(data)+")");
	};

	self.getComponent = function(comp){
		return components[comp];
	};
	

	self.listen = function(){
		if(!settings.worker){
			$(self).on('request-success', function(event, response){
				if(settings.active){
					try{
						parseResponse(response);
					}catch(error){
						console.log(error);
					}
					request();
				}
			});
			request();
		}else{
			settings.worker.postMessage(JSON.stringify({
				'fn': 'init',
				'args': {
					'lou_ip': settings.lou_ip,
					'lou_port': settings.lou_port,
					'screenId': settings.screenId
				}
			}));
			settings.worker.onmessage = function(m){
				parseResponse(m.data);
			};
		}
	};

	self.getScreenId = function(){
		return settings.screenId;
	};

	self.log = function(msg) {
		self.putLou("","log("+msg+",info)");
		return false;
	};

	self.getVars = function(targetid) {
		return callvars[targetid];
	};

        self.log = function(msg,level) {
                self.putLou("","log("+msg+","+level+")");
                return false;
        };

	self.putLou = function(targetid, content, sync) {
		var postData = "put(" + settings.screenId + "," + targetid + ")=" + content;
		self.doRequest({
			'type': 'POST',
			'url': 'http://' + settings.lou_ip + ":" + settings.lou_port + "/lou/LouServlet" + settings.fullapp,
			'contentType': 'text/plain',
			'data': postData,
			'dataType': 'text',
			'async': !sync
		});

		return false;
	};

	self.makesound = function(sound) {
		var audio = new Audio('/eddie/sounds/'+sound+'.mp3');
		audio.play();
	};

	var register = function(){
		var parseRegisterResponse = function(response){
			settings.screenId = $(response).find('screenid').first().text();
			var servertime = parseInt($(response).find('servertime').first().text());
			settings.timeoffset = new Date().getTime() - servertime;
			console.log("timedelay="+settings.timeoffset);
			$(self).trigger('register-success');
		};
		self.doRequest({
			'type': 'POST',
			'url': 'http://' + settings.lou_ip +":"+ settings.lou_port + '/lou/LouServlet' + settings.fullapp+"?"+settings.appparams,
			'data': settings.postData,
			'success': parseRegisterResponse
		});
	};

	var request = function(){
		var putData = "<fsxml><screen><properties><screenId>" + settings.screenId + "</screenId></properties></screen></fsxml>";
		var appId = settings.screenId.substring(0, settings.screenId.indexOf("/1/screen"));

		self.doRequest({
			'type': 'POST',
			'url': 'http://' + settings.lou_ip + ':' + settings.lou_port + '/lou/LouServlet' + appId,
			'data': putData,
			'dataType': 'text',
			'contentType': 'text/plain',
			'success': function(data){
				$(self).trigger('request-success', data);
			}
		});
	};

	var parseResponse = function(response){
		responsetime = new Date().getTime();
		var result = response;
		if (result.indexOf("<screenid>appreset</screenid>")!=-1) {
				alert('server reset');
		}
		var pos = result.indexOf("(");
		while (pos!=-1) {
			var command = result.substring(0,pos);
			result = result.substring(pos+1);
			pos = result.indexOf(")");

            var targetid = result.substring(0,pos);
            switch(command){
            	case "set":
            		var content = result.substring(pos+2);
            		pos = content.indexOf("($end$)");
            		if(pos!=-1) {
            				content = content.substring(0,pos);
            			}
            			setDiv(targetid,content);
            		break;
            	case "val":
            		content = result.substring(pos+2);
            		pos = content.indexOf("($end$)");
            		if(pos!=-1) {
            			content = content.substring(0,pos);
            		}
                    $('#'+targetid).val(content);
            		break;
            	case "html":
            		content = result.substring(pos+2);
            		pos = content.indexOf("($end$)");
            		if(pos!=-1) {
            				content = content.substring(0,pos);
            			}
                    	$('#'+targetid).html(content);
            		break;
            	case "location":
            		content = result.substring(pos+2);
            		pos = content.indexOf("($end$)");
            		if(pos!=-1) {
            				content = content.substring(0,pos);
            			}
                    	window.location.href = content;
            		break;
            	case "translateXY":
            		content = result.substring(pos+2);
            		pos = content.indexOf("($end$)");
            		if(pos!=-1) {
            				content = content.substring(0,pos);
            			}
                    	doTranslateXY(targetid,content);
            		break;
                case "append":
                        content = result.substring(pos+2);
                        pos = content.indexOf("($end$)");
                        if(pos!=-1) {
                                        content = content.substring(0,pos);
                                }
                        $('#'+targetid).append(content);
                        break;
                case "parsehtml":
                        content = result.substring(pos+2);
                        pos = content.indexOf("($end$)");
                        if(pos!=-1)
                                 {
                                 	content = content.substring(0,pos);
                                 }
				parseHtml(targetid,content);
                        break;
            	case "show":
            		content = result.substring(pos+2);
            		pos = content.indexOf("($end$)");
            		if(pos!=-1) {
            				content = content.substring(0,pos);
            			}
                    	$(targetid).show();
            		break;
                case "play":
                        content = result.substring(pos+2);
                        pos = content.indexOf("($end$)");
                        if(pos!=-1) {
                                content = content.substring(0,pos);
                               }
						$("#"+targetid)[0].play();
                        break;
                case "pause":
                        content = result.substring(pos+2);
                        pos = content.indexOf("($end$)");
                        if(pos!=-1) {
                                content = content.substring(0,pos);
                        }
						$("#"+targetid)[0].pause();
                        break;
                case "autoplay":
            		content = result.substring(pos+2);
            		pos = content.indexOf("($end$)");
            		if(pos!=-1) {
            				content = content.substring(0,pos);
            			}
                    	$("#"+targetid)[0].autoplay=content;
            		break;
                case "volume":
            		content = result.substring(pos+2);
            		pos = content.indexOf("($end$)");
            		if(pos!=-1) {
            				content = content.substring(0,pos);
            			}
                    	$("#"+targetid)[0].volume=content;
            		break;
                case "loop":
            		content = result.substring(pos+2);
            		pos = content.indexOf("($end$)");
            		if(pos!=-1) {
            				content = content.substring(0,pos);
            			}
                    	$("#"+targetid)[0].loop=content;
            		break;
            	case "hide":
            		content = result.substring(pos+2);
            		pos = content.indexOf("($end$)");
            		if(pos!=-1) {
            				content = content.substring(0,pos);
            			}
                    	$(targetid).hide(content);
            		break;
            	case "draggable":
            		content = result.substring(pos+2);
            		pos = content.indexOf("($end$)");
            		if(pos!=-1) {
            				content = content.substring(0,pos);
            			}
                    	$(targetid).draggable();
            		break;
            	case "bind":
            		content = result.substring(pos+2);
            		pos = content.indexOf("($end$)");
            		if(pos!=-1) { content = content.substring(0,pos); }
                   	setBind(targetid,content);
			break;
            	case "template":
            		content = result.substring(pos+2);
            		pos = content.indexOf("($end$)");
            		if(pos!=-1) { content = content.substring(0,pos); }
                   	setTemplate(targetid,content);
			break;
            	case "syncvars":
            		content = result.substring(pos+2);
            		pos = content.indexOf("($end$)");
            		if(pos!=-1) { content = content.substring(0,pos); }
                   	setSyncvars(targetid,content);
			break;
            	case "update":
            	 	content = result.substring(pos+2);
            		pos = content.indexOf("($end$)");
            		if(pos!=-1) { content = content.substring(0,pos); }
                   	doUpdate(targetid,content);
			break;
             	case "add":
            	    content = result.substring(pos+2);
            		pos = content.indexOf("($end$)");
            		if(pos!=-1) {
            				content = content.substring(0,pos);
            			}
            			addToDiv(targetid,content);
            		break;
            	case "put":
            		content = result.substring(pos+2);
            		pos = content.indexOf("($end$)");
            		if(pos!=-1) {
            				content = content.substring(0,pos);
            			}
						putMsg(targetid,content);
					break;
				case "remove":
					remove(targetid);
					break;
				case "setcss":
					content = result.substring(pos+2);
					pos = content.indexOf("($end$)");
					if (pos!=-1) { content = content.substring(0,pos); }
                    	setCSS(content);
                    break;
                case "setstyle":
					content = result.substring(pos+2);
					pos = content.indexOf("($end$)");
					if (pos!=-1) {
							content = content.substring(0,pos);
						}
                    	setStyle(content);
                    break;
            	case "setscript":
            		content = result.substring(pos+2);
					pos = content.indexOf("($end$)");
					if (pos!=-1) {
							content = content.substring(0,pos);
						}
                	setScript(targetid,content);
                	break;
                case "removestyle":
            		content = result.substring(result.substring(result.indexOf("("))+1, result.indexOf(")"));
					removeStyle(content);
                	break;
                case "sdiv":
                        content = result.substring(pos+2);
                        pos = content.indexOf("($end$)");
                        if (pos!=-1) { content = content.substring(0,pos); }
                        setDivProperty(targetid,content);
                        break;
               default:
                		break;
            }

            // lets check if there are move messages
			pos = result.indexOf("($end$)");
			if (pos!=-1) {
				result = result.substring(pos+7);
				pos = result.indexOf("(");
			}else {
				pos = -1;
			}
		}
	};

	function remove(args){
		var splits = args.split(",");
		var targetid = splits[0];
		var leaveDiv = splits[1];

		if(leaveDiv === "false"){
			removeDiv(targetid);
		}else{
			emptyDiv(targetid);
		}

		removeScript(targetid);
		removeInstance(targetid);
	}

	function removeDiv(targetid) {
		var div = document.getElementById(targetid);
		if (div!==null) {
			div.parentNode.removeChild(div);
		}
	}

	function emptyDiv(targetid){
		var div = document.getElementById(targetid);
		if(div!==null){
			div.innerHTML = "";
		}
	}
	

	function removeScript(targetid){
		$('#script_' + targetid).remove();
	}

	function removeInstance(targetid){
		//delete components[targetid];
	}

	function putMsg(targetid,content) {
		var new_content = content;

		try{
			var pos = new_content.indexOf("(");
			if (pos!==-1) {
				var command = new_content.substring(0,pos);
				var args = new_content.substring(pos+1,new_content.length-1);
				new_content = {
					"target" : [{
						"id" : command,
						"class" : command
					}],
					"content" : args,
					"originalMessage" : content
				};
			}
		}catch(e){ console.log("escaped: " + content);}

		content = new_content;
		var div = document.getElementById(targetid);

		if(components[targetid]){
			components[targetid].putMsg(content);
		}else{
			window[targetid+"_putMsg"](content);
		}
	}

	function setCSS(filename) {
	  var fileref=document.createElement("link");
	  fileref.setAttribute("rel", "stylesheet");
	  fileref.setAttribute("type", "text/css");
	  fileref.setAttribute("href", filename);
	  document.getElementsByTagName("head")[0].appendChild(fileref);
	}

	function setStyle(content){
		try{
			var css = content;
			var stylename = content.substring(0, content.indexOf(","));
			// if(stylename.indexOf("_")!==-1) stylename = stylename.substring(stylename.indexOf("_"));
			// console.log("**************"+content);
			// console.log('stylename: ' + stylename);
			var head = document.getElementsByTagName('head')[0],
			    style = document.getElementsByTagName('style'),
			    sstyle = $("style#"+stylename);
			    content = content.substring(content.indexOf(",")+1);
			if(sstyle.length===0){
				sstyle = document.createElement("style");
				sstyle.type = 'text/css';
				sstyle.setAttribute("id", stylename);
				if (style.styleSheet){
				  sstyle.styleSheet.cssText = content;
				} else {
				  sstyle.appendChild(document.createTextNode(content));
				}
				head.appendChild(sstyle);
			}
			else{
				sstyle.html(content);
			}
		}catch(err){
			var trace = printStackTrace();
		    console.error("eddie.js: "+err.message + "\n\n" + trace.join('\n\n'));
		}
	}

	function removeStyle(style){
		$('style#'+style).remove();
	}

	function setScript(targetid, scriptbody) {
		// ugly code to map object and create a dataspace for it
		var oname = scriptbody.substring(0,100);
		var pname = oname.split(' ');
		if (pname.length>2) {
			try { // fails if not var name format in js file and thats ok
				if (scripttypes[pname[1]]===undefined) {
					var script   = document.createElement("script");
					script.type  = "text/javascript";
					script.text  = scriptbody;
					script.id = 'script_' + targetid;
					document.body.appendChild(script);
					scripttypes[pname[1]] = targetid;
				}
				var obj = eval(pname[1]);
				//callvars.set(targetid.substring(1),new Map().set("targetid",targetid.substring(1)));
				callvars[targetid.substring(1)]={"targetid":targetid.substring(1)};
				callers[targetid.substring(1)]=obj;
			} catch(err) {
				console.log(err);
			}
		} else {
			script   = document.createElement("script");
			script.type  = "text/javascript";
			script.text  = scriptbody;
			script.id = 'script_' + targetid;
			document.body.appendChild(script);
		}
	}


	function setDivProperty(targetid,content) {
                var div = document.getElementById(targetid);
                if (div!==null) {
			var commands = content.split(",");
			for(i = 0; i < commands.length; i++){
				var command = commands[i];
				var eventtargets = command.split(":");
				command = eventtargets[0];
            			switch(command){
               			case "draggable":
					if (eventtargets.length>1) {
						var data = eval('(' + content.substring(10)+ ')');
                    				$('#'+targetid).draggable(data);
					} else {
                    				$('#'+targetid).draggable();
					}
					break;
               			case "undraggable":
                    			$('#'+targetid).draggable('disable');
					break;
               			case "style":
                    			$('#'+targetid).css(eventtargets[1],eventtargets[2]);
					break;
               			case "sound":
					self.makesound(content.substring(6));
					break;
               			case "bind":
                        		for(j = 1; j < eventtargets.length; j++){
                    				$('#'+targetid).bind(eventtargets[j], {etarget: eventtargets[j]}, function(event) {
							var data = event.data;
							if (this.tagName=="INPUT") {
								var edata = targetid+".value="+this.value;
								self.putLou("",targetid+"/"+data.etarget+"("+edata+")");
							} else {
								edata = "clientX="+event.clientX+",clientY="+event.clientY;
								edata += ",screenX="+event.screenX+",screenY="+event.screenY;

								var draggedElement = jQuery(event.srcElement);
								var dragOffset = draggedElement.offset();
								var elementOffsetLeft = dragOffset.left;
								var elementOffsetTop = dragOffset.top;

								var dragPosition = draggedElement.position();
								var elementPositionLeft = dragPosition.left;
								var elementPositionTop = dragPosition.top;

								var elementWidth = draggedElement[0].clientWidth;
								var elementHeight = draggedElement[0].clientHeight;

								edata += ",elementWidth=" + elementWidth;
								edata += ",elementHeight=" + elementHeight;
								edata += ",elementOffsetTop=" + elementOffsetTop;
								edata += ",elementOffsetLeft=" + elementOffsetLeft;
								edata += ",elementPositionTop=" + elementPositionTop;
								edata += ",elementPositionLeft=" + elementPositionLeft;

								self.putLou("",targetid+"/"+data.etarget+"("+edata+")");
							}
						});
					}
					break;
					default:
					break;
				}
			}
		}
	}

	function parseHtml(targetid,data) {
           var pdata =  JSON.parse(data);
           var parsed = Mustache.render(pdata.template,pdata);
           $('#'+targetid).html(parsed);
	}

        function setTemplate(targetid,content) {
           	var data =  JSON.parse(content);
		callvars[targetid]={"template":data.template};
	}

        function doUpdate(targetid,content) {
           	var data =  JSON.parse(content);
		data['targetid'] = targetid;
		callers[targetid].update(callvars[targetid],data);
	}


        function setSyncvars(targetid,content) {
           	var data =  JSON.parse(content);
		var vars = callvars[targetid];
		for (var key in data) {
			//vars.set(key, data[key]);
			vars[key]=data[key];
		}
	}
	
	function doTranslateXY(targetid,content) {
        var xy = content.split(",");
        var x = xy[0];
        var y = xy[1];
        if (x.indexOf('%')!==0) {
       		var pw = $('#'+targetid).parent();
        	x = (pw.width()/100)*(x.substring(0,x.length-1));
        } 
        if (y.indexOf('%')!==0) {
       		var ph = $('#'+targetid).parent();
        	y = (ph.height()/100)*(y.substring(0,y.length-1));
        } 
        $('#'+targetid).css('transform','translate('+x+'px,'+y+'px)');
	}

	function setBind(targetid,content) {
         var div = document.getElementById(targetid);
 		if (content.indexOf('keypress')===0) {
                	$(document).keydown(function(e) {
                    	if (event.which==9) {
                      	  e.preventDefault();
							e.stopPropagation();
						}

                	});
                	$(document).keyup(function(e) {
                        map = {};
                        map["targetid"] = targetid;
                        map["which"] = event.which;
                        map["id"] = event.target.id;

                        var padding = content.split(",");
                        if (padding.length>1) {
        				for (var i = 1; i < padding.length; i++) {
                			var name = padding[i];
							var p = $("#"+name);
							var nt=$('input[name='+name+']:checked').val();
							if (nt!==undefined) {
    							map[name] = nt;
                			} else if (p.prop("tagName")==="INPUT") {
    							map[name] = $("#"+name).val();
							} else {
    							map[name] = $("#"+name).val();
							}
						}
					}
		        		self.putLou("","event("+targetid+"/keypress,"+JSON.stringify(map)+")");
                	});
        } else if (content.indexOf('play')===0 || content.indexOf('pause')===0 || content.indexOf('ended')===0 || content.indexOf('error')===0) {
               	$("#"+targetid).on(content, function() {
               	    var map = {};
               	     map["targetid"] = targetid;
               	     map["currentTime"] = $("#"+targetid)[0].currentTime*1000;
               	     map["playbackRate"] = $("#"+targetid)[0].playbackRate;
               	     map["buffered"] = $("#"+targetid)[0].buffered;
               	     map["autoplay"] = $("#"+targetid)[0].autoplay;
               	     map["loop"] = $("#"+targetid)[0].loop;
               		self.putLou("","event("+targetid+"/"+content+","+JSON.stringify(map)+")");
               	});
		} else if (content.indexOf('track/')===0) {
			trackers[targetid] = content.substring(6);
			if (content.indexOf('track/devicemotion')===0) {
				window.addEventListener('devicemotion',function(event) {
				    var alpha = Math.round(event.rotationRate.alpha);
				    var beta = Math.round(event.rotationRate.beta);
				    var gamma = Math.round(event.rotationRate.gamma);
				    if (Math.abs(alpha)>10 || Math.abs(beta)>10 || Math.abs(gamma)>10 ) {
                                    	trackervalues["screen/devicemotion_alpha"] = alpha;
                                    	trackervalues["screen/devicemotion_beta"] = beta;
                                    	trackervalues["screen/devicemotion_gamma"] = gamma;
                                    	trackervalues["screen/devicemotion_send"] = "true";
				    }
				});
			} else if (content.indexOf('track/mousemove')===0) {
				// tricky since we need to track it
				console.log('track');
				$("#"+targetid).mousemove(function() {
					// set these already in the tracker to be send
                    var oldvalue = trackervalues[targetid+"/mousemove"];
        			var xp = (event.layerX/event.target.offsetWidth)*100;
            		var yp = (event.layerY/event.target.offsetHeight)*100;
            		var newvalue = event.offsetX+','+event.offsetY+','+event.layerX+','+event.layerY+','+xp+','+yp;
                    if (oldvalue!=newvalue) {
                        	trackervalues[targetid+"/mousemove"] = newvalue;
                            trackervalues[targetid+"/mousemove_send"] = "true";
					}
				});
				$("#"+targetid).on('touchmove', function() {
					// set these already in the tracker to be send
          			var oldvalue = trackervalues[targetid+"/mousemove"];
					var newvalue = '';

					var boundingBox = this.getBoundingClientRect();

					for (var i = 0; i < event.touches.length; i++) {
						var touch = event.touches[i];
						var top = touch.pageY - boundingBox.top;
						var left = touch.pageX - boundingBox.left;
            			if (i===0) {
            			    var xp = (left/window.innerWidth)*100;
            				var yp = (top/window.innerHeight)*100;
							newvalue += left+','+top+','+left+','+top+','+xp+','+yp;

					  	} else {
							newvalue += ','+left+','+top+','+left+','+top+','+xp+','+yp;
					  	}
					}
          if (oldvalue!=newvalue) {
               trackervalues[targetid+"/mousemove"] = newvalue;
               trackervalues[targetid+"/mousemove_send"] = "true";
					}
					event.preventDefault();
				});
			}
		} else if (div!==null) {
                	var eventtargets = content.split(":");
					for(j = 0; j < eventtargets.length; j++){
						var padding = eventtargets[j].split(",");
                        $("#"+targetid).bind(padding[0], {etarget: eventtargets[j]}, function(event) {
                           var data = event.data;
                           sendBasicEvent(targetid,this,data,event);
                        });
					}
		} else {
					var eventtargets = content.split(":");
					for(j = 0; j < eventtargets.length; j++){
						var padding = eventtargets[j].split(",");
						$("."+targetid).bind(padding[0], {etarget: eventtargets[j]}, function(event) {
							var data = event.data;
							sendBasicEvent(targetid,this,data,event);
						});
					}
		}

	}

 
     function sendBasicEvent(targetid,obj,data,event) {
		if (obj.tagName==="INPUT") {
		      var map = {};
              map[targetid+".value"]=obj.value;
              map["value"]=obj.value;
              map["id"] = event.target.id;
		      self.putLou("","event("+targetid+"/"+data.etarget+","+JSON.stringify(map)+")");
		} else if (obj.tagName==="SELECT") {
			  var map = {};
              map["value"]=obj.value;
              map["id"] = event.target.id;
		      self.putLou("","event("+targetid+"/"+data.etarget+","+JSON.stringify(map)+")");
        } else {
			var padding = data.etarget.split(",");
			map = {};
			//map[targetid+".value"]=obj.value;
			map["targetid"] = targetid;
			map["clientX"] = event.clientX;
			map["clientY"] = event.clientY;
			map["screenX"] = event.screenX;
			map["screenY"] = event.screenY;
			var xp = (event.clientX/window.innerWidth)*100;
            var yp = (event.clientY/window.innerHeight)*100;
            map["screenXP"] = xp;
			map["screenYP"] = yp;
			
	
			if (padding.length>1) {
        			for (var i = 1; i < padding.length; i++) {
                			var name = padding[i];
					var p = $("#"+name);
					var nt=$('input[name='+name+']:checked').val();
					if (nt!==undefined) {
    						map[name] = nt;
                			} else if (p.prop("tagName")==="INPUT") {
    						map[name] = $("#"+name).val();
					} else {
    						map[name] = $("#"+name).val();
					}
				}
			}

			var draggedElement = jQuery(event.srcElement);
			var dragOffset = draggedElement.offset();
			if (dragOffset !== undefined) {
				var elementOffsetLeft = dragOffset.left;
				var elementOffsetTop = dragOffset.top;
				map["elementOffsetTop"] = elementOffsetTop;
                        	map["elementOffsetLeft"] = elementOffsetLeft;
			}		
	
			var dragPosition = draggedElement.position();
			if (dragPosition !== undefined) {
				var elementPositionLeft = dragPosition.left;
				var elementPositionTop = dragPosition.top;
				map["elementPositionTop"] = elementPositionTop;
                        	map["elementPositionLeft"] = elementPositionLeft;
			}	
			
			if (draggedElement[0] !== undefined) {
				var elementWidth = draggedElement[0].clientWidth;
				var elementHeight = draggedElement[0].clientHeight;
				map["elementWidth"] = elementWidth;
				map["elementHeight"] = elementHeight;
			}
			map["id"] = event.target.id;
		        self.putLou("","event("+targetid+"/"+padding[0]+","+JSON.stringify(map)+")");
		}
	}

 
	function simpleKeys (original) {
  		return Object.keys(original).reduce(function (obj, key) {
    			obj[key] = typeof original[key] === 'object' ? '{ ... }' : original[key];
    			return obj;
  		}, {});
	}

	function setDiv(targetid,content) {
		var div = document.getElementById(targetid);
		if (div!==null) {
	       	    $('#'+targetid).html(content);
		} else {
	  	    div = document.createElement('div');
	  	    div.setAttribute('id',targetid);
	            div.innerHTML = content;
	            document.getElementById("screen").appendChild(div);
		}

	}

	function addToDiv(targetid,content) {
		var div = document.getElementById(targetid);
		if (div!==null) {

	       	    ne = document.createElement('div');
	            ne.innerHTML = content;
	            div.appendChild(ne);
		}
	}

	function getCapabilities() {
		var body="";
		body +="<platform>"+navigator.platform+"</platform>";
		body +="<appcodename>"+navigator.appCodeName+"</appcodename>";
		body +="<appname>"+navigator.appName+"</appname>";
		body +="<appversion>"+navigator.appVersion+"</appversion>";
		body +="<useragent>"+navigator.userAgent+"</useragent>";
		body +="<cookiesenabled>"+navigator.cookieEnabled+"</cookiesenabled>";
		body +="<screenwidth>"+window.innerWidth+"</screenwidth>";
		body +="<screenheight>"+window.innerHeight+"</screenheight>";
		body +="<orientation>"+window.orientation+"</orientation>";
		body +="<documenturl>"+location.hostname+"</documenturl>";

		var browserid = readCookie("smt_browserid");
		if (browserid===null) {
			var date = ""+new Date().getTime();
			createCookie("smt_browserid",date,365);
		}
		browserid = readCookie("smt_browserid");
		body +="<smt_browserid>"+browserid+"</smt_browserid>";

		var s = document.URL;
		s = s.substring(s.indexOf("/domain/")+8);
		s = s.substring(0,s.indexOf("?"));
		s = s.replace(/\//g,"_");
		s = ""; // ignore sessions per app for now
		var sessionid = readCookie("smt_"+s+"_sessionid");
		if (sessionid===null) {
			date = ""+new Date().getTime();
			createCookie("smt_"+s+"_sessionid",date,365);
		}
		sessionid = readCookie("smt_"+s+"_sessionid");
		body +="<smt_sessionid>"+sessionid+"</smt_sessionid>";
		return body;
	}

	function createCookie(name, value, days) {
    	var expires;
    	if (days) {
       	 	var date = new Date();
       	 	date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
         	expires = "; expires=" + date.toGMTString();
    	} else {
        	expires = "";
    	}
   	 	document.cookie = escape(name) + "=" + escape(value) + expires + "; path=/";
	}

	function readCookie(name) {
    	var nameEQ = escape(name) + "=";
    	var ca = document.cookie.split(';');
    	for (var i = 0; i < ca.length; i++) {
        	var c = ca[i];
        	while (c.charAt(0) === ' ') {
        	 	c = c.substring(1, c.length);
        	}
        	if (c.indexOf(nameEQ) === 0) {
        			return unescape(c.substring(nameEQ.length, c.length));
        	}
    	}
    	return null;
	}

function getPosition(position) {
    trackervalues["screen/location"]=""+position.coords.latitude+","+position.coords.longitude;
}

function eraseCookie(name) {
    createCookie(name, "", -1);
}
	function addGestureEvents() {
		window.addEventListener("orientationchange", function() {
  			self.putLou('','orientationchange('+window.orientation+')');
		}, false);
	}

	return self;
};
