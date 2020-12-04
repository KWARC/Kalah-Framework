# Kalah-Framework
Kalah framework for the AI course at FAU.

This contains:
- `target/scala-2.12/kalah_2.12-1.0.jar` contains the compiled framework in total
- `target/scala-2.12/kalah_2.12-1.0-javadoc.jar` contains the packaged (javadoc) documentation
- `target/scala-2.12/api` contains the javadoc documentation and
- `src` contains source files in scala and (thanks to Marcel Rupprecht for doing all the work!) java interfaces so you don't have to take care of converting scala datastructures.

You are free to use scala or java; even though the framework is written in scala, all the interfaces you'll need to use use standard java datatypes only.

To implement your own agent, create a new class/object extending [[info.kwarc.teaching.AI.Kalah.Agents.Agent]]
and implement the required methods (init and move). For details see the documentation.

**Rules:**
- You can submit a *class*  or (in the case of scala) an *object* for your agents. If you submit a class, for each game we will create a new instance of this class. If it's an object, make sure that it works with consecutive games (i.e. be careful with states).
- Please give your class/object a *unique name* (i.e. not something like `MyAgent`) and make sure that the *name* string value of your class/object is identical.
- You can submit `.class`, `.java`, `.scala` or `.jar`-files; please put your agent in the classpath `info.kwarc.teaching.AI.Kalah.WS2021.agents`
- Your submission may not exceed 200 MB.
- Your implementation must not interact with external resources, such as the network or other programs. 
- If you use additional objects/classes, please put them in a unique namespace *extending* `info.kwarc.teaching.AI.Kalah.WS2021.agents`
- Agents need to implement two functions: *init* and *move*. The first one will be called when the game starts. Your agent will be handed an instance of the `Board`-class, which contains e.g. the number of houses and starting seeds per house.
- Your *init* function has  about 10 seconds to terminate. If it doesn't terminate in 10 seconds, that agent will lose the current game.
- Your *move* function has  about 5 seconds to return a valid move. If it doesn't return a valid move in the time limit, its [[info.kwarc.teaching.AI.Kalah.Agents.Agent.timeoutMove]] variable will be taken instead.
- The exact time limits may change, make sure your implementations can deal with any provided limit. 
- Given the time restrictions, *please make sure your agents are thread-safe and can be forcefully killed in a reasonable time frame*.

**Requirements:**
- Java 8
- The .scala files should be compatible with scala 2.10. If there are any problems with the scala version, let me know as soon as possible via the forum.
- If there are any other problems with using my framework (like java interfacing) please let me/us know via the forum as soon as possible.

## Agent GUI
Luca Reeb has kindly provided a GUI for debugging purposes in the subfolder `KalahGUI`. For a description, see `KalahGUI/README.md`.
