# Wired Redstone v0.2.10

Wired Redstone version 0.2.10 for Minecraft 1.18.2

Changes:

* Adds integration with CC: Restitched, allowing bundled cables to be read and written to using computers.
* Allows insulated wires to be powered by the block they're sitting on.
    * Insulated wires needed to be able to power the block underneath them, because there needed to be some way to power
      blocks weakly. Only allowing insulated wires to power blocks beneath them but not be powered by those blocks felt
      inconsistent.
* Makes sure all client-side networking is initialized before connecting to a server.
