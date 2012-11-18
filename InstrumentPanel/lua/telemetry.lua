---------------------------------------------------------------------------------------------------
-- Export start 
---------------------------------------------------------------------------------------------------
function LuaExportStart()
	package.path = package.path..";.\\LuaSocket\\?.lua"
	package.cpath = package.cpath..";.\\LuaSocket\\?.dll"
	socket = require("socket")
	host1 = host1 or "localhost"
	port1 = port1 or 8080
	c1 = socket.try(socket.connect(host1, port1)) -- connect to the listener socket
	c1:setoption("tcp-nodelay",true) -- set immediate transmission mode
	c1:settimeout(.01)	
	-- export telemetry to panel
	host2 = host2 or "10.0.0.9"
	port2 = port2 or 6000
	c2 = socket.try(socket.connect(host2, port2)) -- connect to the listener socket
	c2:setoption("tcp-nodelay",true) -- set immediate transmission mode
	c2:settimeout(.01)	
end

---------------------------------------------------------------------------------------------------
-- Export stop
---------------------------------------------------------------------------------------------------
function LuaExportStop()
	--socket.try(c:send("quit")) -- to close the listener socket
	if c1 then
		c1:close()
	end	
	if c2 then
		c2:close()
	end	
end

---------------------------------------------------------------------------------------------------
-- Export after next frame
---------------------------------------------------------------------------------------------------
function LuaExportAfterNextFrame()
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
	
	-- read from main panel
	local MainPanel = GetDevice(0)
	local AirspeedNeedle = MainPanel:get_argument_value(11)*1000
	local Altimeter_1000_footPtr = MainPanel:get_argument_value(24)*10000
	local Variometer = MainPanel:get_argument_value(29)*10000
	local TurnNeedle = MainPanel:get_argument_value(27)*math.rad(3)
	local Slipball = MainPanel:get_argument_value(28)
	local CompassHeading = MainPanel:get_argument_value(1) * math.pi * 2.0
	local Landing_Gear_Handle = MainPanel:get_argument_value(150)
	local Manifold_Pressure = MainPanel:get_argument_value(10) * 65 + 10
	local Engine_RPM = MainPanel:get_argument_value(23)*4500
	local AHorizon_Pitch = MainPanel:get_argument_value(15) * math.pi / 3.0
	local AHorizon_Bank = MainPanel:get_argument_value(14) * math.pi
	local AHorizon_PitchShift = MainPanel:get_argument_value(16) * 10.0 * math.pi/180.0
	
	if c1 then
		socket.try(c1:send(string.format("%.3f %.2f %.2f %.2f %.2f %.2f %.2f %.0f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f \n", t, altRad, altBar, pitch*1000.0, bank*1000.0, yaw*1000.0, accel.x*1000.0, angle*1000, accel.y*1000.0, accel.z*1000.0, accel.x*1000.0, (accel.y-1)*1000.0, accel.z*1000.0, user4, user5, user6, 7)))
	end
	if c2 then
		socket.try(c2:send(string.format("{ 'AirspeedNeedle':%.2f, 'Altimeter_1000_footPtr':%.2f, 'Variometer':%.2f, 'TurnNeedle':%.2f, 'Slipball':%.2f, 'CompassHeading':%.2f, 'Landing_Gear_Handle':%.2f, 'Manifold_Pressure':%.2f, 'Engine_RPM':%.2f, 'AHorizon_Pitch':%.2f, 'AHorizon_Bank':%.2f, 'AHorizon_PitchShift':%.2f }\n", AirspeedNeedle, Altimeter_1000_footPtr, Variometer, TurnNeedle, Slipball, CompassHeading, Landing_Gear_Handle, Manifold_Pressure, Engine_RPM, AHorizon_Pitch, AHorizon_Bank, AHorizon_PitchShift)))
	end
end
