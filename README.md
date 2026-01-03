# True Proxy Send Command
Place the Jar file into both Velocity and your Paper servers' `plugins` folder, and you'll have access to the `/send` command via players, datapacks, and command blocks!<br><br>

## How to use:
The syntax is `/send <players> <server>`, where \<players> is any Minecraft player selector, (e.g @a, @p, @s), and \<server> is any named server in your `velocity.toml` file.<br><br>

## How it works:
Both halves talk to each other via Bukkit's plugin channel messages. Velocity will send the list of servers to each Paper server, and Paper will send what player(s) and what server were argued in the command back to Velocity.
