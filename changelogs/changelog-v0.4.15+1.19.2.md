# Wired Redstone v0.4.15+1.19.2

Wired Redstone version 0.4.15 for Minecraft 1.19.2

Changes:

* Fixes method naming conflict with LMP 0.8.1.
* Allows wire bounding-boxes to overlap, as the overlap-avoidance code was more trouble than it was worth.
* Adds a config file at `config/wiredredstone/common.json5` for configuring things needed on both clients and servers.
    * This config allows users to configure many of the values of the Redstone Assembler, including its energy capacity.
