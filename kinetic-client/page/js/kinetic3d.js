var BYTES_PER_KB = 1024;
var preNodesAvailability;
var currNodesAvailability;
var currentNode;

var mode;
var target;
var selectedNodes = [];
var inited = false;

var nodeCounts = 0;

var camera, scene, renderer;
var controls;
var objects = [];
var targets = {
	table : [],
	sphere : [],
	helix : [],
	grid : []
};

// entrance
$(function() {
	init();

	$("#nodeInfo").hide();
	$("#temperature_title").hide();

	$("#nodeInfo").click(function() {
		$("#nodeInfo").slideUp();
		currentNode = "";
	});

	autoUpdate();
});

function init() {
	$.getJSON("/servlet/KineticAdminServlet?action=listNodesAbstract",
					function(nodes) {
						var length = nodes.length;
						if (length == nodeCounts) {
							return;
						}

						nodeCounts = length;
						preNodesAvailability = nodes;
						currNodesAvailability = nodes;

						TWEEN.removeAll();
						TWEEN.update();

						if (controls != null) {
							controls.update();
						}

						if (renderer != null && scene != null && camera != null) {
							for ( var i = 0; i < objects.length; i++) {
								scene.remove(objects[i]);
							}
							camera.updateProjectionMatrix();
							renderer.render(scene, camera);
						}

						var tableZoom = 1200;
						var tableXOffset = 1730;
						var tableYOffset = 900;
						var tableXGap = 380;
						var tableYGap = 100;
						var tableTows = 16;

						var helixYOffset = 450;
						var helixXZZoom = 600;
						var helixPhiRatio = 0.575;

						if (length > 0 && length <= 128) {
							tableZoom = 1200;
							tableXOffset = 1730;
							tableXGap = 380;
							tableYGap = 100;
							tableTows = 16;

							helixYOffset = 450;
							helixXZZoom = 600;
							helixPhiRatio = 0.575;
						} else if (length <= 512) {
							if (BrowserDetect.browser == "Firefox") {
								tableZoom = 2300;
							} else {
								tableZoom = 2100;
							}

							tableXOffset = 3200;
							tableYOffset = 1700;
							tableXGap = 380;
							tableYGap = 100;
							tableTows = 32;

							helixYOffset = 1200;
							helixXZZoom = 1200;
							helixPhiRatio = 0.725;
						} else if (length <= 1024) {
							tableZoom = 3600;
							tableXOffset = 4500;
							tableYOffset = 2500;
							tableXGap = 380;
							tableYGap = 100;
							tableTows = 48;

							helixYOffset = 2400;
							helixXZZoom = 2400;
							helixPhiRatio = 0.850;
						} else if (length <= 2048) {
							tableZoom = 4800;
							tableXOffset = 6400;
							tableYOffset = 3300;
							tableXGap = 380;
							tableYGap = 100;
							tableTows = 64;

							helixYOffset = 3200;
							helixXZZoom = 3200;
							helixPhiRatio = 0.875;
						} else {
							tableZoom = 4800;
							tableXOffset = 6400;
							tableYOffset = 3300;
							tableXGap = 380;
							tableYGap = 100;
							tableTows = 64;

							helixYOffset = 3200;
							helixXZZoom = 3200;
							helixPhiRatio = 0.875;
						}

						var table = [];

						var j = 0;
						for ( var i = 0; i < length; i++) {
							table[j++] = nodes[i].host;
							table[j++] = nodes[i].port;
							table[j++] = nodes[i].status;
							table[j++] = Math.floor(i / tableTows) + 1;
							table[j++] = i % tableTows + 1;
						}

						init();
						animate();

						function init() {

							camera = new THREE.PerspectiveCamera(75,
									window.innerWidth / window.innerHeight, 1,
									5000);
							camera.position.z = tableZoom;

							scene = new THREE.Scene();

							// table
							for ( var i = 0; i < table.length; i += 5) {

								var element = document.createElement('div');
								element.className = 'element';
								element.id = table[i] + ':' + table[i + 1];
								if (table[i + 2] == 1) {
									element.style.backgroundColor = 'rgba(231, 25, 17,' + ( Math.random() * 0.5 + 0.25 ) + ')';
								} else {
									element.style.backgroundColor = 'rgba(0,127,127,' + ( Math.random() * 0.5 + 0.25 ) + ')';
								}

								var symbol = document.createElement('div');
								symbol.className = 'symbol';
								symbol.id = 'element' + i;
								symbol.textContent = table[i] + ':'
										+ table[i + 1];
								element.appendChild(symbol);

								element.ondblclick = function() {

										var node = this
												.getElementsByClassName("symbol")[0].textContent;
										currentNode = node;
										$("#nodeInfo").center();
										showNodeInfo(node);
										$("#nodeInfo").show();
								};

								var object = new THREE.CSS3DObject(element);
								object.position.x = Math.random() * 4000 - 2000;
								object.position.y = Math.random() * 4000 - 2000;
								object.position.z = Math.random() * 4000 - 2000;
								scene.add(object);

								objects.push(object);

								var object = new THREE.Object3D();
								object.position.x = (table[i + 3] * tableXGap)
										- tableXOffset;
								object.position.y = -(table[i + 4] * tableYGap)
										+ tableYOffset;

								targets.table.push(object);

							}

							// sphere

							var vector = new THREE.Vector3();

							for ( var i = 0, l = objects.length; i < l; i++) {

								var phi = Math.acos(-1 + (2 * i) / l);
								var theta = Math.sqrt(l * Math.PI) * phi;

								var object = new THREE.Object3D();

								object.position.x = 800 * Math.cos(theta)
										* Math.sin(phi);
								object.position.y = 800 * Math.sin(theta)
										* Math.sin(phi);
								object.position.z = 800 * Math.cos(phi);

								vector.copy(object.position).multiplyScalar(2);

								object.lookAt(vector);

								targets.sphere.push(object);

							}

							// helix

							var vector = new THREE.Vector3();

							for ( var i = 0, l = objects.length; i < l; i++) {

								var phi = i * helixPhiRatio + Math.PI;

								var object = new THREE.Object3D();

								object.position.x = helixXZZoom * Math.sin(phi);
								object.position.y = -(i * 8) + helixYOffset;
								object.position.z = helixXZZoom * Math.cos(phi);

								vector.x = object.position.x * 2;
								vector.y = object.position.y;
								vector.z = object.position.z * 2;

								object.lookAt(vector);

								targets.helix.push(object);

							}

							// grid

							for ( var i = 0; i < objects.length; i++) {

								var object = new THREE.Object3D();

								object.position.x = ((i % 5) * 400) - 800;
								object.position.y = (-(Math.floor(i / 5) % 5) * 400) + 800;
								object.position.z = (Math.floor(i / 25)) * 1000 - 2000;

								targets.grid.push(object);

							}
							
							renderer = new THREE.CSS3DRenderer();
							renderer.setSize(window.innerWidth,
									window.innerHeight);
							renderer.domElement.style.position = 'absolute';
							document.getElementById('container').appendChild(
									renderer.domElement);

							controls = new THREE.TrackballControls(camera,
									renderer.domElement);
							controls.rotateSpeed = 0.5;
							controls.addEventListener('change', render);

							var button = document.getElementById('table');
							button.addEventListener('click', function(event) {

								transform(targets.table, 2000);

							}, false);

							var button = document.getElementById('sphere');
							button.addEventListener('click', function(event) {

								transform(targets.sphere, 2000);

							}, false);

							var button = document.getElementById('helix');
							button.addEventListener('click', function(event) {

								transform(targets.helix, 2000);

							}, false);

							var button = document.getElementById('grid');
							button.addEventListener('click', function(event) {

								transform(targets.grid, 2000);

							}, false);

							transform(targets.table, 3000);
							
							window.addEventListener('resize', onWindowResize,
									false);

							inited = true;
						}

						function transform(targets, duration) {

							TWEEN.removeAll();

							for ( var i = 0; i < objects.length; i++) {

								var object = objects[i];
								var target = targets[i];

								if (inited) {
									new TWEEN.Tween(object.position).to({
										x : target.position.x,
										y : target.position.y,
										z : target.position.z
									}, 0)
									.easing(TWEEN.Easing.Exponential.InOut)
											.start();

									new TWEEN.Tween(object.rotation).to({
										x : target.rotation.x,
										y : target.rotation.y,
										z : target.rotation.z
									}, 0)
									.easing(TWEEN.Easing.Exponential.InOut)
											.start();
								} else {
									new TWEEN.Tween(object.position)
											.to(
													{
														x : target.position.x,
														y : target.position.y,
														z : target.position.z
													},
													Math.random() * duration
															+ duration)
											.easing(
													TWEEN.Easing.Exponential.InOut)
											.start();

									new TWEEN.Tween(object.rotation)
											.to(
													{
														x : target.rotation.x,
														y : target.rotation.y,
														z : target.rotation.z
													},
													Math.random() * duration
															+ duration)
											.easing(
													TWEEN.Easing.Exponential.InOut)
											.start();
								}
							}

							new TWEEN.Tween(this).to({}, duration * 2)
									.onUpdate(render).start();

						}

						function onWindowResize() {

							camera.aspect = window.innerWidth
									/ window.innerHeight;
							camera.updateProjectionMatrix();

							renderer.setSize(window.innerWidth,
									window.innerHeight);

							render();

						}

						function animate() {

							requestAnimationFrame(animate);

							TWEEN.update();
							controls.update();

						}

						function render() {

							renderer.render(scene, camera);

						}
					});
}

