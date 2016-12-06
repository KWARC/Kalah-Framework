# Kalah-WS1617
Kalah framework for the AI course at FAU WS16/17

`target/scala-2.12/kalah_2.12-1.0-javadoc.jar` and
`target/scala-2.12/api` contain the documentation,
`src` contains source files.

To implement your own agent, create a new class/object extending [[info.kwarc.teaching.AI.Kalah.Agent]]
and implement the required methods (init and move). For details see the documentation.

**Rules:**
- You can submit a *class*  or an *object* for your agents. If you submit a class, for each game we will create a new instance of this class. If it's an object, make sure that it works with consecutive games (i.e. be careful with states).
- Please give your class/object a *unique name* (i.e. not something like `MyAgent`) and make sure that the *name* string value of your class/object is identical.
- You can submit `.class` or `.jar`-files; please put your agent in the classpath `info.kwarc.teaching.AI.Kalah.WS1617.agents`
- Agents need to implement two functions: *init* and *move*. The first one will be called when the game starts. Your agent will be handed an instance of the `Board`-class, which contains e.g. the number of houses and starting seeds per house.

Your *init* function has 10 seconds to terminate. If it doesn't terminate in 10 seconds, that agent will lose the current game.

Your *move* function has 5 seconds to return a valid move. If it doesn't return a valid move in
5 seconds, the first house with non-zero seeds will be chosen as a move.
