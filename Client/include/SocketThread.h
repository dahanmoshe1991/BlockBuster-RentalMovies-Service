//
// Created by adirben on 1/11/18.
//

#ifndef BOOST_ECHO_CLIENT_SOCKETTHREAD_H
#define BOOST_ECHO_CLIENT_SOCKETTHREAD_H

#endif //BOOST_ECHO_CLIENT_SOCKETTHREAD_H

#include "../include/connectionHandler.h"

class SocketThread{

private:
    ConnectionHandler &con;

public:
    SocketThread(ConnectionHandler &con);

    void operator()();
}; //class

