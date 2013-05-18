---------------------------------------------------------------------------------------------------
-- Export UH-1H instruments
---------------------------------------------------------------------------------------------------

f_uh1h =
{
Start=function(self) 
	package.path = package.path..";.\\LuaSocket\\?.lua"
	package.cpath = package.cpath..";.\\LuaSocket\\?.dll"
	socket = require("socket")
	
	my_init = socket.protect(function()	
		-- export telemetry to instrumeny panel on android
		host2, port2 = "10.0.0.3", 6000 -- replace IP with android device ip 
		udp = socket.try(socket.udp())	
	end)
	my_init()	
end,

AfterNextFrame=function(self)
    --local player = LoGetObjectById(LoGetPlayerPlaneId())
	--print(player.Name)
	--if (player and player.Name == "UH-1H") then
		-- read from UH-1H main panel instruments
		local MainPanel = GetDevice(0)
		local AirspeedNeedle = MainPanel:get_argument_value(117)*150
		local Altimeter_10000_footPtr = MainPanel:get_argument_value(178)*100000
		local Altimeter_1000_footPtr = MainPanel:get_argument_value(179)*10000
		local Altimeter_100_footPtr = MainPanel:get_argument_value(180)*1000
		local Variometer = MainPanel:get_argument_value(134)*4000
		local TurnNeedle = MainPanel:get_argument_value(132)*math.rad(1.5)
		local Slipball = MainPanel:get_argument_value(133)
		local CoursePointer1 = MainPanel:get_argument_value(159) * 2.0 * math.pi
		local CoursePointer2 = MainPanel:get_argument_value(160) * 2.0 * math.pi
		local CompassHeading = MainPanel:get_argument_value(165) * 2.0 * math.pi
		local Torque = MainPanel:get_argument_value(124) * 103 + -3
		local Engine_RPM = MainPanel:get_argument_value(122)*7200
		local AHorizon_Pitch = MainPanel:get_argument_value(143) * math.pi / 2.0
		local AHorizon_Bank = MainPanel:get_argument_value(142) * math.pi
		local AHorizon_PitchShift = 0
		local GyroHeading = MainPanel:get_argument_value(165) * 2.0 * math.pi
		local Oil_Temperature = MainPanel:get_argument_value(114) * 150
		local Oil_Pressure = MainPanel:get_argument_value(113) * 100
		local Fuel_Pressure = MainPanel:get_argument_value(126) * 50
		local Fuel_Tank = MainPanel:get_argument_value(239) * 1580.0 / 100.0
		local VerticalBar = MainPanel:get_argument_value(151) * -2.0
		local HorisontalBar = MainPanel:get_argument_value(152) * -2.0
		local ToMarker = MainPanel:get_argument_value(153)
		local FromMarker = MainPanel:get_argument_value(154)		
		local RotCourseCard = MainPanel:get_argument_value(156) * 2.0 * math.pi
		
		my_send = socket.protect(function()
			local json = string.format("{ 'AirspeedNeedle':%.2f, 'Altimeter_10000_footPtr':%.2f, 'Altimeter_1000_footPtr':%.2f, 'Altimeter_100_footPtr':%.2f, 'Variometer':%.2f, 'TurnNeedle':%.2f, 'Slipball':%.2f, 'CoursePointer1':%.2f, 'CoursePointer2':%.2f, 'CompassHeading':%.2f, 'Torque':%.2f, 'Engine_RPM':%.2f, 'AHorizon_Pitch':%.2f, 'AHorizon_Bank':%.2f, 'GyroHeading':%.2f, 'Oil_Temperature':%.2f, 'Oil_Pressure':%.2f, 'Fuel_Pressure':%.2f, 'Fuel_Tank':%.2f,'VerticalBar':%.2f,'HorisontalBar':%.2f,'ToMarker':%.2f,'FromMarker':%.2f,'RotCourseCard':%.2f}\n", 
				AirspeedNeedle, 
				Altimeter_10000_footPtr, 
				Altimeter_1000_footPtr, 
				Altimeter_100_footPtr, 
				Variometer, 
				TurnNeedle, 
				Slipball, 
				CoursePointer1, 
				CoursePointer2, 
				CompassHeading, 
				Torque, 
				Engine_RPM, 
				AHorizon_Pitch, 
				AHorizon_Bank, 
				GyroHeading, 
				Oil_Temperature, 
				Oil_Pressure, 
				Fuel_Pressure, 
				Fuel_Tank,
				VerticalBar,
				HorisontalBar,
				ToMarker,
				FromMarker,
				RotCourseCard
			)
			--print(json)	
			socket.try(udp:sendto(json, host2, port2))
		end) -- my_send
		my_send()
	--end -- if UH-1H	
end,


Stop=function(self)
	my_close = socket.protect(function()
		socket.try(udp:close())
	end)
	my_close()
end
}

-- =============
-- Overload
-- =============
do
	local PrevLuaExportStart=LuaExportStart
	LuaExportStart=function()
		f_uh1h:Start()
		if PrevLuaExportStart then
			PrevLuaExportStart()
		end
	end
end

do
	local PrevLuaExportAfterNextFrame=LuaExportAfterNextFrame
	LuaExportAfterNextFrame=function()
		f_uh1h:AfterNextFrame()
		if PrevLuaExportAfterNextFrame then
			PrevLuaExportAfterNextFrame()
		end
	end
end

do
	local PrevLuaExportStop=LuaExportStop
	LuaExportStop=function()
		f_uh1h:Stop()
		if PrevLuaExportStop then
			PrevLuaExportStop()
		end
	end
end
