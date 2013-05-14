---------------------------------------------------------------------------------------------------
-- Export start 
---------------------------------------------------------------------------------------------------

f_telemetryFC =

{
Start=function(self) 
	package.path = package.path..";.\\LuaSocket\\?.lua"
	package.cpath = package.cpath..";.\\LuaSocket\\?.dll"
	socket = require("socket")
	
	local my_init = socket.protect(function()	
		-- export telemetry to instrumeny panel on android
		host_telemetryFC = host_telemetryFC or "localhost"  	 -- android IP
		port_telemetryFC = port_telemetryFC or 6000
		c_telemetryFC = socket.try(socket.connect(host_telemetryFC, port_telemetryFC)) -- connect to the listener socket
		c_telemetryFC:setoption("tcp-nodelay",true) -- set immediate transmission mode
		c_telemetryFC:settimeout(.01)
	end)
	my_init()	
end,

ActivityNextEvent=function(self, t)
	local tNext = t
	
	-- read from FC3
	local MainPanel = GetDevice(0)
	local AirspeedNeedle = LoGetIndicatedAirSpeed() * 1.943
	local Altimeter_10000_footPtr = LoGetAltitudeAboveSeaLevel() * 3.28
	local Altimeter_1000_footPtr = LoGetAltitudeAboveSeaLevel() * 3.28
	local Altimeter_100_footPtr = LoGetAltitudeAboveSeaLevel() * 3.28
	local Variometer = LoGetVerticalVelocity() * 196
	local AngleOfAttack = LoGetAngleOfAttack() 
	local TurnNeedle = 0
	local Slipball = LoGetSlipBallPosition()
	local CompassHeading = math.pi * 2.0 - LoGetControlPanel_HSI().HeadingPointer
	local Landing_Gear_Handle = 0
	local Manifold_Pressure = 0
	local Engine_RPM = LoGetEngineInfo().RPM.left * 100
	local pitch, bank, yaw = LoGetADIPitchBankYaw()
	local AHorizon_Pitch = - pitch
	local AHorizon_Bank = - bank
	local AHorizon_PitchShift = 0
	local GyroHeading = math.pi * 2.0 - LoGetControlPanel_HSI().HeadingPointer
	local Oil_Temperature = LoGetEngineInfo().Temperature.left / 10
	local Oil_Pressure = 0
	local Fuel_Pressure = 0
	
	local Fuel_Tank_Left = LoGetEngineInfo().fuel_internal * 2.2 / 100
	local Fuel_Tank_Right = LoGetEngineInfo().fuel_internal * 2.2 /100
	local Fuel_Tank_Fuselage = LoGetEngineInfo().fuel_external * 2.2 / 100
		
	local my_send = socket.protect(function()
		if c_telemetryFC then
			socket.try(c_telemetryFC:send(string.format("{ 'AirspeedNeedle':%.2f, 'Altimeter_10000_footPtr':%.2f, 'Altimeter_1000_footPtr':%.2f, 'Altimeter_100_footPtr':%.2f, 'Variometer':%.2f,'AngleOfAttack':%.2f, 'TurnNeedle':%.2f, 'Slipball':%.2f, 'CompassHeading':%.2f, 'Landing_Gear_Handle':%.2f, 'Manifold_Pressure':%.2f, 'Engine_RPM':%.2f, 'AHorizon_Pitch':%.2f, 'AHorizon_Bank':%.2f, 'AHorizon_PitchShift':%.2f, 'GyroHeading':%.2f, 'Oil_Temperature':%.2f, 'Oil_Pressure':%.2f, 'Fuel_Pressure':%.2f, 'Fuel_Tank_Left':%.2f, 'Fuel_Tank_Right':%.2f, 'Fuel_Tank_Fuselage':%.2f }\n", AirspeedNeedle, Altimeter_10000_footPtr, Altimeter_1000_footPtr, Altimeter_100_footPtr, Variometer, AngleOfAttack, TurnNeedle, Slipball, CompassHeading, Landing_Gear_Handle, Manifold_Pressure, Engine_RPM, AHorizon_Pitch, AHorizon_Bank, AHorizon_PitchShift, GyroHeading, Oil_Temperature, Oil_Pressure, Fuel_Pressure, Fuel_Tank_Left, Fuel_Tank_Right, Fuel_Tank_Fuselage)))
		end
	end)
	my_send()
	
	return tNext + 0.1	
end,


Stop=function(self)
	local my_close = socket.protect(function()
		if c_telemetryFC then
			c_telemetryFC:close()
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
		f_telemetryFC:Start()
		if PrevLuaExportStart then
			PrevLuaExportStart()
		end
	end
end

-- Works just after every simulation frame.
do
	local PrevLuaExportActivityNextEvent=LuaExportActivityNextEvent
	LuaExportActivityNextEvent=function(t)
		local tNext = t
		tNext = f_telemetryFC:ActivityNextEvent(t)
		if PrevLuaExportActivityNextEvent then
			PrevLuaExportActivityNextEvent(t)
		end
		return tNext
	end
end

-- Works once just after mission stop.
do
	local PrevLuaExportStop=LuaExportStop
	LuaExportStop=function()
		f_telemetryFC:Stop()
		if PrevLuaExportStop then
			PrevLuaExportStop()
		end
	end
end
