
require 'data_structures/linked_list'
require 'geography'

class RequestHistory

  @@request_history = nil

  @@MAX_TIME_SHORT_SPAM_SECONDS = 10
  @@MAX_TIME_LONG_SPAM_SECONDS = 3600
  @@IP_SHORT_SPAM_LIMIT_COUNT = 5
  @@IP_LONG_SPAM_LIMIT_COUNT = 25
  
  @@MAX_NUMBER_SIMILAR_LOCATIONS = 3
  @@MAX_TIME_SPAM_LOCATIONS = 3
  
  @@SECONDS_BEFORE_CLEAR = 60

  def self.get_instance()
    if @@request_history.nil?
      @@request_history = RequestHistory.new
    end
    return @@request_history
  end
  
  def initialize()
    # sanitation thread, which cleans up the data structures once in a while
    @ip_history = {}
    @submission_history = {}
    @ip_history_mutex = Mutex.new
    @submission_history_mutex = Mutex.new
    Thread.new do
      loop_remove_outdated_ip_history()
    end
    Thread.new do
      loop_remove_outdated_submissions_history()
    end
  end

  def loop_remove_outdated_ip_history()
    while true do
      sleep(@@SECONDS_BEFORE_CLEAR)
      @ip_history_mutex.lock
      ips = @ip_history.keys
      @ip_history_mutex.unlock

      for ip in ips
        @ip_history_mutex.lock
        ip_list = @ip_history[ip]
        iter = ip_list.get_tail()
        while iter != nil
          if (Time.now.to_i - iter.get_data()) > @@MAX_TIME_LONG_SPAM_SECONDS
            ip_list.pop_tail()
            iter = ip_list.get_tail
          else
            break
          end
        end
        if @ip_history[key].size() == 0
          @ip_history.delete(key)
        end
        @ip_history_mutex.unlock
      end
    end
  end

  def loop_remove_outdated_submissions_history()
    while true do
      sleep(@@SECONDS_BEFORE_CLEAR)

      @submission_history_mutex.lock
      ips = @submissions_history.keys
      @submission_history_mutex.unlock

      for ip in ips
        @submission_history_mutex.lock
        submission_list = @submission_history[ip]
        iter = submission_list.get_tail()
        while iter != nil
          if (Time.now.to_i - iter.get_data()[0]) > @@MAX_TIME_SPAM_LOCATIONS
            submission_list.pop_tail()
            iter = submission_list.get_tail
          else
            break
          end
        end
        if @submission_history[key].size() == 0
          @submission_history.delete(key)
        end
        @submission_history_mutex.unlock
      end
    end
  end

  def add_ip(ip)
    @ip_history_mutex.synchronize do
      if not @ip_history.key?(ip)
        @ip_history[ip] = LinkedList.new()
      end
      ip_list = @ip_history[ip]
      ip_list.add_front(Time.now.to_i)
    end
  end

  def add_submission(ip, name, lat, long)
    @submission_history_mutex.synchronize do
      if not @submission_history.key?(ip) then
        @submission_history[ip] = LinkedList.new()
      end
      submission_list = @submission_history[ip]
      submission_list.add_front([Time.now.to_i, name, lat, long])
    end
  end
  
  def is_similar_location(ip, name, lat, long)
    @submission_history_mutex.lock
    submission_list = @submission_history[ip]
    iter = submission_list.get_head()
    spam_counter = 0
    while iter != nil
      if (Time.now.to_i - iter.get_data()[0]) < @@MAX_TIME_SPAM_LOCATIONS
        submission = iter.get_data()
        if submission[1] == name or (submission[2] == lat and submission[3])
          spam_counter += 1
        end
      end
      iter = iter.get_next()
    end
    @submission_history_mutex.unlock
    
    if spam_counter > @@MAX_NUMBER_SIMILAR_LOCATIONS
      return true
    end
    
    return false
  end
  
  def is_ip_spam(ip)
    
    @ip_history_mutex.lock
    ip_list = @ip_history[ip]
    # count number of POST requests - if either long or short are too many, then this is probably spam
    iter = ip_list.get_head()
    short_spam_counter = 0
    long_spam_counter = 0
    while iter != nil
      if (Time.now.to_i - iter.get_data()) < @@MAX_TIME_SHORT_SPAM_SECONDS
        short_spam_counter += 1
      end
      long_spam_counter += 1
      iter = iter.get_next()
    end
    @ip_history_mutex.unlock
    
    if short_spam_counter > @@IP_SHORT_SPAM_LIMIT_COUNT or long_spam_counter > @@IP_LONG_SPAM_LIMIT_COUNT
      return true
    end
    return false
  end

end