function autoUpdate() {
	setInterval(function() {
		init();
	}, 8000);

	setInterval(
			function() {
				$
						.getJSON(
								"/servlet/KineticAdminServlet?action=listNodesAbstract",
								function(nodes) {
									currNodesAvailability = nodes;
									var key;
									for ( var i = 0; i < currNodesAvailability.length; i++) {
										key = currNodesAvailability[i].host
												+ ":"
												+ currNodesAvailability[i].port;
										if (currNodesAvailability[i].status == '1'
												&& preNodesAvailability[i].status == '0') {
											document.getElementById(key).style.backgroundColor = 'rgba(231, 25, 17,0.8)';
										} else if (currNodesAvailability[i].status == '0'
												&& preNodesAvailability[i].status == '1') {
											document.getElementById(key).style.backgroundColor = 'rgba(0,127,127,0.8)';
										}

									}
									preNodesAvailability = currNodesAvailability;
								});
			}, 5000);

	setInterval(function() {
        if (currentNode != undefined && currentNode != "") {
            $.getJSON("servlet/KineticAdminServlet?action=getNodeDetails&node="
					+ currentNode, function(data) {
                //update capacity
                renderCapacity(data);

                //update temperature
                renderTemperature(data);

                //update utilization
                renderUtilizations(data);

                //update counts
                renderCounters(data);
            });
        }
	}, 5000);
};

