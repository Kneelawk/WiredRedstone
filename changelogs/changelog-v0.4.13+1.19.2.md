# Wired Redstone v0.4.13+1.19.2

Wired Redstone version 0.4.13 for Minecraft 1.19.2

Changes:

* Changes Redstone Projectors to highlight the block receiving the projected redstone power instead of the block
  providing the projected redstone power.
* Fixes gate port text rendering on non-vanilla renderers.
* Fixes Projection Viewer items.
* Removes deprecated core shader usage to improve compatibility with renderer mods like Canvas and Sodium/Indium/Iris.
* Removes cursed `IdentifierMixin` that was used to get core shaders working.
* Fixes Projection Viewer renderer to work with Canvas and Iris.
* Fixes WTHIT plugin registration to not use the deprecated method.
