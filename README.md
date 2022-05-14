<center>
<p align="center"><img src="https://raw.githubusercontent.com/Kneelawk/WiredRedstone/main/src/main/resources/assets/wiredredstone/icon-256.png" alt="Logo" width="200"></p>
<h1 align="center">Wired Redstone</h1>
<h3 align="center">Adds redstone wires and stuff</h3>
</center>

## Pre-Alpha

This project is currently in pre-alpha. Expect bugs, missing items, and sometimes needing to use a command to fix
things.

## Wired Redstone & RSWires

Wired Redstone is based on [RSWires by 2xsaiko][RSWires CF], and is essentially a rewrite. However, Wired Redstone
incorporates some rather large changes:

* Uses LibMultiPart for multi-part wires instead of doing the multi-part logic itself, like RSWires does.
    * This means improved compatibility with other mods and the ability to do more complex things like having multiple
      kinds of wire in one block, but also carries the weight of a full multi-part system like LibMultiPart.
* Has support for analog signals.
    * This means you can send more complicated signals, but it also means the mod spends more time calculating them.
* Adds support for wires being cut off by things (i.e. not going through blocks when going around corners, being able to
  be blocked by facades, etc.).
    * This can be handy to keep red-alloy-wires from connecting, but it does mean more calculations, checking for things
      in the way of wires.
* Makes some rather large optimizations to RSWires's wire-net engine that hopefully out-weigh the cost imposed by the
  other features added.
* Adds gates.
    * These probably could be added to RSWires too, fairly easily, I just haven't gotten around to adding them and no
      one else has either.

[RSWires CF]: https://www.curseforge.com/minecraft/mc-mods/rswires/

## Breaking Changes

Version 0.2.0 introduces changes to how wire networks are saved. ~~All wires from before version 0.2.0 will not function
when updated until they are fixed using a command.~~ Fixed in 0.2.1.

The command to fix wires is:

```
/graphlib updatewires <from> <to>
```

This command can be used for more than just fixing wires from old versions. It can also be used if wires just aren't
connecting for some reason or another. This command can cause significant amounts of lag if used over a large area, so
it is restricted to only be usable with operator privileges. For reference, updating a 100x100x100 area on my machine
took about 16 seconds. During that time, the server thread was completely stopped.

## Recipes

Recipes? What are those? Who would ever want to use this mod in survival?

I'll add recipes eventually, but I first need to add the machine that the recipes will be in :)

If you really want to use this mod in your survival world, you'll probably want to make a datapack that adds recipes for
the parts you need.

## Screenshots

Here is a picture of one of my dev worlds:
![Dev World Image](https://cdn-raw.modrinth.com//data/lyYGrdho/images/30a01e05b57c42f363c58aabe2d35051cb6eee04.png)
