# Short Circuit
Fast, compact, modular way to design redstone circuits.

## Downloads
Short Circuit is available for downloads on Modrinth and Curseforge, for multiple mod loaders including Fabric, Forge, NeoForge and Quilt (uses Fabric).
- [Modrinth](https://modrinth.com/mod/short-circuit)
- [Curseforge](https://www.curseforge.com/minecraft/mc-mods/short-circuit)

You may also build the mod from source using JDK 21 and by running the following:
```bash
git clone https://github.com/North-West-Wind/ShortCircuit.git
cd ShortCircuit
./grawdlew build
```
Compiled mod files will be output at `{loader}/build/libs/`, where `{loader}` can be either `fabric`, `forge` or `neoforge`.

## Usage
I have made a video for this, if you prefer having some visuals.  
[![](https://img.youtube.com/vi/klFv8QrZ1eY/maxresdefault.jpg)](https://youtu.be/klFv8QrZ1eY)

This mod comes with 4 items you can craft:
- Circuit
- Poking Stick
- Truth Assigner
- Labelling Stick

### Circuit
![Crafting recipe of a Circuit](https://github.com/North-West-Wind/ShortCircuit/blob/main/images/circuit.png?raw=true)
A circuit item looks like a repeater with green torches. When placed, it is shown as a glass block with a carpet inside.
This is an uninitialized circuit. To make it work, hold the Poking Stick, and right-click on it.
You will be brought to the inside of the circuit.

The inside has 6 walls, with the middle of them labelled U (up), D (down), L (left), R (right), F (front), B (back).
They relate to the direction you placed the circuit block in.

Mark the sides as input or output as you need with the Poking Stick by crouch right-clicking the wall.
Input is where external redstone signals will be passed into, and output where signals from inside the circuit will be output to the world.

Build your circuit inside. Once you are done, you can leave the inside by right-clicking the wall with the Poking Stick.

When you get out, right-click the circuit block with an empty hand.
If the circuit doesn't break the rules, you should see the components you placed inside the circuit in the block.
This circuit is now ready to receive signals!

#### Duplication
You can duplicate circuits by right-clicking circuit items on a placed circuit block.

### Poking Stick
![Crafting recipe of a Poking Stick](https://github.com/North-West-Wind/ShortCircuit/blob/main/images/poking_stick.png?raw=true)
As introduced in the circuit section, the Poking Stick is the main way to interact with the circuit.
However, there are additional uses for it.

By crouch right-clicking a circuit, you can hide its inner components.
This is useful when you have too many dynamic circuits rendering, which may cause some lag.

You can also set the exterior size of a circuit using the Poking Stick.
By right-clicking the item towards nothing, you can cycle sizes between 4 and 256 (only powers of 2).

### Truth Assigner
![Crafting recipe of a Truth Assigner](https://github.com/North-West-Wind/ShortCircuit/blob/main/images/truth_assigner.png?raw=true)
This is the "fast" part of Short Circuit.
As circuits are made base on existing redstone components, they will have a certain amount of delay.
The Truth Assigner (aka the washing machine) fixes this problem.

The Truth Assigner is a machine that simulates dynamic (normal) circuits to create integrated circuits (ICs).
It records the output of the dynamic circuit and constructs a truth table for it, thus being called the Truth Assigner.

Integrated circuits are the outputs of Truth Assigner's conversion.
The main advantage of integrated circuits is that they are 0-tick, meaning they activate instantly from external sources, and also immediately update neighbor blocks if they changed.

However, one should be aware that, integrated circuits are not always the best way.
Under 2 main situations, you should not convert a dynamic circuit to an IC.
1. Delay is intentional
2. The circuit has a memory component

1 is trivial. As ICs are 0-tick, no delay is preserved after the conversion.
2 simply refers to if the output of the circuit depends on states of blocks inside the circuit that are not constant.
For example, if the output of the circuit depends on a copper bulb inside the circuit, and said copper bulb can change from receiving different inputs, then it is a memory component.

#### Duplication
Similar to dynamic circuits, integrated circuits can be duplicated in the same way, by right-clicking circuit items on the block.
Any circuit items can change between dynamic or integrated circuits.

#### Truth Assigner Settings
The Truth Assigner also needs to be configured in order to output the correct integrated circuit.

The first number field is the maximum delay. It is a number in (game) ticks per second.
In each simulation iteration, if the output of the circuit doesn't change after the max delay, its output will be forcefully recorded.

The button below max delay is the record mode. It can be toggled between "First Change" or "Full Delay".
"First Change" causes the Truth Assigner to record the output as soon as one of the outputs changed,
while "Full Delay" makes it wait for the full max delay duration to record the output.

The button next to max delay is the bit button. It cycles between 4, 2 and 1-bit.
As redstone signal can have strength ranging from 0 to 15, it can be represented as a 4-bit number.
During simulation, all possible signal strength combinations will be tried if it is set to 4-bit.
However, if a circuit has a lot of input sides, it can take a long time.
For example, a 3-input circuit will take 16x16x16=4096 iterations, which even with 1 tick max delay will still cost more than 3 minutes.

Using 2-bit for 3-input will result in 4x4x4=64 iterations, while using 1-bit will result in 2x2x2=8 iterations.
If your circuit is not signal strength dependant, consider using 1 or 2-bit.

### Labelling Stick
![Crafting recipe of a Labelling Stick](https://github.com/North-West-Wind/ShortCircuit/blob/main/images/labelling_stick.png?raw=true)
The Labelling Stick is an item for changing colors of circuits.
This is particularly useful for ICs, as all of them have the exact same model.
Changing the color allow players to distinguish between circuits with different functionality.

By right-clicking on any circuit block, the block will cycle through the 16 dye colors.
Doing so while crouching will cycle them in reverse.

The stick can also be used on the walls inside the circuit block.
By right-clicking on the wall, it will toggle its annotation.

## Known Issues
- The block model of integrated circuit can be used to see through the world when MoreCulling is used.
- Integrated circuits cannot output directly to walls of circuits.

## Reporting Bugs
Just use the issues tab on GitHub.

To make it easier for me to fix things, if you encounter a seemingly unintentional behaviour, try to do the following first:
- Create different variations of the buggy circuit. This can narrow down what actually went wrong. Examples of variation:
  - Swapping component order
  - Changing input/output sides
- Duplicate the circuit and place down multiple of them. Check if any of them run correctly.

If you confirm that a circuit is consistently buggy, please also include the circuit in the bug report.

## Plans
- Span large circuit generations across multiple ticks to avoid completely freezing the game
  - 1 chunk per tick?
- Create different renders for different integrated circuits

## Inspiration
This mod is inspired by another mod, Compact Machines, and real world computer engineering.

### Compact Machines
Is a mod that allows players to create single blocks that houses a bigger inside volume.
Players can pass item, fluid and energy through the machine's side.

Short Circuit takes this concept and apply it for redstone.

### Computer Engineering
It is not just computer engineering, but circuit design in general.
In real world circuit design, a circuit is often composed of smaller and simpler circuits.

Some examples:
- A 1-bit full adder (FA), which adds 3 1-bit values together and output their sum and carry, uses 2 XOR gates, 2 AND gates and 1 OR gate.
- In an arithmetic and logic unit (ALU), it is basically a combination of AND, OR, NOT, XOR logic gates, a full adder, and a multiplexer (MUX).
- Inside a CPU, it combines the ALU, memory and some other registers to function.

As these examples have shown, we use circuits that have been previously designed to create newer, better, more complex circuits.
Short Circuit aims to achieve that by squashing circuits into 1 block, and making them cloneable, so that circuits can be reused.

## Support
If you find my projects great, consider supporting me on Ko-fi!
[![Summatia drinking tea](https://files.catbox.moe/qlm7iq.png)](https://ko-fi.com/nww)