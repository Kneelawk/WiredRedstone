<center>
<p align="center"><img src="https://raw.githubusercontent.com/Kneelawk/WiredRedstone/main/src/main/resources/assets/wiredredstone/icon-256.png" alt="Logo" width="200"></p>
<h1 align="center">Wired Redstone</h1>
<h3 align="center">Adds redstone wires and stuff</h3>
</center>

## Beta

This project is currently in beta. Expect bugs, missing items, and sometimes needing to use a command to fix
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

Please go checkout [RSWires][RSWires CF] if you haven't already.

[RSWires CF]: https://www.curseforge.com/minecraft/mc-mods/rswires/

## Recipes

As of version 0.3.0, survival-based recipes have been added. Most circuit components are crafted using a machine called
the Redstone Assembler. Redstone assembler recipes are visible in REI and EMI.

## Sometimes things might break

Sometimes you might encounter a bug or a strange mod interaction that causes wires to stop behaving correctly. Wired
Redstone uses [GraphLib] for managing its wires connections. If something doesn't seem right, GraphLib has some commands
that can help. These commands can be accessed via:

```
/graphlib <sub-command>
```

## Screenshots

Here is a picture of one of my dev worlds:
![Dev World Image](https://cdn-raw.modrinth.com//data/lyYGrdho/images/68625d24a76d7f37ee8e24a4f3f1a99500396656.png)