jQuery.fn.center = function() {
	this.css("position", "absolute");
	this.css("top", Math.max(0,
			(($(window).height() - $(this).outerHeight()) / 2)
					+ $(window).scrollTop())
			+ "px");
	this.css("left", Math.max(0,
			(($(window).width() - $(this).outerWidth()) / 2)
					+ $(window).scrollLeft())
			+ "px");
	return this;
};

function showNodeInfo(node) {
	$("#nodeInfoContainer").remove();
	$
			.getJSON(
					"/servlet/KineticAdminServlet?action=getNodeDetails&node=" + node,
					function(data) {
						$("#nodeInfo").append(
								"<div id='nodeInfoContainer'" + ">"
										+ "<a class='button'>" + data.host
										+ ":" + data.port + "  (tlsPort: "
										+ data.tlsPort + ")" + "</a>"
										+ "<div id='capacity' class='capacity'></div>"
										+ "<div id='temperature' class='temperature'>"
										+ "<text id='temperature_title'>Temperature(℃)</text>"
										+ "<div id='temperature_hda' class='temperature_hda'></div>"
										+ "<div id='temperature_cpu' class='temperature_cpu'></div>"
										+ "</div>"
										+ "<div id='utilizations' class='utilizations'></div>"
										+ "<div id='counters' class='counters'></div>"
										+ "</div>");
						
						$("#temperature_title").hide();

						var key;
						for ( var i = 0; i < currNodesAvailability.length; i++) {
							key = currNodesAvailability[i].host + ":"
									+ currNodesAvailability[i].port;
							if (node == key) {
								if (data.status == '1')
								{
									currNodesAvailability[i].status == '1';
									document.getElementById(key).style.backgroundColor = 'rgba(231, 25, 17,0.8)';
								}
								
								// node is unreachable
								if (currNodesAvailability[i].status == '1' || data.status == '1') {
									$("#capacity").remove();
									$("#temperature").remove();
									$("#utilizations").remove();
									$("#counters").remove();
									$("#nodeInfoContainer")
											.append(
													"<div style='text-align: center;'><img src='page/img/Not_available_icon.png' style='margin-top: 50px'/></div>");
								} else {
									setTimeout(
											function() {
												renderCapacity(data);
												renderTemperature(data);
												renderUtilizations(data);
												renderCounters(data);
											}, 200);
								}
								break;
							}
						}
					});
}

