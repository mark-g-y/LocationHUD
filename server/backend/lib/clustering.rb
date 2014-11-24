
require 'cluster'

class Clustering
	
	def initialize(pois)
		@pois = pois
		@clusters = []
		
		for poi in pois
			added_to_cluster = false
			for cluster in @clusters
				if cluster.is_in_cluster(poi)
					cluster.add(poi)
					added_to_cluster = true
					break
				end
			end
			if not added_to_cluster
				cluster = Cluster.new()
				cluster.add(poi)
				@clusters.push(cluster)
			end
		end
	end
	
	def get_clusters_as_models()
		models = []
		for cluster in @clusters
			models.push(cluster.get_most_common)
		end
		return models
	end
	
end