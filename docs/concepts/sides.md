# Concepts: Sides

Minecraft operates in 2 Environments.
You can Think of this as 2 programs.
One is the Server, one is the Client. Normally you can see this when you connect to e.g. hypixel to play skyblock.
You are running the Client and Hypixel is running the Server. The same thing happens when you are playing
Singleplayer aswell, but the Server is hosted on the same machine as the Client.

## Sides in FoundryEngine

Foundry Engine also operates in this 2 sided way. There is a server and a client.
When you write [scripts](scripts.md), they are separated into 3 folders.

- `/server`
- `/common`
- `/client`

If you want to execute your code on the Client, you need to write an [entrypoint](entrypoint.md) in the Client.
And the same for the other entrypoints. You do not need one for each, but if you want to write code specific to one side
like use `ServerEvents` or `ClientEvents` then you need to separate those.