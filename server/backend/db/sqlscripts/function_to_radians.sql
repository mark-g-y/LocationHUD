
drop function if exists to_radians;

delimiter $$
create function to_radians(angle double) returns double
	deterministic
begin
	return angle * pi() / 180;
end