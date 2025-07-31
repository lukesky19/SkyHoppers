# SkyHoppers
## Description
* SkyHoppers adds upgradable hoppers that can suction items, transfer items wirelessly to linked containers.
## Features
* Features upgrades for suction speed, suction amount, suction range, transfer speed, transfer amount, and number of linked containers.
* Wirelessly suctions and transfers items.
* Optimized for performance.
* Highly configurable.
* Supports upgrading from the predecessor of this plugin by the same name.
  * [My Fork of SkyHoppers](<https://github.com/lukesky19/Legacy_SkyHoppers>)
  * [The Upstream Project](<https://github.com/Oribuin/SkyHoppers>)
## Required Dependencies
* SkyLib
* Vault
## Optional Dependencies
* BentoBox
* QuickShop-Hikari
* RoseStacker
## Commands
- /skyhoppers reload - Command to reload the plugin
- /skyhoppers help - View the plugin's help message
- /skyhoppers give <player name> <amount> \[suction speed] \[suction amount] \[suction range] \[transfer speed] \[transfer amount] \[max containers] - Command to give a Player a SkyHopper.
- /skyhoppers transfer <player name> - Transfer a SkyHopper to another player.
- /skyhoppers load <true | false> - Loads all SkyHoppers. true will force load all SkyHoppers regardless if they are cached, false will only those SkyHoppers not already cached.
- /skyhoppers pause - Will pause all SkyHoppers globally.
- /skyhoppers unpause - Will unpause all SkyHoppers globally.
## Permisisons
- `skyhoppers.admin` - The permission to bypass a SkyHopper's owner, member, and protection checks.
- `skyhoppers.commands.skyhoppers` - The permission to access the /skyhoppers command.
- `skyhoppers.commands.skyhoppers.reload` - The permission to access /skyhoppers reload.
- `skyhoppers.commands.skyhoppers.help` - The permission to access /skyhoppers help.
- `skyhoppers.commands.skyhoppers.give` - The permission to access /skyhoppers give.
- `skyhoppers.commands.skyhoppers.transfer` - The permission to access /skyhoppers transfer.
- `skyhoppers.commands.skyhoppers.load` - The permission to access /skyhoppers load.
- `skyhoppers.commands.skyhoppers.pause` - The permission to access /skyhoppers pause.
- `skyhoppers.commands.skyhoppers.unpause` - The permission to access /skyhoppers unpause.
## Issues, Bugs, or Suggestions
* Please create a new [Github Issue](https://github.com/lukesky19/SkyHoppers/issues) with your issue, bug, or suggestion.
* If an issue or bug, please post any relevant logs containing errors related to SkyShop and your configuration files.
* I will attempt to solve any issues or implement features to the best of my ability.
## FAQ
Q: What versions does this plugin support?

A: 1.21.4, 1.21.5, 1.21.6, 1.21.7, and 1.21.8.

Q: Are there any plans to support any other versions?

A: The plugin will always support the latest version of the game at the time.

Q: Does this work on Spigot and Paper?

A: This plugin only works with Paper, it makes use of many newer API features that don't exist in the Spigot API. There are no plans to support Spigot.

Q: Is Folia supported?

A: There is no Folia support at this time. I may look into it in the future though.

## For Server Admins/Owners
* Download the plugin [SkyLib](https://github.com/lukesky19/SkyLib/releases).
* Download the plugin from the releases tab and add it to your server.

## Building
* Go to [SkyLib](https://github.com/lukesky19/SkyLib) and follow the "For Developers" instructions.
* Then run:
  ```./gradlew build```

## Why AGPL3?
I wanted a license that will keep my code open source. I believe in open source software and in-case this project goes unmaintained by me, I want it to live on through the work of others. And I want that work to remain open source to prevent a time when a fork can never be continued (i.e., closed-sourced and abandoned).
