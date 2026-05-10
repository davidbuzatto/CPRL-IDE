# CPRL IDE

A lightweight integrated development environment for the **CPRL** (CPRL Programming Language) built in Java using the NetBeans GUI builder.

CPRL is a small, Pascal-like programming language designed for educational purposes, introduced in the book *Compiler Design Using Java®: An Object-Oriented Approach* by [John I. Moore, Jr.](https://github.com/SoftMoore) ([latest edition on Amazon](https://www.amazon.com/Compiler-Design-Using-Java-Object-Oriented/dp/1734139137)). This IDE is based on the **second edition** of the book and wraps the full CPRL toolchain — compiler, assembler, virtual machine, and disassembler — into a single desktop application with an internal console and syntax highlighting.

---

## Features

- **Syntax highlighting** — keywords, types, literals, operators, and comments are highlighted using a custom `AbstractTokenMaker` for CPRL.
- **Multi-tab editor** — open and edit multiple `.cprl` source files simultaneously, each with its own console and assembly view.
- **Integrated pipeline** — compile, assemble, and run with a single action; stale artifacts (`.asm`, `.obj`, `.dis`) are automatically deleted before each build so failed compilations never silently execute old code.
- **Internal console** — all pipeline output (compiler messages, assembler messages, runtime output, and errors) is redirected to a per-tab console pane. Standard input is also handled interactively via a text field inside the console area.
- **Assembly view** — after a successful build, the generated `.asm` source is displayed in a read-only side panel.
- **Disassembler** — disassembles the `.obj` bytecode file and opens the result in a separate viewer window.
- **File management** — New, Open, Save, Save As, and Save All, with dirty-state tracking (unsaved tabs are marked with `*`).
- **Dark theme** — powered by [FlatLaf](https://www.formdev.com/flatlaf/) Dark.

---

## Keyboard Shortcuts

| Shortcut | Action |
|---|---|
| `Ctrl+N` | Create new file |
| `Ctrl+O` | Open files |
| `Ctrl+S` | Save the active file |
| `Ctrl+Shift+S` | Save all open files |
| `Shift+F5` | Compile the active file |
| `Shift+F6` | Compile and run the active file |
| `Shift+F7` | Disassembly the active file |

---

## Toolbar Buttons

| Button | Action |
|---|---|
| New | Create a new untitled CPRL file |
| Open | Open an existing `.cprl` file |
| Save | Save the active file |
| Save As | Save the active file under a new name |
| Save All | Save all open files |
| Compile | Compile and assemble only (no execution) |
| Compile & Run | Compile, assemble, and run on the CVM |
| Disassemble | Compile, assemble, and disassemble the object code |

---

## Pipeline

Each build action follows these steps:

1. **Save** the current file.
2. **Delete** any stale `.asm`, `.obj`, and `.dis` artifacts from a previous build.
3. **Compile** the `.cprl` source into a `.asm` assembly file.
4. **Assemble** the `.asm` file into a `.obj` bytecode file.
5. *(Compile & Run)* **Execute** the `.obj` file on the CPRL Virtual Machine (CVM).
6. *(Disassemble)* **Disassemble** the `.obj` file and open the result in a viewer.

All output produced during steps 3–6 is written to the internal console of the active tab.

---

## Requirements

- **Java 17** or later
- The **FullCPRL** library (`FullCPRL.jar`) — provides the CPRL compiler, assembler, CVM, and disassembler from the Citadel CPRL project.
- [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea) — syntax-highlighting text editor component.
- [FlatLaf](https://www.formdev.com/flatlaf/) — modern Swing look and feel.

---

## Building

The project is structured as a **NetBeans** project. Open it in NetBeans, resolve the libraries under `lib/`, and run or build normally.

---

## Author

**Prof. Dr. David Buzatto**
