CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lboost_thread

# All Targets
all: BBclient

# Tool invocations
# Executable "make" depends on the files make.o and make.o.
BBclient: bin/connectionHandler.o bin/KeyboardThread.o bin/SocketThread.o bin/BBclient.o
	@echo 'Building target: BBclient'
	@echo 'Invoking: C++ Linker'
	g++ -o bin/BBClient bin/connectionHandler.o bin/KeyboardThread.o bin/SocketThread.o bin/BBclient.o $(LDFLAGS)
	@echo 'Finished building target: BBclient'
	@echo ' '

	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/BBclient.o: src/BBclient.cpp
	g++ $(CFLAGS) -o bin/BBclient.o src/BBclient.cpp

bin/KeyboardThread.o: src/KeyboardThread.cpp
	g++ $(CFLAGS) -o bin/KeyboardThread.o src/KeyboardThread.cpp

bin/SocketThread.o: src/SocketThread.cpp
	g++ $(CFLAGS) -o bin/SocketThread.o src/SocketThread.cpp
	
.PHONY: clean
clean:
	rm -f bin/*


