# Wired Redstone v0.4.16+1.19.2

Wired Redstone version 0.4.16 for Minecraft 1.19.2

Changes:

* Added config option to switch version checking and config syncing to happen in the `PLAY` networking phase instead of
  the `LOGIN` networking phase for improved proxy compatibility.
* Added config option to disable version checking and config syncing entirely.
* Cleaned up log messages to be more readable.
* Cleaned up gradle and prevented runtime dependencies from leaking into others' projects when they depend on Wired
  Redstone.
