
Parse.Cloud.define("nearby_locations", function(request, response) {

	function Cluster(){
		this.name = "";
		this.names = {};
		this.most_popular_count = 0;
		this.add = function(name, result) {
			if (!(name in this.names)) {
				this.names[name] = [];
			}
			this.names[name].push(result);
			if (this.names[name].length > this.most_popular_count) {
				this.name = name;
				this.most_popular_count = this.names[name].length;
			}
		};
		this.get_levenshtein_distance = function(name1, name2) {
			var d = new Array(name1.length + 1);
			for (var i = 0; i < name1.length + 1; i++) {
				d[i] = new Array(name2.length + 1);
				d[i][0] = i;
			}
			for (var i = 0; i < name2.length + 1; i++) {
				d[0][i] = i;
			}
			for (var x = 1; x < name2.length + 1; x++) {
				for (var y = 1; y < name1.length + 1; y++) {
					if (name1[y] == name2[x]) {
						d[y][x] = d[y - 1][x - 1];
					} else {
						d[y][x] = Math.min(d[y - 1][x], d[y][x - 1], d[y - 1][x - 1]) + 1;
					}
				}
			}
			return d[name1.length][name2.length];
		};
		this.is_in_cluster = function(name) {
			if (this.get_levenshtein_distance(name.toLowerCase(), this.name.toLowerCase()) < 3) {
				return true;
			}
			return false;
		};
	}
		
	var query = new Parse.Query("Location");
	var current_location = new Parse.GeoPoint(request.params.latitude, request.params.longitude);
	query.withinKilometers("latlong", current_location, 100);
	query.find({
		success : function(results) {
			var groups = [];
			for (var i = 0; i < results.length; i++) {
				var result = results[i];
				if (groups.length == 0) {
					var group = new Cluster();
					group.add(result.get("name"), result);
					groups.push(group);
				} else {
					for (var m = 0; m < groups.length; m++) {
						if (groups[m].is_in_cluster(result.get("name"))) {
							groups[m].add(result.get("name"), result);
							break;
						} else if (m == groups.length - 1) {
							var group = new Cluster();
							group.add(result.get("name"), result);
							groups.push(group);
							break;
						}
					}
				}
			}
			response.success(groups);
		},
		error : function() {
			response.error("oh no!");
		}
	});
});