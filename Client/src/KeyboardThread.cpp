#include <iostream>
#include "../include/KeyboardThread.h"

//
// Created by adirben on 1/11/18.
//

KeyboardThread::KeyboardThread(ConnectionHandler &con):con(con){}

void KeyboardThread::operator()() {

    while (!std::cin.eof()) {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        if (!con.sendLine(line)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
    }
}



