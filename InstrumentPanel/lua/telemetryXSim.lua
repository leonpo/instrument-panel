---------------------------------------------------------------------------------------------------
-- Export start 
---------------------------------------------------------------------------------------------------

f_xsim =

{
Start=function(self) 
	package.path = package.path..";.\\LuaSocket\\?.lua"
	package.cpath = package.cpath..";.\\LuaSocket\\?.dll"
	socket = require("socket")
	
	local my_init = socket.protect(function()
		-- export telemetry to x-sim extractor
		host_xsim = host_xsim or "localhost"
		port_xsim= port_xsim or 8080
		c_xsim = socket.try(socket.connect(host_xsim, port_xsim)) -- connect to the listener socket
		c_xsim:setoption("tcp-nodelay",true) -- set immediate transmission mode
		c_xsim:settimeout(.01)	
	end)
	my_init()	
end,


AfterNextFrame=function(self)
	local t = LoGetModelTime()
	local altBar = LoGetAltitudeAboveSeaLevel() * 3.28084
	local altRad = LoGetAltitudeAboveGroundLevel()
	local pitch, bank, yaw = LoGetADIPitchBankYaw()
	local angle = 0
	local accel = LoGetAccelerationUnits()
	local user1	 = 1
	local user2	 = 2
	local user3	 = 3
	local user4	 = 4
	local user5	 = 5
	local user6	 = 6

	
	-- reduce forces on ground
	local speed = LoGetTrueAirSpeed()
	if speed < 30 then
		accel.x = accel.x * 0.2
		accel.y = accel.y * 0.2
		accel.z = accel.z * 0.2
	end
	
	local my_send = socket.protect(function()
		if c_xsim then
			socket.try(c_xsim:send(string.format("%.3f %.2f %.2f %.2f %.2f %.2f %.2f %.0f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f \n", t, altBar, altRad, pitch*1000.0, bank*1000.0, yaw*1000.0, accel.x*1000.0, angle*1000, accel.y*1000.0, accel.z*1000.0, accel.x*1000.0, (accel.y-1)*1000.0, accel.z*1000.0, user4, user5, user6, 7)))
		end
	end)
	my_send()
		
end,


Stop=function(self)
	local my_close = socket.protect(function()
		if c_xsim then
			c_xsim:close()
		end	
	end)
	my_close()
end
}


-- =============
-- Overload
-- =============

-- Works once just before mission start.
do
	local PrevLuaExportStart=LuaExportStart
	LuaExportStart=function()
		f_xsim:Start()
		if PrevLuaExportStart then
			PrevLuaExportStart()
		end
	end
end

-- Works just after every simulation frame.
do
	local PrevLuaExportAfterNextFrame=LuaExportAfterNextFrame
	LuaExportAfterNextFrame=function()
		f_xsim:AfterNextFrame()
		if PrevLuaExportAfterNextFrame then
			PrevLuaExportAfterNextFrame()
		end
	end
end

-- Works once just after mission stop.
do
	local PrevLuaExportStop=LuaExportStop
	LuaExportStop=function()
		f_xsim:Stop()
		if PrevLuaExportStop then
			PrevLuaExportStop()
		end
	end
end
