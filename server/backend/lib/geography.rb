
require 'my_math'

module Geography
	def get_distance(lat1, long1, lat2, long2)
		lat1 = MyMath.to_radians(lat1)
		lat2 = MyMath.to_radians(lat2)
		lat_diff = MyMath.to_radians(lat2 - lat1)
		long_diff = MyMath.to_radians(long2 - long1)
		
		a = Math::sin(lat_diff/2) * Math::sin(lat_diff/2) + Math::cos(lat1) * Math::cos(lat2) * Math::sin(long_diff/2) * Math::sin(long_diff/2)
		d = 2 * Math::atan2(Math::sqrt(a), Math::sqrt(1-a))
		return d * 6371
	end
end