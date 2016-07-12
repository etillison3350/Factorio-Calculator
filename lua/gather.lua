recipes = {}
for k, v in pairs(data.raw.recipe) do
	table.insert(recipes, v)
end

resources = {}
for k, v in pairs(data.raw.resource) do
	table.insert(resources, v)
end

assemblers = {}
for k, v in pairs(data.raw["assembling-machine"]) do
	table.insert(assemblers, v)
end
for k, v in pairs(data.raw.furnace) do
	table.insert(assemblers, v)
end

drills = {}
for k, v in pairs(data.raw["mining-drill"]) do
	table.insert(drills, v)
end

modules = {}
for k, v in pairs(data.raw.module) do
	table.insert(modules, v)
end

fuel = {}
for k, v in pairs(data.raw.item) do
	if v["fuel_value"] ~= nil then
		table.insert(fuel, {name = v["name"], fuel_value = v["fuel_value"]})
	end
end

function getIngredients(arr)
	standard = {}

	for i, o in ipairs(arr) do
		if o["name"] then
			table.insert(standard, {name = o["name"], amount = o["amount"]})
		else
			table.insert(standard, {name = o[1], amount = o[2]})
		end
	end
	
--[[for i, o in ipairs(standard) do
		for k, v in pairs(o) do
			print(tostring(k) .. ": " .. tostring(v))
		end
	end]]
	
	return standard
end

function getEffects(arr)
	standard = {}
	
	for k, v in pairs(arr) do
		table.insert(standard, {effect = k, amount = v["bonus"]})
	end
	
	return standard
end