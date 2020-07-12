# Kernel Design

## Functional Requirements

### gRPC Server

The kernel runs a gRPC server that supports the following operations.

#### Running Scripts

- Operations:
  - Run a script from its contents.
  - Run a script from a Git File URI.
- Dependency resolution:
  - Scripts may import from other scripts in the project. These dependencies are resolved to the relevant files in the project.
  - Scripts may depend on Bowler libraries specified by a Git File URI.
    - By default, resolution uses GitFS to pull the script.
    - If the URI resolves to a Bowler library that has been dev'd, then the dev'd version must be used instead of the version from the remote.
  - If credentials are required to resolve a dependency, the kernel must try to load them from the local environment first. If that fails, the kernel must ask the IDE for authentication. The authentication received from the IDE must not be stored on disk. If no IDE is available, dependency resolution fails and an exception is thrown at the call site.
- A script may be given arguments via a list of objects when it is invoked.
- A script may return an object when it returns.

#### GitFS

- Each repo is uniquely identified by a Git Repo URI.
- Each file in a repo is uniquely identified by a Git File URI.
- Each Git URI includes a remote.
- Operations:
  - Clone or pull a repo.
  - List files in a repo.
  - Read the contents of a file in a repo.
  - Run a script in a repo.
    - May pass arguments as a list of objects.
    - May receive a return value as an object.
  - Load a robot from a robot config file in a repo.

#### Hardware Management

- Operations:
  - Load a robot from its config file.
    - May specify the contents of the config file.
    - May specify a Git File URI.
    - Must specify whether to connect to hardware or a simulator.
      - In the hardware case, a connection method must be specified.
      - In the simulator case, a simulator plugin must be specified.

#### Simulator Plugins

- Operations:
  - List available plugins
  - Add a plugin
    - Adding a plugin with an ID that conflicts with an ID of an already-added plugin throws an error.
    - TODO: What should the schema for adding a plugin be? The most natural method is by specifying the path to the Jar file, but we don't want to use file paths. We could support specifying a Git File URI. What else? I don't want to allow specifying content because that would involve sending potentially lots of megabytes over the RPC call.
  - Remove a plugin by its ID.
- Each plugin has a unique ID and a Jar file.

### Headless Execution

- The kernel must not include any references to JavaFX so that it can run in a headless environment.

### Data Logging

- Every connected device resource must be automatically available as a data source.
  - Sensors return their sensor value (e.g., potentiometer -> voltage, encoder -> ticks, IMU -> values of axes).
  - Actuators return their control signal (e.g., servo -> angle, solenoid -> digital state, motor -> voltage). These are the values the user's program commands, not the measured value (which would be a sensor).
- A script may add its own data sources via *variables*.
  - A variable is addressed by name (a String).
  - State changes of variables are tracked such that the current state of a variable can be read from another thread in parallel.
- Data types that can be logged:
  - Numerical types (e.g., int, long, float, double, byte, char, short, boolean).
  - Strings.
  - Frame transforms.
  - Images (e.g., frames from a camera feed).
  - Arbitrary bytes.
- Data that has been logged must be able to be saved and exported to other formats (e.g., CSV) if the data type supports it (e.g., when exporting to a CSV you can export numerical data but not images).
- The amount of data to buffer must be configurable via the settings.

### Hardware

- A robot can be loaded from its config file and either a connection method (if connecting to the hardware) or simulator plugin (if connecting to a simulator).
- All hardware access must go through a robot (i.e., no direct access to hardware).
  - Scripts would not work in a simulator if they could directly access the hardware.

## Non-Functional Requirements

### Open Source

- All core components and their dependencies must be open-source under a compatible license. Some optional, non-core components may have closed-source dependencies.

### Portability

- x86_64 Windows, macOS, and Linux must be supported. Arm linux must be supported.

### Responsiveness

- The kernel's runtime must not pause for longer than 15 milliseconds when running under ZGC. Runtimes that must run with another garbage collector, like G1, do not have this requirement.