function renderCapacity(nodeInfo) {
    var used = nodeInfo.capacity.portionFull * nodeInfo.capacity.nominalCapacityInBytes;
    var remaining = nodeInfo.capacity.nominalCapacityInBytes - used;
    var freePercentage = 1 - nodeInfo.capacity.portionFull;
	
    if ($('#capacity').length > 0) {
        $.getScript('https://www.google.com/jsapi', function (data, textStatus) {
            google.load('visualization', '1.0', { 'packages': ['corechart'], 'callback': function () {
                var data = google.visualization.arrayToDataTable([
                    ['Capacity', 'Current'],
                    ['Remaining', remaining],
                    ['Used', used]
                ]);

                var options = {
                    title: 'Capacity (B)',
                    is3D: true,
                    legend : {alignment : 'center', position: 'bottom'}
                };

                var chart = new google.visualization.PieChart(document.getElementById('capacity'));
                chart.draw(data, options);
            }
            });
        });
    }
}

function renderTemperature(nodeInfo) {
	$("#temperature_title").show();
	
    if ($('#temperature_hda').length > 0) {
        $.getScript('https://www.google.com/jsapi', function (data, textStatus) {
            google.load('visualization', '1.0', { 'packages': ['gauge'], 'callback': function () {
                var tMax = nodeInfo.temperatures[0].max;
                var tMin = nodeInfo.temperatures[0].min;
                var tTarget = nodeInfo.temperatures[0].target;
            	
            	var data = google.visualization.arrayToDataTable([
                    ['Label', 'Value'],
                    ['HDA', nodeInfo.temperatures[0].current]
                ]);

                var options = {
                    width: 125, height: 100,
                    redFrom: tTarget, redTo: tMax,
                    yellowFrom:tMin, yellowTo: tTarget,
                    greenFrom: 0, greenTo: tMin,
                    minorTicks: 5
                };

                var chart = new google.visualization.Gauge(document.getElementById('temperature_hda'));
                chart.draw(data, options);

            	$('#temperature_hda_comments').remove();
            	$('#temperature_hda').append("<text id='temperature_hda_comments'>Min:&nbsp;"
            			+ tMin + "℃<br>Tgt:&nbsp;"
            			+ tTarget + "℃<br>Max:&nbsp;"
            			+ tMax + "℃</text>");
            }
            });
        });
    }
    
    if ($('#temperature_cpu').length > 0) {
        $.getScript('https://www.google.com/jsapi', function (data, textStatus) {
            google.load('visualization', '1.0', { 'packages': ['gauge'], 'callback': function () {
                var tMax = nodeInfo.temperatures[1].max;
                var tMin = nodeInfo.temperatures[1].min;
                var tTarget = nodeInfo.temperatures[1].target;
            	
            	var data = google.visualization.arrayToDataTable([
                    ['Label', 'Value'],
                    ['CPU', nodeInfo.temperatures[1].current]
                ]);

                var options = {
                    width: 125, height: 100,
                    redFrom: tTarget, redTo: tMax,
                    yellowFrom:tMin, yellowTo: tTarget,
                    greenFrom: 0, greenTo: tMin,
                    minorTicks: 5
                };

                var chart = new google.visualization.Gauge(document.getElementById('temperature_cpu'));
                chart.draw(data, options);
                
            	$('#temperature_cpu_comments').remove();
            	$('#temperature_cpu').append("<text id='temperature_cpu_comments'>Min:&nbsp;"
            			+ tMin + "℃<br>Tgt:&nbsp;"
            			+ tTarget + "℃<br>Max:&nbsp;"
            			+ tMax + "℃</text>");
            }
            });
        });
    }
}

