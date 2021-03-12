//
// Created by adirben on 1/11/18.
//

#ifndef BOOST_ECHO_CLIENT_KEYBOARDTHREAD_H
#define BOOST_ECHO_CLIENT_KEYBOARDTHREAD_H

#endif //BOOST_ECHO_CLIENT_KEYBOARDTHREAD_H

#include "../include/connectionHandler.h"

class KeyboardThread{

        private:
        ConnectionHandler &con;

        public:
        KeyboardThread(ConnectionHandler &con);

        void operator()();
}; //class

