# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 2.8

#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:

# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list

# Suppress display of executed commands.
$(VERBOSE).SILENT:

# A target that is always out of date.
cmake_force:
.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /usr/bin/cmake

# The command to remove a file.
RM = /usr/bin/cmake -E remove -f

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /home/liubo/git/hadoop-2.4.0/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-nodemanager/src

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /home/liubo/git/hadoop-2.4.0/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-nodemanager/target/native

# Include any dependencies generated for this target.
include CMakeFiles/test-container-executor.dir/depend.make

# Include the progress variables for this target.
include CMakeFiles/test-container-executor.dir/progress.make

# Include the compile flags for this target's objects.
include CMakeFiles/test-container-executor.dir/flags.make

CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.o: CMakeFiles/test-container-executor.dir/flags.make
CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.o: /home/liubo/git/hadoop-2.4.0/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-nodemanager/src/main/native/container-executor/test/test-container-executor.c
	$(CMAKE_COMMAND) -E cmake_progress_report /home/liubo/git/hadoop-2.4.0/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-nodemanager/target/native/CMakeFiles $(CMAKE_PROGRESS_1)
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Building C object CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.o"
	/usr/bin/gcc  $(C_DEFINES) $(C_FLAGS) -o CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.o   -c /home/liubo/git/hadoop-2.4.0/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-nodemanager/src/main/native/container-executor/test/test-container-executor.c

CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.i"
	/usr/bin/gcc  $(C_DEFINES) $(C_FLAGS) -E /home/liubo/git/hadoop-2.4.0/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-nodemanager/src/main/native/container-executor/test/test-container-executor.c > CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.i

CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.s"
	/usr/bin/gcc  $(C_DEFINES) $(C_FLAGS) -S /home/liubo/git/hadoop-2.4.0/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-nodemanager/src/main/native/container-executor/test/test-container-executor.c -o CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.s

CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.o.requires:
.PHONY : CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.o.requires

CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.o.provides: CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.o.requires
	$(MAKE) -f CMakeFiles/test-container-executor.dir/build.make CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.o.provides.build
.PHONY : CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.o.provides

CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.o.provides.build: CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.o

# Object files for target test-container-executor
test__container__executor_OBJECTS = \
"CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.o"

# External object files for target test-container-executor
test__container__executor_EXTERNAL_OBJECTS =

target/usr/local/bin/test-container-executor: CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.o
target/usr/local/bin/test-container-executor: libcontainer.a
target/usr/local/bin/test-container-executor: CMakeFiles/test-container-executor.dir/build.make
target/usr/local/bin/test-container-executor: CMakeFiles/test-container-executor.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --red --bold "Linking C executable target/usr/local/bin/test-container-executor"
	$(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/test-container-executor.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
CMakeFiles/test-container-executor.dir/build: target/usr/local/bin/test-container-executor
.PHONY : CMakeFiles/test-container-executor.dir/build

CMakeFiles/test-container-executor.dir/requires: CMakeFiles/test-container-executor.dir/main/native/container-executor/test/test-container-executor.c.o.requires
.PHONY : CMakeFiles/test-container-executor.dir/requires

CMakeFiles/test-container-executor.dir/clean:
	$(CMAKE_COMMAND) -P CMakeFiles/test-container-executor.dir/cmake_clean.cmake
.PHONY : CMakeFiles/test-container-executor.dir/clean

CMakeFiles/test-container-executor.dir/depend:
	cd /home/liubo/git/hadoop-2.4.0/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-nodemanager/target/native && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/liubo/git/hadoop-2.4.0/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-nodemanager/src /home/liubo/git/hadoop-2.4.0/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-nodemanager/src /home/liubo/git/hadoop-2.4.0/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-nodemanager/target/native /home/liubo/git/hadoop-2.4.0/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-nodemanager/target/native /home/liubo/git/hadoop-2.4.0/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-nodemanager/target/native/CMakeFiles/test-container-executor.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : CMakeFiles/test-container-executor.dir/depend

