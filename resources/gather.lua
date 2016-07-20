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
for k, v in pairs(data.raw["rocket-silo"]) do
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
icons = {}
for k, v in pairs(data.raw) do
	for k2, v2 in pairs(v) do
		if v2["type"] == "ammo" 
		or v2["type"] == "armor"
		or v2["type"] == "battery-equipment"
		or v2["type"] == "blueprint"
		or v2["type"] == "blueprint-book"
		or v2["type"] == "capsule"
		or v2["type"] == "deconstruction-item"
		or v2["type"] == "fluid"
		or v2["type"] == "gun"
		or v2["type"] == "item"
		or v2["type"] == "mining-tool"
		or v2["type"] == "module"
		or v2["type"] == "movement-bonus-equipment"
		or v2["type"] == "night-vision-equipment"
		or v2["type"] == "rail-planner"
		or v2["type"] == "repair-tool"
		or v2["type"] == "roboport-equipment"
		or v2["type"] == "solar-panel-equipment"
		or v2["type"] == "tool"
		then -- This is a list of all of the types that inherit from item, because I haven't been able to find any sort of file that defines this
			if v2["fuel_value"] ~= nil then
				table.insert(fuel, {name = v2["name"], fuel_value = v2["fuel_value"]})
			end
			icons[v2["name"]] = v2["icon"]
		end
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
	
	return standard
end

function getEffects(arr)
	standard = {}
	
	for k, v in pairs(arr) do
		table.insert(standard, {effect = k, amount = v["bonus"]})
	end
	
	return standard
end