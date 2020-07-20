# Kernel Design

## Functional Requirements

### gRPC Server

The kernel runs a gRPC server that supports the following operations.

#### Script Operations

- Run a script from a file specifier and a list of dev'd libraries

#### GitFS Operations

- Clone or pull a repo.
- List files in a repo.
- Read the contents of a file in a repo.
- Clear the cache.

#### Hardware Operations

- Load a robot from its config file (given as a file specifier) and a connection method.

#### Plugin Operations

- List plugins with a matching group, name, and version.
  - The group and name fields may be matched using a regex.
  - The version field may be matched using a version range.
- Download a plugin given its triple.
- Clear the plugin cache.

#### UI Interaction

- The client and kernel share a bidirectional stream.
  - The kernel can create tasks. Task schema:
    - Unique ID
    - Description
    - Progress
  - The kernel can update tasks. Update schema:
    - Task ID
    - Progress
  - The kernel can stop tasks. Stop schema:
    - Task ID
  - The kernel can ask the IDE for confirmation. Confirmation schema:
    - Description
    - Confirmation response: Allowed/Denied
  - A progress of `NaN` must show an indeterminate progress bar. Otherwise, progress is a percentage stored as a floating point number in the range `[0, 100]`.

#### Timeout

The client must periodically call a keepalive function in the kernel. If the kernel has not received a keepalive call for some time period, then the kernel must interrupt any running scripts, disconnect any connected hardware, and exit. If the kernel is running headlessly without a client, then no keepalives are required to keep the kernel alive.

- When started headlessly, the kernel is configured to not have a timeout.
- When started with a head, the kernel is configured to have a timeout.

### Script Dependency Management

- Script dependency resolution:
  - Scripts may import from other scripts in the project. These dependencies are resolved to the relevant files in the project.
  - Scripts may depend on Bowler libraries specified by a Git Repo URI.
    - By default, resolution uses GitFS to pull in the scripts.
    - If the URI resolves to a Bowler library that has been dev'd, then the dev'd version must be used instead of the version from the remote. The kernel must then ask the client to provide the files in the dev'd dependency.
  - If credentials are required to resolve a dependency, the kernel must try to load them from the local environment first. If that fails, the kernel must ask the client for authentication. The authentication received from the client must not be stored on disk or in memory for longer than strictly necessary (the kernel is allowed to frequently ask for credentials). If no client is available, dependency resolution fails.

### GitFS

- Each repo is uniquely identified by a Git Repo URI.
- Each file in a repo is uniquely identified by a Git File URI.
- Each Git URI includes a remote.

### File Runner

- There is a file runner interface which specifies:
  - A method to run a script in a repo that accepts a list of objects and returns an object.
  - A method to load a robot from a robot config file specified as a file specifier and a connection method.
    - A connection method can be a device connection method (e.g. UDP or HID) or a simulator.
- A script is given a string containing a JSON object. The schema of this object is:
  - A dictionary of script environment variables. This is not to be confused with the operating system's environment variables or the JVM's environment variables.
- Scripts have persistent storage. Scripts can modify files "on disk" and these changes need to make their way to the user's machine. ! RFC: When is this needed? I can't think of a use case.

### Plugin Manager

- There is a plugin manager interface which specifies:
  - A method for loading a plugin given its triple.
  - A method for downloading a plugin given its triple.
    - When downloading a newer version of an already-downloaded plugin, the older version is first evicted from the cache to maintain the invariant that there can only be one version of a plugin installed.
  - A method for listing plugins with a matching group, name, and version.
    - The group and name fields may be matched using a regex.
    - The version field may be matched using a version range.
- There can only be one version of a plugin installed.
- Plugins are uniquely specified by a group, name, and version triple which directly corresponds to the Maven group, name, and version.
  - A version range can be specified to use the latest version in the range.
- Published plugin artifacts must follow this naming scheme:
  - The artifact name must start with `bowler-plugin-`.
  - Any plugin type-specific naming prefix must follow next (e.g., device plugins may have a `device-` prefix, so the plugin name must have the prefix `bowler-plugin-device-`).

