---------------------------------------------------------------------------------------------------
-- Export start 
---------------------------------------------------------------------------------------------------

f_mfd =
{
Start=function(self) 
	package.path = package.path..";.\\LuaSocket\\?.lua"
	package.cpath = package.cpath..";.\\LuaSocket\\?.dll"
	socket = require("socket")
	
	local my_init = socket.protect(function()	
		-- export telemetry to instrumeny panel on android
		host_mfd = host_mfd or "10.0.0.3"  	 -- android IP
		port_mfd = port_mfd or 6000
		c_mfd = socket.try(socket.connect(host_mfd, port_mfd)) -- connect to the listener socket
		c_mfd:setoption("tcp-nodelay",true) -- set immediate transmission mode
		c_mfd:settimeout(.1)
	end)
	my_init()	
end,

ActivityNextEvent=function(self, t)
	local tNext = t
	
	-- read from Targets FC3 export
	local info = LoGetSightingSystemInfo()
	local targets = LoGetTargetInformation()
	local jsonMFD = "{ 'scale_distance':40.0, 'pos_azimuth': 0.0, 'pos_elevation':0.0, 'size_azimuth': 0.0, 'size_elevation':0.0, 'coverage_H_min': 0.0, 'coverage_H_max':30.0, 'TDC_x':0.0, 'TDC_y':0.0, 'PRF':'HI', Targets':[{}], 'LockedTargets':[{}] }\n"
	if targets then		
		-- add targets to json
		local jsonTargets = "[ "
		for i,target in pairs(targets) do
			local jsonTarget = ""		
			if target.Type then 
				local targetType = LoGetNameByType(target.Type.level1, target.Type.level2, target.Type.level3, target.Type.level4)
			end
			if targetType then
				jsonTarget = string.format("{ 'ID':'%s', 'distance':%f, 'convergence_velocity':%f, 'mach':%f, 'delta_psi':%f, 'fim':%f, 'fin':%f, 'course':%f, 'isjamming':'%s', 'jammer_burned':'%s', 'country':'%s', 'Type':'%s' }", target.ID, target.distance, target.convergence_velocity, target.mach, target.delta_psi, target.fim, target.fin, target.course, tostring(target.isjamming), tostring(target.jammer_burned), target.country, targetType)		
			else
				jsonTarget = string.format("{ 'ID':'%s', 'distance':%f, 'convergence_velocity':%f, 'mach':%f, 'delta_psi':%f, 'fim':%f, 'fin':%f, 'course':%f, 'isjamming':'%s', 'jammer_burned':'%s', 'country':'%s', 'Type':'NA' }", target.ID, target.distance, target.convergence_velocity, target.mach, target.delta_psi, target.fim, target.fin, target.course, tostring(target.isjamming), tostring(target.jammer_burned), target.country)		
			end
			if jsonTargets ~= "[ " then
				jsonTargets = jsonTargets .. ","
			end
			jsonTargets = jsonTargets .. jsonTarget
			---print(jsonTarget)
		end
		jsonTargets = jsonTargets .. "]"
		
		jsonMFD = string.format("{ 'scale_distance':%f, 'pos_azimuth': %f, 'pos_elevation':%f, 'size_azimuth': %f, 'size_elevation':%f, 'coverage_h_min': %f, 'coverage_h_max':%f, 'TDC_x':%f, 'TDC_y':%f, 'PRF':%s,  'Targets':%s }\n", info.scale.distance, info.ScanZone.position.azimuth, info.ScanZone.position.elevation, info.ScanZone.size.azimuth, info.ScanZone.size.elevation, info.ScanZone.coverage_H.min, info.ScanZone.coverage_H.max, info.TDC.x, info.TDC.y, info.PRF.current, jsonTargets)		
	end
	
	-- write to log
	--print(jsonMFD)
	
	local my_send = socket.protect(function()
		if c_mfd then
			socket.try(c_mfd:send(jsonMFD))
		end
	end)
	my_send()
	
	return tNext + 0.1
end,

Stop=function(self)
	local my_close = socket.protect(function()
		if c_mfd then
			c_mfd:close()
		end	
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
		f_mfd:Start()
		if PrevLuaExportStart then
			PrevLuaExportStart()
		end
	end
end

do
	local PrevLuaExportActivityNextEvent=LuaExportActivityNextEvent
	LuaExportActivityNextEvent=function(t)
		local tNext = t
		tNext = f_mfd:ActivityNextEvent(t)
		if PrevLuaExportActivityNextEvent then
			PrevLuaExportActivityNextEvent(t)
		end
		return tNext
	end
end

do
	local PrevLuaExportStop=LuaExportStop
	LuaExportStop=function()
		f_mfd:Stop()
		if PrevLuaExportStop then
			PrevLuaExportStop()
		end
	end
end
