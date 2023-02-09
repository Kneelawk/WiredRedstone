# Wired Redstone v0.3.11+1.18.2

Wired Redstone version 0.3.11 for Minecraft 1.18.2

Changes:

* Adds a config file at `config/wiredredstone/common.json5` for configuring things needed on both clients and servers.
    * This config allows users to configure many of the values of the Redstone Assembler, including its energy capacity.
* Added config option to switch version checking and config syncing to happen in the `PLAY` networking phase instead of
  the `LOGIN` networking phase for improved proxy compatibility.
* Added config option to disable version checking and config syncing entirely.