function renderUtilizations(nodeInfo) {
    if ($('#utilizations').length > 0) {
        $.getScript('https://www.google.com/jsapi', function (data, textStatus) {
            google.load("visualization", "1.0", { packages: ["corechart"], "callback": function () {                
                var data = google.visualization.arrayToDataTable([
                                                                  ["Type", "Utilizations (%)"],
                                                                  ["HDA", Math.floor(nodeInfo.utilizations[0].utility * 100)],
                                                                  ["EN0", Math.floor(nodeInfo.utilizations[1].utility * 100)],
                                                                  ["EN1", Math.floor(nodeInfo.utilizations[2].utility * 100)],
                                                                  ["CPU", Math.floor(nodeInfo.utilizations[3].utility * 100)],
                                                              ]);

                var view = new google.visualization.DataView(data);
                view.setColumns([0, 1,
                    { calc: "stringify",
                        sourceColumn: 1,
                        type: "string",
                        role: "annotation" }]);

                var options = {
                    title: "Utilizations (%)",
                    width: 250,
                    height: 200,
                    bar: {groupWidth: "65%"},
                    legend: { position: "none" },
                    hAxis: { maxValue: 100, minValue: 0}
                };
                var chart = new google.visualization.ColumnChart(document.getElementById("utilizations"));
                chart.draw(view, options);
            }
            });
        });
    }
}

