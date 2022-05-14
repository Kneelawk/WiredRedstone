<center>
<p align="center"><img src="https://raw.githubusercontent.com/Kneelawk/WiredRedstone/main/src/main/resources/assets/wiredredstone/icon-256.png" alt="Logo" width="200"></p>
<h1 align="center">Wired Redstone</h1>
<h3 align="center">Adds redstone wires and stuff</h3>
</center>

## Pre-Alpha

This project is currently in pre-alpha. Expect bugs, missing items, and sometimes needing to use a command to fix
things.

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
