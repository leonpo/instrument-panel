---------------------------------------------------------------------------------------------------
-- Export start 
---------------------------------------------------------------------------------------------------

f_tews =
{
Start=function(self) 
	package.path = package.path..";.\\LuaSocket\\?.lua"
	package.cpath = package.cpath..";.\\LuaSocket\\?.dll"
	socket = require("socket")
	
	my_init = socket.protect(function()	
		-- export telemetry to instrumeny panel on android
		host_tews = host_tews or "10.0.0.10"  	 -- android IP
		port_tews = port_tews or 6000
		c_tews = socket.try(socket.connect(host_tews, port_tews)) -- connect to the listener socket
		c_tews:setoption("tcp-nodelay",true) -- set immediate transmission mode
		c_tews:settimeout(.01)
	end)
	my_init()	
end,

AfterNextFrame=function(self)
	-- read from TWS FC3 export
	local threats = LoGetTWSInfo()
	local jsonThreats = "{ 'Mode':1.0, 'Emiters':[{ 'ID':'test', 'Power':1.0, 'Azimuth':0.8, 'Priority':1.0, 'SignalType':'AAA', 'Type':'BBB' }] }\n"
	if threats then		
		-- add emiters to json
		local jsonEmiters = "[ "
		for mode,emit in pairs (threats.Emitters) do
			local jsonEmit = ""		
			local threatType = LoGetNameByType(emit.Type.level1, emit.Type.level2, emit.Type.level3, emit.Type.level4)
			if threatType then
				jsonEmit = string.format("{ 'ID':'%s', 'Power':%f, 'Azimuth':%f, 'Priority':%f, 'SignalType':'%s', 'Type':'%s' }", emit.ID, emit.Power, emit.Azimuth, emit.Priority, emit.SignalType, threatType)		
			else
				jsonEmit = string.format("{ 'ID':'%s', 'Power':%f, 'Azimuth':%f, 'Priority':%f, 'SignalType':'%s', 'Type':'N/A' }", emit.ID, emit.Power, emit.Azimuth, emit.Priority, emit.SignalType)		
			end
			if jsonEmiters ~= "[ " then
				jsonEmiters = jsonEmiters .. ","
			end
			jsonEmiters = jsonEmiters .. jsonEmit
		end
		jsonEmiters = jsonEmiters .. "]"
		
		jsonThreats = string.format("{ 'Mode':%f, 'Emiters':%s }\n", threats.Mode, jsonEmiters)		
	end
	
	my_send = socket.protect(function()
		if c_tews then
			socket.try(c_tews:send(jsonThreats))
		end
	end)
	my_send()
		
end,

Stop=function(self)
	my_close = socket.protect(function()
		if c_tews then
			c_tews:close()
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
		f_tews:Start()
		if PrevLuaExportStart then
			PrevLuaExportStart()
		end
	end
end

-- Works just after every simulation frame.
do
	local PrevLuaExportAfterNextFrame=LuaExportAfterNextFrame
	LuaExportAfterNextFrame=function()
		f_tews:AfterNextFrame()
		if PrevLuaExportAfterNextFrame then
			PrevLuaExportAfterNextFrame()
		end
	end
end

-- Works once just after mission stop.
do
	local PrevLuaExportStop=LuaExportStop
	LuaExportStop=function()
		f_tews:Stop()
		if PrevLuaExportStop then
			PrevLuaExportStop()
		end
	end
end
