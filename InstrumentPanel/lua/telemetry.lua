---------------------------------------------------------------------------------------------------
-- Export start 
---------------------------------------------------------------------------------------------------

Myfunction2 =

{
Start=function(self) 
	package.path = package.path..";.\\LuaSocket\\?.lua"
	package.cpath = package.cpath..";.\\LuaSocket\\?.dll"
	socket = require("socket")
	
	my_init = socket.protect(function()	
		-- export telemetry to instrumeny panel on android
		host2 = host2 or "10.0.0.3"  	 -- android IP
		port2 = port2 or 6000
		c2 = socket.try(socket.connect(host2, port2)) -- connect to the listener socket
		c2:setoption("tcp-nodelay",true) -- set immediate transmission mode
		c2:settimeout(.01)
	end)
	my_init()	
end,


AfterNextFrame=function(self)
	-- read from P-51D main panel instruments
	local MainPanel = GetDevice(0)
	local AirspeedNeedle = MainPanel:get_argument_value(11)*1000
	local Altimeter_10000_footPtr = MainPanel:get_argument_value(96)*100000
	local Altimeter_1000_footPtr = MainPanel:get_argument_value(24)*10000
	local Altimeter_100_footPtr = MainPanel:get_argument_value(25)*1000
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
	local GyroHeading = MainPanel:get_argument_value(12) * 2.0 * math.pi
	local Oil_Temperature = MainPanel:get_argument_value(30) * 100
	local Oil_Pressure = MainPanel:get_argument_value(31) * 200
	local Fuel_Pressure = MainPanel:get_argument_value(32) * 25
	local Fuel_Tank_Left = MainPanel:get_argument_value(155) * 92
	local Fuel_Tank_Right = MainPanel:get_argument_value(156) * 92
	local Fuel_Tank_Fuselage = MainPanel:get_argument_value(160) * 85
	
	my_send = socket.protect(function()
		if c2 then
			socket.try(c2:send(string.format("{ 'AirspeedNeedle':%.2f, 'Altimeter_10000_footPtr':%.2f, 'Altimeter_1000_footPtr':%.2f, 'Altimeter_100_footPtr':%.2f, 'Variometer':%.2f, 'TurnNeedle':%.2f, 'Slipball':%.2f, 'CompassHeading':%.2f, 'Landing_Gear_Handle':%.2f, 'Manifold_Pressure':%.2f, 'Engine_RPM':%.2f, 'AHorizon_Pitch':%.2f, 'AHorizon_Bank':%.2f, 'AHorizon_PitchShift':%.2f, 'GyroHeading':%.2f, 'Oil_Temperature':%.2f, 'Oil_Pressure':%.2f, 'Fuel_Pressure':%.2f, 'Fuel_Tank_Left':%.2f, 'Fuel_Tank_Right':%.2f, 'Fuel_Tank_Fuselage':%.2f }\n", AirspeedNeedle, Altimeter_10000_footPtr, Altimeter_1000_footPtr, Altimeter_100_footPtr, Variometer, TurnNeedle, Slipball, CompassHeading, Landing_Gear_Handle, Manifold_Pressure, Engine_RPM, AHorizon_Pitch, AHorizon_Bank, AHorizon_PitchShift, GyroHeading, Oil_Temperature, Oil_Pressure, Fuel_Pressure, Fuel_Tank_Left, Fuel_Tank_Right, Fuel_Tank_Fuselage)))
		end
	end)
	my_send()
		
end,


Stop=function(self)
	my_close = socket.protect(function()
		if c2 then
			c2:close()
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
		Myfunction2:Start()
		if PrevLuaExportStart then
			PrevLuaExportStart()
		end
	end
end

-- Works just after every simulation frame.
do
	local PrevLuaExportAfterNextFrame=LuaExportAfterNextFrame
	LuaExportAfterNextFrame=function()
		Myfunction2:AfterNextFrame()
		if PrevLuaExportAfterNextFrame then
			PrevLuaExportAfterNextFrame()
		end
	end
end

-- Works once just after mission stop.
do
	local PrevLuaExportStop=LuaExportStop
	LuaExportStop=function()
		Myfunction2:Stop()
		if PrevLuaExportStop then
			PrevLuaExportStop()
		end
	end
end
