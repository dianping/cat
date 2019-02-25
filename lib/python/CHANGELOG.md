## 3.x

## 3.0.x

### 3.0.1

- Fix a bug that may cause segmentation fault when getting local ip.

## 3.1.x

### 3.1.0

- Now `cat` can be initialized in child process after it has been forked despite `cat` had already been initialized in the master process.
- An new option `cat.init(auto_init=True)` is offered to automatically initialize cat after each child process has been forked.

#### Bugfix

- Fixed a bug that may cause exception when calling `transaction.add_data` twice in `python 3.x`

### 3.1.1 && 3.1.2

update documentation.
