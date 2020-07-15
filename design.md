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
    - ! RFC: What should the schema for adding a plugin be? The most natural method is by specifying the path to the Jar file, but we don't want to use file paths. We could support specifying a Git File URI. What else? I don't want to allow specifying content because that would involve sending potentially lots of megabytes over the RPC call.
  - Remove a plugin by its ID.
- Each plugin has a unique ID and a Jar file.
- The kernel must be able to download simulator plugins.
  - We can ship the default simulator plugin with the IntelliJ plugin, but there must be a way to download any of them because of the [usability requirement](#usability).

#### UI Interaction

- ! RFC: The kernel must be able to notify the UI of multiple tasks happening in parallel. For example, running a script could cause some repos to get cloned and a toolchain to get downloaded in parallel. As a user, I want to see both of these operations as tasks in IntelliJ happening in parallel. As a general rule, a script can start multiple tasks.
- ! RFC: The kernel needs a way to ask the UI for confirmation with a custom confirmation message. For example, if the kernel needs to flash the device with a new program, by default it should ask the user for confirmation. There also needs to be some local environment configuration option that bypasses this so that the confirmation results is either always allowed or denied (for headless operation). For example, you may want to always allow the kernel to flash the device during development or during some competitions (as a user, I would rather have a delayed start to my match rather than having the robot not work at all; it depends on the competition). As another example, you may want to always stop the kernel from flashing the device if you have released a product and you expect all the products to work the same for all your users and would rather handle these issues with your own support system.

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
  - ! RFC: Schema of a robot config file.
    - Kinematics
    - Device
    - Device resources
    - Scripts for body control & cad, limb control & cad, joint control
    - Device resources in the robot config file need a flag that says whether they are allowed to fail discovery or not. By default, this is false, meaning that all device resources need to complete discovery without errors or else the entire discovery process fails. If it is true, and that device resources fails discovery, then the discovery process does not fail. This could be useful for competition robots where the user would rather the robot work with partial functionality rather than not work at all.
- All hardware access must go through a robot (i.e., no direct access to hardware).
  - Scripts would not work in a simulator if they could directly access the hardware.
- We should use most of the hardware layer from the previous version of the kernel.

#### RPC

- We should use the Bowler RPC protocol from the previous version of the kernel.
  - Need to add support for asking the device its capabilities so that the kernel can figure out if it needs to upload a new program.

#### Dependency Management

- The kernel must be able to download the toolchain for the device specified in the robot config file. This is because of the [usability requirement](#usability).

#### uC Code

- The Bowler RPC protocol can handle dynamic definition of device resources, but including support for every possible device resource is not possible because uC's have limited space. Therefore, the kernel must be able to generate, compile, and flash C++ programs to the device.
- The kernel must be able to determine if it needs to upload a new program to the device. It will do this by asking the device for its capabilities, which will include the supported device resources (see [RPC](#rpc)). If a device resource in the robot config file being loaded is not in the supported capabilities, then the kernel must generate, compile, and flash a new program to the device.

### CAD

- Use the existing JCSG API for now.

## Non-Functional Requirements

### Open Source

- All core components and their dependencies must be open-source under a compatible license. Some optional, non-core components may have closed-source dependencies.

### Portability

- x86_64 Windows, macOS, and Linux must be supported. Arm linux must be supported.

### Responsiveness

- The kernel's runtime must not pause for longer than 15 milliseconds when running under ZGC. Runtimes that must run with another garbage collector, like G1, do not have this requirement.

### Usability

- Changing the device resources (e.g., changing the type of a servo, moving a sensor or actuator to a different pin) must not require recompiling and/or reflashing code to the microcontroller.
