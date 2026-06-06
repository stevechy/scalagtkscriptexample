# Example Scala 3 GTK script

A quick example of using Scala 3 with GTK4 using the Java-GI bindings.

It uses the podman cli to list and run containers on Linux. 

The script also starts up on WSL Ubuntu though it's probably less useful there.

# Usage

Install gtk dev libraries:
- Ubuntu: `sudo apt install libgtk-4-dev`
- Fedora: `sudo dnf install gtk4-devel`

Run:

```aiignore
./mill podmanutil
```