# Overview
This project implements an evaluation GUI for Kalah engines to facilitate engine testing.

Main features are:
- State visualization
- Manual variation exploration
- Intuitive evaluation display
- Principal variation requesting.
- Easy to configure *(due to the fact that there is not a lot to configure)*
- DnD for matches *(encoded in JSON)*.

## Contents
- `src`: contains the sources for the GUI
- `img`: contains images used in the GUI
- `libs`: the dependencies of the code
- `serv`: the (java) infrastructure to connect a kalah engine to the gui. See `serv/README.md` for more details.

## Starting the GUI
After successful compilation, run the class `Coordinator`.

## Connecting your engine
The GUI and any engine communicate via sockets. 
To connect your engine to the gui you have to follow the steps listed below:
1. Implement the interface `Evaluator` *(see `serv\Evaluator.java`)* for your engine. The requiered datatypes are provided. Make sure to include the Jackson library *(included in the folder `libs`)* in the buildpath of your engine.
2. Adjust `eval.yaml` so that it references your engine. 
E.g. if your engine is set up to listen on the port X on the local machine, the you want to adjust the field `evaluators[0].port` to X.
If you want to have a different engine connected for each player, add another entry to the list `evaluators`, in analogous fashion to the first one.
The engine to player assignment is implemented by inserting the to assigned engine by name in the field `north` for the northern *(or second)* player and `south` for the southern *(or first)* player.
3. Adjust `cmap.yaml`. This configuration file controls the mapping from evaluation values to colors. 
There are currently two types of colormaps: *divergent* and *uniform* ones.
See [the maptplotlib documentation on colormaps](https://matplotlib.org/examples/color/colormaps_reference.html) for details on colormap types.
I recommend using divergent colormaps to visualize evaluation values. 
In most cases the only thing you need to do here is to set the upper and lower bound on your evaluation values. 
It is required that the upper bound is greater than zero. The analogous case applies to the lower bound.
E.g. if your evaluation ranges from -100 to 100, you need to set the field `upper` and `lower` to 100 and -100 respectivly.
If you want to change the colors involved, use the fields `good`, `bad` and `neutral`.
Colours for values greater than zero are determined by linearly interpolating between the colors `good` and `neutral`.
Analogously, colours for values smaller than zero are calculated linear interpolation between the colors `bad` and `neutral`.
For uniform colormaps, only a single color has to be specified.
Instances of both types of colormaps are referenced by the name specified in the `name` field. 
To assign colormaps to players, put the to assigned colormap name in the field `north` for the second and `south` for the first player.
Lastly the `fallback` field specifies the house color to be used when no evaluation is present.

After completing these steps you are done. Starting the GUI now should now establish a connection to your engine and show display the evaluation values appropriately.

Note: If you just want to explore a game or position without connecting an engine, you can just use the gui anyways. 
The engine will continouslty try to connect to the assigned engines defined in `eval.yaml` but will still work if these connections cannot be established.

# ToDo: 
- enable position export/import. File -> export current state/ import state
- add a logger pane to the gui. -> implement activiy listener.
- move this list to issues