### Hardware

- All hardware access must go through a robot (i.e., no direct access to hardware).
  - Scripts would not work in a simulator if they could directly access the hardware.

#### Robot Config File Schema

- 1 kinematic description
- 1 device
- 1 toolchain
- 0..* device resource
  - Any device resource can be attached to any member of the kinematic description. The meaning of a device resources attached to a part of the kinematic description is ascribed by the relevant kinematic controller for that part of the kinematic description.
    - An IMU may be attached to the body, to any limb, or to any link.
    - A servo may be attached to the body, to any limb, or to any link.
    - A potentiometer may be attached to the body, to any limb, or to any link.
    - ! RFC: I'm not sure this is a good solution. Right now, we are missing a method of storing the root offset transform. For example, an IMU has a transform from the root of the member it attaches to to its frame. A servo on the joint doesn't need this, though. Maybe we could store it for the z-offset of the servo, if it doesn't line up exactly with the origin of the joint. You could also mount a servo on a link that has nothing to do with the joint, but maybe we don't need to support those weird use cases.
- 1 script for each of body control, body cad
- 1 script per limb for limb control, limb cad
- 1 script per joint for joint control
- Kinematic description schema:
  - 1 body
  - 0..* limb
- Body schema:
  - ! RFC
- Limb schema:
  - 1..* link
- Link schema:
  - 1..* DH parameter
- Device schema:
  - Group, name, version triple identifying the plugin
- Toolchain schema:
  - Group, name, version triple identifying the plugin
- Device resource schema:
  - Group, name, version triple identifying the plugin
  - Flag for whether discovery is allowed to fail (false by default). If this flag is `false` and this device resource's discovery fails, then the entire discovery process fails. If this flag is `true` and this device resource's discovery fails, then the discovery process continues.
- Script schema:
  - File specifier

#### Hardware Plugins

