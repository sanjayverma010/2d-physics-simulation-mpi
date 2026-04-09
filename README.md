# Distributed 2D Physics Simulation using MPI

## Overview

This project implements a scalable distributed 2D physics simulation engine using MPI (Message Passing Interface). It focuses on solving the computational bottleneck of collision detection (O(n²)) using parallel processing.

---

## Key Features

* Parallel physics simulation using MPI
* Efficient collision detection
* Distributed object computation
* Performance comparison (Sequential vs Parallel)
* Speedup and scalability analysis

---

## Core Concepts

* Object motion simulation
* Gravity-based velocity updates
* Distance-based collision detection
* Parallel computing using MPI
* Synchronization using MPI_Allgather and MPI_Reduce

---

## System Architecture

```
Input → Physics Engine → Collision Detection → Output
                ↓
        Sequential / Parallel Engine
```

### Components

* Body Class: Stores position, velocity, radius
* Sequential Engine: Single processor execution
* Parallel Engine: Multi-process MPI execution
* MPI Layer: Communication and synchronization

---

## Technologies Used

* Java
* MPI (MPJ Express)
* Parallel Computing
* Distributed Systems

---

## How to Run

### Compile

```bash
javac *.java
```

### Run Sequential Version

```bash
java SequentialPhysics
```

### Run Parallel Version

```bash
mpjrun -np 4 ParallelPhysics
```

---

## Performance Results

| Version      | Execution Time |
| ------------ | -------------- |
| Sequential   | 255 sec        |
| Parallel (4) | 139 sec        |

Speedup ≈ 1.83x

---

## Why Not Linear Speedup

* Communication overhead
* Synchronization delay
* Shared collision dependency

---

## Project Structure

```
├── SequentialPhysics.java
├── ParallelPhysics.java
├── Body.java
├── utils/
├── data/
└── README.md
```

---

## Demo

![Demo](demo.gif)

Full Video: 

---

## References

* Distributed Systems – Tanenbaum
* Game Engine Architecture – Jason Gregory
* Real-Time Collision Detection – Christer Ericson

---

## Author

Sanjay Verma
B.Sc (Computer Science)
Email: [rssanjayverma010@gmail.com](mailto:rssanjayverma010@gmail.com)
GitHub: https://github.com/sanjayverma010

---

## Contribution

Feel free to fork this repository and improve performance or add visualization.

---

## Future Improvements

* GPU acceleration (CUDA/OpenCL)
* Spatial partitioning (QuadTree)
* Real-time visualization
* 3D simulation support

---

## Support

If you find this project useful, consider starring the repository.
