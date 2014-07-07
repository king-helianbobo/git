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
CMAKE_SOURCE_DIR = /home/liubo/git/hadoop-2.4.0/hadoop-hdfs-project/hadoop-hdfs/src

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /home/liubo/git/hadoop-2.4.0/hadoop-hdfs-project/hadoop-hdfs/target/native

# Include any dependencies generated for this target.
include CMakeFiles/test_libhdfs_write.dir/depend.make

# Include the progress variables for this target.
include CMakeFiles/test_libhdfs_write.dir/progress.make

# Include the compile flags for this target's objects.
include CMakeFiles/test_libhdfs_write.dir/flags.make

CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.o: CMakeFiles/test_libhdfs_write.dir/flags.make
CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.o: /home/liubo/git/hadoop-2.4.0/hadoop-hdfs-project/hadoop-hdfs/src/main/native/libhdfs/test/test_libhdfs_write.c
	$(CMAKE_COMMAND) -E cmake_progress_report /home/liubo/git/hadoop-2.4.0/hadoop-hdfs-project/hadoop-hdfs/target/native/CMakeFiles $(CMAKE_PROGRESS_1)
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Building C object CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.o"
	/usr/bin/gcc  $(C_DEFINES) $(C_FLAGS) -o CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.o   -c /home/liubo/git/hadoop-2.4.0/hadoop-hdfs-project/hadoop-hdfs/src/main/native/libhdfs/test/test_libhdfs_write.c

CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.i"
	/usr/bin/gcc  $(C_DEFINES) $(C_FLAGS) -E /home/liubo/git/hadoop-2.4.0/hadoop-hdfs-project/hadoop-hdfs/src/main/native/libhdfs/test/test_libhdfs_write.c > CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.i

CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.s"
	/usr/bin/gcc  $(C_DEFINES) $(C_FLAGS) -S /home/liubo/git/hadoop-2.4.0/hadoop-hdfs-project/hadoop-hdfs/src/main/native/libhdfs/test/test_libhdfs_write.c -o CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.s

CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.o.requires:
.PHONY : CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.o.requires

CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.o.provides: CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.o.requires
	$(MAKE) -f CMakeFiles/test_libhdfs_write.dir/build.make CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.o.provides.build
.PHONY : CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.o.provides

CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.o.provides.build: CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.o

# Object files for target test_libhdfs_write
test_libhdfs_write_OBJECTS = \
"CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.o"

# External object files for target test_libhdfs_write
test_libhdfs_write_EXTERNAL_OBJECTS =

test_libhdfs_write: CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.o
test_libhdfs_write: target/usr/local/lib/libhdfs.so.0.0.0
test_libhdfs_write: /usr/lib/jvm/java-7-openjdk-amd64/jre/lib/amd64/jamvm/libjvm.so
test_libhdfs_write: CMakeFiles/test_libhdfs_write.dir/build.make
test_libhdfs_write: CMakeFiles/test_libhdfs_write.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --red --bold "Linking C executable test_libhdfs_write"
	$(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/test_libhdfs_write.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
CMakeFiles/test_libhdfs_write.dir/build: test_libhdfs_write
.PHONY : CMakeFiles/test_libhdfs_write.dir/build

CMakeFiles/test_libhdfs_write.dir/requires: CMakeFiles/test_libhdfs_write.dir/main/native/libhdfs/test/test_libhdfs_write.c.o.requires
.PHONY : CMakeFiles/test_libhdfs_write.dir/requires

CMakeFiles/test_libhdfs_write.dir/clean:
	$(CMAKE_COMMAND) -P CMakeFiles/test_libhdfs_write.dir/cmake_clean.cmake
.PHONY : CMakeFiles/test_libhdfs_write.dir/clean

CMakeFiles/test_libhdfs_write.dir/depend:
	cd /home/liubo/git/hadoop-2.4.0/hadoop-hdfs-project/hadoop-hdfs/target/native && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/liubo/git/hadoop-2.4.0/hadoop-hdfs-project/hadoop-hdfs/src /home/liubo/git/hadoop-2.4.0/hadoop-hdfs-project/hadoop-hdfs/src /home/liubo/git/hadoop-2.4.0/hadoop-hdfs-project/hadoop-hdfs/target/native /home/liubo/git/hadoop-2.4.0/hadoop-hdfs-project/hadoop-hdfs/target/native /home/liubo/git/hadoop-2.4.0/hadoop-hdfs-project/hadoop-hdfs/target/native/CMakeFiles/test_libhdfs_write.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : CMakeFiles/test_libhdfs_write.dir/depend

