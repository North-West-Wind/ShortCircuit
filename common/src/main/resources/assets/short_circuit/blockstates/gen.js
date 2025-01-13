const fs = require("fs");

const template = {
  parent: "minecraft:block/cube_all",
  textures: {
    all: "short_circuit:block/circuit_board"
  }
}
const json = JSON.parse(fs.readFileSync("circuit_board.json", { encoding: "utf8" }));
for (const variant of Object.values(json.variants)) {
	const copy = Object.assign({}, template);
	copy.textures.all = variant.model;

	fs.writeFileSync(`../models/block/${variant.model.split("/").pop()}.json`, JSON.stringify(copy, null, 2));
}