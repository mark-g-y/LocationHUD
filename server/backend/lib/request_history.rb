
require 'data_structures/linked_list'
require 'geography'

class RequestHistory

	@@counter = 0
	@@ip_history = {}
	@@user_history = {}
	@@MAX_TIME_SPAM_SECONDS = 10
	@@IP_SPAM_LIMIT_COUNT = 10
	
	@@SAME_POI_DISTANCE_THRESHOLD = 1.5
	@@MAX_NUMBER_SIMILAR_LOCATIONS = 3
	@@MAX_TIME_SPAM_LOCATIONS = 3
	
	def self.add()
		@@counter = @@counter + 1
	end
	
	def self.get
		return @@counter
	end
	
	def self.request(user, ip)
		
	end
	
	def self.is_similar_location(ip, lat, long)
		close_locations = PoiRaw.where('get_distance(latitude, longitude, ?, ?) < ? and ip = ? and datediff(?, time_uploaded) < ?', lat, long, @@SAME_POI_DISTANCE_THRESHOLD, ip, Time.now, @@MAX_TIME_SPAM_LOCATIONS)
		if close_locations.count > @@MAX_NUMBER_SIMILAR_LOCATIONS
			return true
		end
		return false
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
				ip_list.pop_tail()
				iter = ip_list.get_tail()
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