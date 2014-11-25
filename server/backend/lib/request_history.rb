
require 'data_structures/linked_list'

class RequestHistory

	@@counter = 0
	@@ip_history = {}
	@@user_history = {}
	@@MAX_TIME_SPAM_SECONDS = 30
	@@IP_SPAM_LIMIT_COUNT = 10
	
	def self.add()
		@@counter = @@counter + 1
	end
	
	def self.get
		return @@counter
	end
	
	def self.request(user, ip)
		
	end
	
	def self.is_ip_spam(ip)
		if not @@ip_history.key?(ip)
			@@ip_history[ip] = LinkedList.new()
		end
		ip_list = @@ip_history[ip]
		ip_list.add_front([ip, Time.now.to_i])
		
		# remove outdated stuff out of the back
		iter = ip_list.get_tail()
		while iter != nil
			if (Time.now.to_i - iter.get_data()[1]) > @@MAX_TIME_SPAM_SECONDS
				iter = ip_list.pop_tail()
			else
				break
			end
		end
		
		puts("IP list size" + ip_list.size.to_s)
		
		if ip_list.size > @@IP_SPAM_LIMIT_COUNT
			return true
		end
		return false
	end

end