## 3.x

## 3.0.x

### 3.0.1

- Fix a bug that may cause segmentation fault when getting local ip.

## 3.1.x

### 3.1.0

- Now `cat` can be initialized in child process after it has been forked despite `cat` had already been initialized in the master process.
- A new option `enableAutoInitialize` is offered to automatically initialize cat after each child process has been forked.

### 3.1.1

- Fix some probable bugs of HTTP protocol.
