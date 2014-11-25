
require_relative 'linked_list_node'

class LinkedList
	
	def initialize()
		@head = nil
		@tail = nil
		@size = 0
	end
	
	def add_end(data)
		@size += 1
		if @head == nil and @tail == nil
			add_first_node(data)
			return @tail
		end
		node = LinkedListNode(data)
		node.set_previous(@tail)
		@tail.set_next(node)
		@tail = node
		return @tail
	end
	
	def add_front(data)
		@size += 1
		if @head == nil and @tail == nil
			add_first_node(data)
			return @head
		end
		node = LinkedListNode.new(data)
		node.set_next(@head)
		@head.set_previous(node)
		@head = node
		return @head
	end
	
	def add_first_node(data)
		node = LinkedListNode.new(data)
		node.set_next(nil)
		node.set_previous(nil)
		@head = node
		@tail = node
	end
	
	def get_head()
		return @head
	end
	
	def pop_head()
		@size -= 1
		if @head.get_next() == nil
			temp_head = @head
			@head = nil
			@tail = nil
			return temp_head
		end
		temp_head = @head
		@head.get_next().set_previous(nil)
		@head = @head.get_next()
		return temp_head
	end
	
	def get_tail()
		return @tail
	end
	
	def pop_tail()
		@size -= 1
		if @tail.get_previous == nil
			temp_tail = @tail
			@head = nil
			@tail = nil
			return temp_tail
		end
		temp_tail = @tail
		@tail.get_previous().set_next(nil)
		@tail = @tail.get_previous()
		return temp_tail
	end
	
	def size()
		return @size
	end
	
end