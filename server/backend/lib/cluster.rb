
class Cluster

	@@SAME_WORD_DIFF_TOLERANCE = 1
	@@SAME_CLUSTER_DIFF_TOLERANCE = 2

	def initialize()
		@pois = []
		@most_common = nil
	end
	
	def add(poi)
		@pois.push(poi)
		if @most_common.nil?
			@most_common = poi
		elsif poi['count'] > @most_common['count']
			@most_common = poi
		end
	end
	
	def get_most_common() 
		return @most_common
	end
	
	def is_in_cluster(poi)
		word_diff_with_most_common = get_name_difference(@most_common['title'], poi['title'])
		if word_diff_with_most_common > @@SAME_CLUSTER_DIFF_TOLERANCE
			# if this title isn't even remotely close to the title that represents the group, we assume that this doesn't belong here
			return false
		end
		
		# see if this is a good match for the title representing this group
		if word_diff_with_most_common <= @@SAME_WORD_DIFF_TOLERANCE
			return true
		end
		
		# otherwise, we try to see if the POI is close enough to anything else for a match
		for p in @pois
			if get_name_difference(p, poi['title']) <= SAME_WORD_DIFF_TOLERANCE
				return true
			end
		end
		
		return false
	end
	
	def get_name_difference(name1, name2)
		name1 = (' ' + name1.downcase).each_char.to_a
		name2 = (' ' + name2.downcase).each_char.to_a
		matrix = [[]]
		# fill the first row
		for i in (0 .. name2.size - 1)
			matrix[0].push(i)
		end
		
		# fill the first column
		for i in (1 .. name1.size - 1)
			matrix.push([0])
			matrix[i][0] = i
		end
		
		for i in (1 .. name1.size - 1)
			for j in (1 .. name2.size - 1)
				if name1[i] == name2[j]
					matrix[i][j] = matrix[i - 1][j - 1]
				else
					matrix[i][j] = [matrix[i - 1][j], matrix[i][j - 1], matrix[i - 1][j - 1]].min + 1
				end
			end
		end
		
		return matrix[name1.size - 1][name2.size - 1]
		
	end

end