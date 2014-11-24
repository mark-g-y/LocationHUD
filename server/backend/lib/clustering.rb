
class Clustering

	@@SAME_WORD_DIFF_TOLERANCE = 2
	
	def initialize(pois)
		@pois = pois
	end
	
	def self.get_name_difference(name1, name2)
		name1 = (' ' + name1).each_char.to_a
		name2 = (' ' + name2).each_char.to_a
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