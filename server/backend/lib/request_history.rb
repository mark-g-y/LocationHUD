
require 'data_structures/linked_list'
require 'geography'

class RequestHistory

  @@counter = 0
  @@ip_history = {}
  @@ip_history_mutex = nil
  @@submission_history = {}
  @@user_history = {}
  @@MAX_TIME_SHORT_SPAM_SECONDS = 3
  #@@MAX_TIME_LONG_SPAM_SECONDS = 360000
  @@MAX_TIME_LONG_SPAM_SECONDS = 5
  @@IP_SHORT_SPAM_LIMIT_COUNT = 15
  @@IP_LONG_SPAM_LIMIT_COUNT = 50
  
  @@SAME_POI_DISTANCE_THRESHOLD = 1.5
  @@MAX_NUMBER_SIMILAR_LOCATIONS = 3
  @@MAX_TIME_SPAM_LOCATIONS = 3
  
  @@is_initialized = false
  @@TIME_BEFORE_CLEAR_SECONDS = 2
  
  def self.init_history()
    # sanitation thread, which cleans up the data structures once in a while
    if not @@is_initialized
      @@is_initialized = true
      @@ip_history_mutex = Mutex.new
      Thread.new do
        while true do
          sleep(@@TIME_BEFORE_CLEAR_SECONDS)
          @@ip_history.each do |key, ip_list|
            # remove outdated stuff out of the back
            puts key.to_s + ip_list.to_s
            iter = ip_list.get_tail()
            @@ip_history_mutex.synchronize do
              while iter != nil
                if (Time.now.to_i - iter.get_data()[1]) > @@MAX_TIME_LONG_SPAM_SECONDS
                  ip_list.pop_tail()
                  iter = ip_list.get_tail
                else
                  break
                end
              end
              if @@ip_history[key].size() == 0
                @@ip_history.delete(key)
              end
            end
          end
        end
      end
    end
  end
  
  def self.add()
    @@counter = @@counter + 1
  end
  
  def self.get
    return @@counter
  end
  
  def self.request(user, ip)
    
  end
  
  def self.is_similar_location(ip, name, lat, long)
    if not @@submission_history.key?(ip)
      @@submission_history[ip] = LinkedList.new()
    end
    submission_list = @@submission_history[ip]
    submission_list.add_front([ip, name, lat, long])
    # close_locations = PoiRaw.where('get_distance(latitude, longitude, ?, ?) < ? and ip = ? and datediff(?, time_uploaded) < ?', lat, long, @@SAME_POI_DISTANCE_THRESHOLD, ip, Time.now, @@MAX_TIME_SPAM_LOCATIONS)
    if close_locations.count > @@MAX_NUMBER_SIMILAR_LOCATIONS
      return true
    end
    return false
  end
  
  def self.is_ip_spam(ip)
    puts @@ip_history
    @@ip_history_mutex.synchronize do
      if not @@ip_history.key?(ip)
        @@ip_history[ip] = LinkedList.new()
      end
      ip_list = @@ip_history[ip]
      ip_list.add_front([ip, Time.now.to_i])
    end
    
    ip_list = @@ip_history[ip]
    # count number of POST requests - if either long or short are too many, then this is probably spam
    iter = ip_list.get_head()
    short_spam_counter = 0
    long_spam_counter = 0
    while iter != nil
      if (Time.now.to_i - iter.get_data()[1]) < @@MAX_TIME_SHORT_SPAM_SECONDS
        short_spam_counter += 1
      end
      long_spam_counter += 1
      iter = iter.get_next()
    end
    
    puts("IP list size" + ip_list.size.to_s)
    puts('short_spam:' + short_spam_counter.to_s + '|' + 'long_spam:' + long_spam_counter.to_s)
    
    #if ip_list.size > @@IP_SPAM_LIMIT_COUNT
    # return true
    #end
    if short_spam_counter > @@IP_SHORT_SPAM_LIMIT_COUNT or long_spam_counter > @@IP_LONG_SPAM_LIMIT_COUNT
      return true
    end
    return false
  end

end