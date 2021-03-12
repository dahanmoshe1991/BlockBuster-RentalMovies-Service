#include <stdlib.h>
#include "../include/connectionHandler.h"
#include <boost/thread.hpp>
#include "../include/KeyboardThread.h"
#include "../include/SocketThread.h"

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
int main (int argc, char *argv[]) {

    /*if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }*/
    std::string host = "127.0.0.1";//argv[1];
    short port = 7777;//atoi(argv[2]);

    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    KeyboardThread KeyboardThread(connectionHandler);
    SocketThread SocketThread(connectionHandler);
    boost::thread thKT(KeyboardThread);                         //constructor thread1
    boost::thread thS(SocketThread);                            //constructor thread2
    thS.join();

    return 0;
}


