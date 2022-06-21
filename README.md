<center>
<p align="center"><img src="https://raw.githubusercontent.com/Kneelawk/WiredRedstone/main/src/main/resources/assets/wiredredstone/icon-256.png" alt="Logo" width="200"></p>
<h1 align="center">Wired Redstone</h1>
<h3 align="center">Adds redstone wires and stuff</h3>
<p align="center">
<a href="https://discord.gg/6vgpHcKmxg"><img src="https://kneelawk.com/assets/discord-64x64.png" alt="Discord"></a>
<a href="https://github.com/Kneelawk/WiredRedstone"><img src="https://kneelawk.com/assets/github-white-64x64.png" alt="Github"></a>
<a href="https://modrinth.com/mod/wiredredstone"><img src="https://kneelawk.com/assets/modrinth-64x64.png" alt="Modrinth"></a>
</p>
</center>

## Beta

This project is currently in beta. Expect bugs, missing items, and sometimes needing to use a command to fix
things. Please make regular backups.

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

## Mod Integrations

Wired Redstone has integrations support for [CC: Restitched], [EMI], [REI], and [WTHIT].

### [CC: Restitched]

Wired Redstone Bundled Cables can connect to CC: Restitched computers, allowing those computers to send and receive
bundled signals.

### [EMI] & [REI]

Wired Redstone machines have recipe support in both EMI and REI.

### [WTHIT]

Many Wired Redstone gates and wires will show extra information in the WTHIT HUD.

[CC: Restitched]: https://modrinth.com/mod/cc-restitched

[EMI]: https://modrinth.com/mod/emi

[REI]: https://www.curseforge.com/minecraft/mc-mods/roughly-enough-items

[WTHIT]: https://modrinth.com/mod/wthit

## Powered by GraphLib

Wired Redstone uses [GraphLib] for managing its wires connections. Sometimes you might encounter a strange mod
interaction or corruption caused by a crash that causes wires to stop behaving correctly. If something doesn't seem
right, GraphLib has some commands that can help. These commands can be accessed via:

```
/graphlib <sub-command>
```

[GraphLib]: https://github.com/Kneelawk/GraphLib

## Screenshots

Here is a picture of one of my dev worlds:
![Dev World Image](https://cdn-raw.modrinth.com//data/lyYGrdho/images/68625d24a76d7f37ee8e24a4f3f1a99500396656.png)
