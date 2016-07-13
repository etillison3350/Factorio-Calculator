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
icons = {}
for k, v in pairs(data.raw) do
	for k, v in pairs(v) do
		if v["type"] == "ammo" 
		or v["type"] == "armor"
		or v["type"] == "battery-equipment"
		or v["type"] == "blueprint"
		or v["type"] == "blueprint-book"
		or v["type"] == "capsule"
		or v["type"] == "deconstruction-item"
		or v["type"] == "fluid"
		or v["type"] == "gun"
		or v["type"] == "item"
		or v["type"] == "mining-tool"
		or v["type"] == "module"
		or v["type"] == "movement-bonus-equipment"
		or v["type"] == "night-vision-equipment"
		or v["type"] == "rail-planner"
		or v["type"] == "repair-tool"
		or v["type"] == "roboport-equipment"
		or v["type"] == "solar-panel-equipment"
		or v["type"] == "tool"
		then -- This is a list of all of the types that inherit from item, because I haven't been able to find any sort of file that defines this
			if v["fuel_value"] ~= nil then
				table.insert(fuel, {name = v["name"], fuel_value = v["fuel_value"]})
			end
			icons[v["name"]] = v["icon"]
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