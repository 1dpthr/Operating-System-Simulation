# Operating System Simulation

A comprehensive Java-based Operating System simulation project that demonstrates core OS concepts including memory management, process scheduling, inter-process communication, and distributed systems.

## Features

### Memory Management
- **Contiguous Memory Allocation** - First fit, best fit, and worst fit algorithms
- **Non-Contiguous Memory Allocation** - Paging and segmentation
- **Memory Sharing** - Shared memory segments and mapped files
- **Page Replacement Algorithms** - LRU, FIFO, and optimal page replacement
- **Page Table Management** - Virtual to physical address translation

### Process Scheduling
- **FCFS (First Come First Serve)** - Live simulator with Gantt chart visualization
- **Multi-Level Scheduling** - Multi-level queue scheduling algorithm
- **Process Control Block (PCB)** - Process state management
- **Process Registry** - Process creation and tracking

### Inter-Process Communication (IPC)
- **Shared Memory IPC** - Direct memory sharing between processes
- **Message Passing IPC** - Synchronous and asynchronous message passing
- **Message Queue Kernel** - Kernel-managed message queues
- **Mailbox IPC** - Email-style message passing system
- **Direct IPC** - Direct process-to-process communication

### Distributed Systems
- **Distributed Kernel Server** - Network-based kernel services
- **Socket-based IPC** - TCP/IP socket communication
- **Remote Process Service** - Remote process management

### Additional Features
- **Interrupt Handling** - Simulated interrupt mechanism
- **Process Communication GUI** - Interactive visualization of IPC mechanisms
- **Live Simulators** - Real-time visualization of scheduling and memory allocation
- **Configuration Management** - Customizable kernel parameters

## Technologies Used

- **Java 19** - Core programming language
- **JavaFX 21.0.6** - GUI framework for interactive simulations
- **FlatLaf 3.5.2** - Modern Look and Feel for Swing applications
- **Maven** - Build automation and dependency management
- **Swing** - Legacy GUI components

## Project Structure

```
Operating-System-Simulation/
├── src/main/java/
│   ├── PHH1.java                    # Main application entry point
│   ├── PHH2.java                    # Secondary GUI interface
│   ├── ContiguosMemory.java         # Contiguous memory management
│   ├── noncontigious.java           # Non-contiguous memory (paging)
│   ├── MemoryAllocationAlgorithms.java  # Memory allocation strategies
│   ├── PageReplacementAlgorithms.java   # Page replacement algorithms
│   ├── PageTableManager.java        # Page table management
│   ├── MultiLevelScheduler.java     # Multi-level queue scheduler
│   ├── Schedulingg.java             # Scheduling algorithms
│   ├── FcfsLiveSimulator.java       # FCFS live simulation
│   ├── SchedulingLiveSimulator.java # Scheduling visualization
│   ├── SharedMemoryIPC.java         # Shared memory IPC
│   ├── MessagePassingIPC.java       # Message passing IPC
│   ├── MessageQueueKernel.java      # Message queue implementation
│   ├── OSMailbox.java               # Mailbox IPC system
│   ├── DirectIPC.java               # Direct IPC mechanism
│   ├── DistributedKernelServer.java # Distributed kernel services
│   ├── ProcessControlBlock.java     # PCB implementation
│   ├── ProcessRegistry.java         # Process management
│   ├── interrupt.java               # Interrupt handling
│   └── [additional GUI and utility classes]
├── pom.xml                          # Maven configuration
└── README.md                        # Project documentation
```

## Building the Project

### Prerequisites
- Java 19 or higher
- Maven 3.6+

### Build Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/1dpthr/Operating-System-Simulation.git
   cd Operating-System-Simulation
   ```

2. **Build with Maven**
   ```bash
   mvn clean compile
   ```

3. **Package as JAR**
   ```bash
   mvn package
   ```

4. **Run the application**
   ```bash
   mvn exec:java
   ```
   
   Or run the packaged JAR:
   ```bash
   java -jar target/SimulationOS-1.0-SNAPSHOT.jar
   ```

## Running with JavaFX

If you encounter JavaFX runtime issues, ensure you have the correct VM options:

```bash
java --module-path /path/to/javafx-sdk-21/lib --add-modules javafx.controls,javafx.fxml,javafx.swing -jar target/SimulationOS-1.0-SNAPSHOT.jar
```

## Modules Overview

### Memory Management Module
- Visualize memory allocation and deallocation
- Compare different allocation algorithms
- Simulate page faults and replacement
- View page tables and memory maps

### Scheduling Module
- Simulate various scheduling algorithms
- Visualize Gantt charts
- Calculate average waiting and turnaround times
- Compare algorithm performance

### IPC Module
- Demonstrate different IPC mechanisms
- Visualize message passing between processes
- Simulate synchronization issues
- Compare IPC performance

### Distributed OS Module
- Network-based process communication
- Remote kernel services
- Socket programming demonstration

## Educational Purpose

This project is designed for educational purposes to help students and learners understand:
- How operating systems manage resources
- Process scheduling algorithms and their trade-offs
- Different IPC mechanisms and their use cases
- Memory management strategies
- Distributed operating system concepts

## Screenshots

The application includes multiple interactive GUI screens:
- Main synchronization dashboard
- Memory allocation visualizer
- Scheduling simulator with live charts
- IPC demonstration panels
- Distributed OS interface

## Contributing

Contributions are welcome! Please feel free to submit issues, feature requests, or pull requests.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is available for educational purposes. Please check the repository for specific licensing information.

## Author

Developed as an Operating System simulation project for academic and learning purposes.

## Acknowledgments

- JavaFX community for GUI framework
- FlatLaf for modern UI components
- Maven for build automation

---

**Note**: This is a simulation project intended for educational purposes. It demonstrates OS concepts but is not a production operating system.