- A device plugin implements the DeviceType interface which specifies:
  - What attachment points are available
  - If a combination of device resources is valid
  - Based on [DeviceType](https://github.com/CommonWealthRobotics/bowler-kernel/blob/b696f17b842ccd3ff00ed2e4afb56615de7f858d/bowler-kernel/hardware/src/main/kotlin/com/neuronrobotics/bowlerkernel/hardware/device/deviceid/DeviceType.kt) and  [DefaultDeviceTypes](https://github.com/CommonWealthRobotics/bowler-kernel/blob/b696f17b842ccd3ff00ed2e4afb56615de7f858d/bowler-kernel/hardware/src/main/kotlin/com/neuronrobotics/bowlerkernel/hardware/device/deviceid/DefaultDeviceTypes.kt)
- A device resource plugin implements the DeviceResourceType interface which specifies:
  - The type of the resource (Byte)
  - The send length (Byte)
  - The receive length (Byte)
  - Based on [ResourceType](https://github.com/CommonWealthRobotics/bowler-kernel/blob/b696f17b842ccd3ff00ed2e4afb56615de7f858d/bowler-kernel/hardware/src/main/kotlin/com/neuronrobotics/bowlerkernel/hardware/deviceresource/resourceid/ResourceType.kt) and [DefaultResourceTypes](https://github.com/CommonWealthRobotics/bowler-kernel/blob/b696f17b842ccd3ff00ed2e4afb56615de7f858d/bowler-kernel/hardware/src/main/kotlin/com/neuronrobotics/bowlerkernel/hardware/deviceresource/resourceid/DefaultResourceTypes.kt)
- A toolchain plugin implements the Toolchain interface which specifies:
  - A method for compiling a program and flashing it to a connected device that has arguments for the program (String) and the device (USB port).
  - The toolchain plugin is responsible for downloading/caching/updating its toolchain artifacts. It is expected that toolchain plugin authors release toolchain plugin versions in lockstep with toolchain versions.

#### Loading a Robot

- When loading a robot config file, the kernel will encounter 1 device, 1 toolchain, and 0..* device resources specified as plugins.
  1. The kernel will use the plugin manager to load those plugins. If loading any plugin fails, then loading the robot fails.
  2. The kernel will check that the chosen combination of device resources is compatible with the device using the device plugin. If it is not, then loading the robot fails.
  3. The kernel will check if the device supports all the required device resources using the RPC. If it does not, it will generate a program that includes support for the required device resources and use the toolchain plugin to compile and flash it to the device. If this fails, then loading the robot fails. The kernel will then re-check that the device supports all the required device resources. If it does not, then loading the robot fails. The kernel will not attempt to update the device program more than once.
     - The kernel may be configured to never flash the device. If this configuration is set and the device does not support all the required device resources, then loading the robot fails.
  4. The kernel will attempt to complete the discovery process. If the discovery process fails, then loading the robot fails.
  - There is explicitly no verification of the compatibility between the device plugin and the toolchain plugin. The toolchain cannot be part of the device because we want to let users receive toolchain updates without needing to wait for device updates. The toolchain also can't be required to verify its compatibility with a device because toolchain authors can't be required to know about all the devices in the Bowler ecosystem.

#### RPC

- We should use the Bowler RPC protocol from the previous version of the kernel.
  - Need to add support for asking the device its capabilities so that the kernel can figure out if it needs to upload a new program.

### Simulation

- A simulator plugin implements the Simulator interface which specifies:
  - A method for initializing the simulator with a robot config file
  - ! RFC: What does the simulator interface need?
- The kernel must be able to download simulator plugins.
  - We can ship the default simulator plugin with the IntelliJ plugin, but there must be a way to download any of them because of the [usability requirement](#usability).

### Headless Execution

- The kernel must not include any references to JavaFX so that it can run in a headless environment.

### Data Logging

- Every connected device resource must be automatically available as a data source. The resources should be loaded from the robot config file.
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
  - Aggregate data types: collections, maps.
  - Arbitrary bytes.
- Data that has been logged must be able to be saved and exported to other formats (e.g., CSV) if the data type supports it (e.g., when exporting to a CSV you can export numerical data but not images).
- The amount of data to buffer must be configurable via the settings.

### CAD

- Use the existing JCSG API for now.

### Installing and Updating

#### Published Artifacts

! RFC: Refine this

- A shadow Jar that is cross-platform for desktop-class Windows, macOS, and Linux.
- A shadow Jar for each embedded computer we support.
- An image for each embedded computer we support.
- deb and rpm packages that install the auto-updater for the kernel.

#### Updating

! RFC: Refine this

- We want to use an auto-updater because there is no cross-platform auto-updating unless we write it ourselves.
  - The IntelliJ plugin should not auto-update. We should use the plugin marketplace for that.
  - The kernel and the display can auto-update. If the IntelliJ plugin is installed, the auto-updater shouldn't install a version of the kernel that is incompatible with the plugin. The auto-updater also shouldn't install a version of the display that is incompatible with the kernel.
  - If the kernel is on another machine than the IntelliJ plugin, we need the ability to remotely update the kernel.

#### Installing on Embedded Systems

- If the user doesn't or can't use one of our images for their embedded system, they should be able to run a script that configures the kernel to start as a systemd service or whatever is relevant to their operating system. ! RFC: Refine this

## Non-Functional Requirements

### Open Source

- All core components and their dependencies must be open-source under a compatible license. Some optional, non-core components may have closed-source dependencies.

### Portability

- x86_64 Windows, macOS, and Linux must be supported. Arm linux must be supported.

### Responsiveness

- The kernel's runtime must not pause for longer than 15 milliseconds when running under ZGC. Runtimes that must run with another garbage collector, like G1, do not have this requirement.

### Usability

- Changing the device resources (e.g., changing the type of a servo, moving a sensor or actuator to a different pin) must not require recompiling and/or reflashing code to the microcontroller.
