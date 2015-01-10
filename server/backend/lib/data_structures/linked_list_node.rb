
class LinkedListNode

	def initialize(data)
		@data = data
		@next = nil
		@previous = nil
	end
	
	def get_data()
		return @data
	end
	
	def set_data(data)
		@data = data
	end
	
	def get_next()
		return @next
	end
	
	def set_next(n)
		@next = n
	end
	
	def get_previous()
		return @previous
	end
	
	def set_previous(previous)
		@previous = previous
	end

end