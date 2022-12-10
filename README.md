<center>
<p align="center"><img src="https://raw.githubusercontent.com/Kneelawk/WiredRedstone/main/src/main/resources/assets/wiredredstone/icon-256.png" alt="Logo" width="200"></p>
<h1 align="center">Wired Redstone</h1>
<h3 align="center">Adds redstone wires and stuff</h3>
<p align="center">
<a href="https://discord.gg/6vgpHcKmxg"><img src="https://kneelawk.com/assets/discord-64x64.png" alt="Discord"></a>
<a href="https://github.com/Kneelawk/WiredRedstone"><img src="https://kneelawk.com/assets/github-white-64x64.png" alt="Github"></a>
<a href="https://modrinth.com/mod/wiredredstone"><img src="https://kneelawk.com/assets/modrinth-64x64.png" alt="Modrinth"></a>
<a href="https://www.curseforge.com/minecraft/mc-mods/wired-redstone"><img src="https://kneelawk.com/assets/curseforge-64x64.png" alt="CurseForge"></a>
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

## Screenshots

Here is a picture of one of my dev worlds:<br/>
![Dev World Image](https://cdn-raw.modrinth.com/data/lyYGrdho/images/68625d24a76d7f37ee8e24a4f3f1a99500396656.png)

## Recipes

As of version 0.3.0, survival-based recipes have been added. Most circuit components are crafted using a machine called
the Redstone Assembler. Redstone assembler recipes are visible in REI and EMI.

The Redstone Assembler can be crafted in a normal crafting table like such:<br/>
![Redstone Assembler Recipe](https://raw.githubusercontent.com/Kneelawk/WiredRedstone/main/screenshots/redstone_assembler_recipe.png)

Redstone Alloy Ingots can be crafted in the Redstone Assembler:<br/>
![Redstone Alloy Ingot Recipe](https://raw.githubusercontent.com/Kneelawk/WiredRedstone/main/screenshots/redstone_alloy_recipe.png)

Red Alloy Wire can then be crafted:<br/>
![Red Alloy Wire Recipe](https://raw.githubusercontent.com/Kneelawk/WiredRedstone/main/screenshots/red_alloy_wire_recipe.png)

Or Insulated Wire can be crafted:<br/>
![Insulated Wire Recipe](https://raw.githubusercontent.com/Kneelawk/WiredRedstone/main/screenshots/insulated_wire_recipe.png)

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

## Known Issues

* ~~LibMultiPart blocks (meaning all wires, gates, etc.) will crash when moved by Create
  contraptions (**[#15](https://github.com/Kneelawk/WiredRedstone/issues/15)**).~~ Fixed in `v0.4.11+1.19.2`.
* Under certain circumstances Not Enough Crashes can get Wired Redstone's rendering system into an invalid state while
  generating a crash report, causing an actual crash (**[#12](https://github.com/Kneelawk/WiredRedstone/issues/12)**).
* Server crashes can cause wires to stop working correctly and appear disconnected or refuse to connect when placed in
  specific areas. This is caused by the server crash preventing information about changes to wires' connections from
  being saved to the world. This can be fixed by breaking and replacing the wires until they start behaving properly or
  by running the `/graphlib updateblocks <from-x> <from-y> <from-z> <to-x> <to-y> <to-z>` command over an affected area
  as a server operator.

## Powered by GraphLib

Wired Redstone uses [GraphLib] for managing its wires connections. Sometimes you might encounter a strange mod
interaction or corruption caused by a crash that causes wires to stop behaving correctly. If something doesn't seem
right, GraphLib has some commands that can help. These commands can be accessed via:

```
/graphlib <sub-command>
```

[GraphLib]: https://github.com/Kneelawk/GraphLib

## License

This mod is licensed under the MIT license. This means you can redistribute it, make derivatives of it, and include it
in modpacks.