function renderCounters(nodeInfo) {
    var operationCounters = {};
    var bytesCounters = {};
    var counter;
    for (var i=0; i<nodeInfo.statistics.length; i++)
    {
        counter = nodeInfo.statistics[i];
        if (counter.messageType == 'GET')
        {
            operationCounters.GET = counter.count;
            bytesCounters.GET = counter.bytes;
        }else if (counter.messageType == 'PUT')
        {
            operationCounters.PUT = counter.count;
            bytesCounters.PUT = counter.bytes;
        }else if (counter.messageType == 'DELETE')
        {
            operationCounters.DELETE = counter.count;
            bytesCounters.DELETE = counter.bytes;
        }else if (counter.messageType == 'GETNEXT')
        {
            operationCounters.GETNEXT = counter.count;
            bytesCounters.GETNEXT = counter.bytes;
        }else if (counter.messageType == 'GETPREVIOUS')
        {
            operationCounters.GETPREVIOUS = counter.count;
            bytesCounters.GETPREVIOUS = counter.bytes;
        }else if (counter.messageType == 'GETKEYRANGE')
        {
            operationCounters.GETKEYRANGE = counter.count;
            bytesCounters.GETKEYRANGE = counter.bytes;
        }else if (counter.messageType == 'GETVERSION')
        {
            operationCounters.GETVERSION = counter.count;
            bytesCounters.GETVERSION = counter.bytes;
        }else if (counter.messageType == 'SETUP')
        {
            operationCounters.SETUP = counter.count;
            bytesCounters.SETUP = counter.bytes;
        }else if (counter.messageType == 'GETLOG')
        {
            operationCounters.GETLOG = counter.count;
            bytesCounters.GETLOG = counter.bytes;
        }else if (counter.messageType == 'SECURITY')
        {
            operationCounters.SECURITY = counter.count;
            bytesCounters.SECURITY = counter.bytes;
        }else if (counter.messageType == 'PEER2PEERPUSH')
        {
            operationCounters.PEER2PEERPUSH = counter.count;
            bytesCounters.PEER2PEERPUSH = counter.bytes;
        }
    }
    if ($('#counters').length > 0) {
        $.getScript('https://www.google.com/jsapi', function (data, textStatus) {
            google.load('visualization', '1.0', { 'packages': ['corechart'], 'callback': function () {
                var data = google.visualization.arrayToDataTable([
                    ['Type', 'Operations (times)', 'Bytes (KB)'],
                    ['GET',  operationCounters.GET,      bytesCounters.GET/BYTES_PER_KB],
                    ['PUT',  operationCounters.PUT,      bytesCounters.PUT/BYTES_PER_KB],
                    ['DELETE',  operationCounters.DELETE,       bytesCounters.DELETE/BYTES_PER_KB],
                    ['GETNEXT',  operationCounters.GETNEXT,      bytesCounters.GETNEXT/BYTES_PER_KB],
                    ['GETPREVIOUS',  operationCounters.GETPREVIOUS,      bytesCounters.GETPREVIOUS/BYTES_PER_KB],
                    ['GETKEYRANGE',  operationCounters.GETKEYRANGE,      bytesCounters.GETKEYRANGE/BYTES_PER_KB],
                    ['GETVERSION',  operationCounters.GETVERSION,      bytesCounters.GETVERSION/BYTES_PER_KB],
                    ['SETUP',  operationCounters.SETUP,      bytesCounters.SETUP/BYTES_PER_KB],
                    ['GETLOG',  operationCounters.GETLOG,      bytesCounters.GETLOG/BYTES_PER_KB],
                    ['SECURITY',  operationCounters.SECURITY,      bytesCounters.SECURITY/BYTES_PER_KB],
                    ['PEER2PEERPUSH',  operationCounters.PEER2PEERPUSH,      bytesCounters.PEER2PEERPUSH/BYTES_PER_KB]
                ]);

                var options = {
                    title: 'Operation and Bytes Counters',
                    legend : {alignment : 'center', position: 'bottom'},
                    hAxis: {minTextSpacing: 6, textStyle: {fontSize: 8}},
                    chartArea: {height: '50%'}
                };

                var chart = new google.visualization.ColumnChart(document.getElementById("counters"));
                chart.draw(data, options);
            }
            });
        });
    }
}

var BrowserDetect = {
	init : function() {
		this.browser = this.searchString(this.dataBrowser) || "Other";
		this.version = this.searchVersion(navigator.userAgent)
				|| this.searchVersion(navigator.appVersion) || "Unknown";
	},

	searchString : function(data) {
		for ( var i = 0; i < data.length; i++) {
			var dataString = data[i].string;
			this.versionSearchString = data[i].subString;

			if (dataString.indexOf(data[i].subString) != -1) {
				return data[i].identity;
			}
		}
	},

	searchVersion : function(dataString) {
		var index = dataString.indexOf(this.versionSearchString);
		if (index == -1)
			return;
		return parseFloat(dataString.substring(index
				+ this.versionSearchString.length + 1));
	},

	dataBrowser : [ {
		string : navigator.userAgent,
		subString : "Chrome",
		identity : "Chrome"
	}, {
		string : navigator.userAgent,
		subString : "MSIE",
		identity : "Explorer"
	}, {
		string : navigator.userAgent,
		subString : "Firefox",
		identity : "Firefox"
	}, {
		string : navigator.userAgent,
		subString : "Safari",
		identity : "Safari"
	}, {
		string : navigator.userAgent,
		subString : "Opera",
		identity : "Opera"
	} ]

};
BrowserDetect.init();
