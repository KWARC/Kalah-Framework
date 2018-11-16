# Overview
The source files included in this folder provide the necessary infrastructure to enable any Kalah position evaluator to connect to the GUI, effectively visualizing the evaluators "thoughts" on the current position. This is particularly useful to increase the understanding how of the given evaluator does evaluate.

# Setup
1. Make sure your engine implements the `Evaluator` interface.
2. Create an instance of your Evaluator in the `main` of `ExternalEvaluatorServer`, analogous to the way it is done in the template. 
2. Make sure to include the Jackson library (provided in the `lib` folder) in your project.
3. Run `ExternalEvaluatorServer`. This will start the server to which the gui connects. 
The starting order of programs is irrelevant. 
The GUI will continously try to connect to the engine, and the engine will restart listening after any connection has been ended.
4. Start the GUI