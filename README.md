<center>
<p align="center"><img src="https://raw.githubusercontent.com/Kneelawk/WiredRedstone/main/src/main/resources/assets/wiredredstone/icon-256.png" alt="Logo" width="200"></p>
<h1 align="center">Wired Redstone</h1>
<h3 align="center">Adds redstone wires and stuff</h3>
</center>

## Pre-Alpha

This project is currently in pre-alpha. Expect bugs, missing items, and sometimes needing to use a command to fix
things.

## Breaking Changes

Version 0.2.0 introduces changes to how wire networks are saved. All wires from before version 0.2.0 will not function
when updated until they are fixed using a command.

The command to fix wires is:

```
/graphlib updatewires <from> <to>
```

This command can be used for more than just fixing wires from before 0.2.0. It can also be used if wires aren't
connecting for some reason or another (like being world-edited).

## World Edit

For the time being, wires will not function after they have been edited by world-edit. This is because world-edit does
not modify the information wires use to understand their connections.

To Fix this, use the same command as before in the affected areas:

```
/graphlib updatewires <from> <to>
```

**Note:** You will also likely want to use this command, even if you're only removing wires, because the wires would
leave their connection data behind otherwise, which could make new wires placed in the area act strange.

## Recipes

Recipes? What are those? Who would ever want to use this mod in survival?

I'll add recipes eventually, but I first need to add the machine that the recipes will be in :)

If you really want to use this mod in your survival world, you'll probably want to make a datapack that adds recipes for
the parts you need.
