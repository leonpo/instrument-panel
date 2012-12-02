---------------------------------------------------------------------------------------------------
-- Export start 
---------------------------------------------------------------------------------------------------

Myfunction =

{
Start=function(self) 
	package.path = package.path..";.\\LuaSocket\\?.lua"
	package.cpath = package.cpath..";.\\LuaSocket\\?.dll"
	socket = require("socket")
	
	my_init = socket.protect(function()
		-- export telemetry to x-sim extractor
		host1 = host1 or "localhost"
		port1 = port1 or 8080
		c1 = socket.try(socket.connect(host1, port1)) -- connect to the listener socket
		c1:setoption("tcp-nodelay",true) -- set immediate transmission mode
		c1:settimeout(.01)	
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
	
	my_send = socket.protect(function()
		if c1 then
			socket.try(c1:send(string.format("%.3f %.2f %.2f %.2f %.2f %.2f %.2f %.0f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f \n", t, altRad, altBar, pitch*1000.0, bank*1000.0, yaw*1000.0, accel.x*1000.0, angle*1000, accel.y*1000.0, accel.z*1000.0, accel.x*1000.0, (accel.y-1)*1000.0, accel.z*1000.0, user4, user5, user6, 7)))
		end
	end)
	my_send()
		
end,


Stop=function(self)
	my_close = socket.protect(function()
		if c1 then
			c1:close()
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
		Myfunction:Start()
		if PrevLuaExportStart then
			PrevLuaExportStart()
		end
	end
end

-- Works just after every simulation frame.
do
	local PrevLuaExportAfterNextFrame=LuaExportAfterNextFrame
	LuaExportAfterNextFrame=function()
		Myfunction:AfterNextFrame()
		if PrevLuaExportAfterNextFrame then
			PrevLuaExportAfterNextFrame()
		end
	end
end

-- Works once just after mission stop.
do
	local PrevLuaExportStop=LuaExportStop
	LuaExportStop=function()
		Myfunction:Stop()
		if PrevLuaExportStop then
			PrevLuaExportStop()
		end
	end
end
