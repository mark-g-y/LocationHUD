
drop function if exists get_distance;

delimiter $$
create function get_distance(lat1 double, long1 double, lat2 double, long2 double) returns double
	deterministic
begin
	declare a double;
	set lat1 = to_radians(lat1);
	set lat2 = to_radians(lat2);
	set long1 = to_radians(long1);
	set long2 = to_radians(long2);
	set a = pow(sin((lat1-lat2)/2),2)+cos(lat1)*cos(lat2)*pow(sin(long1-long2),2);
	return 2 * atan2(sqrt(a),sqrt(1-a)) * 6371;
end