******************** SPL 2018 ***********************

***********BlockBuster RentalMovies Service**********

In this project I implemented an online movie rental service (R.I.P. Blockbuster) server and client. 
The communication between the server and the client(s) is performed using a text based communication protocol, which will support renting, listing
and returning of movies.

The implementation of the server is on the Thread-Per-Client (TPC) and Reactor servers (you can choose depending on startup). 
The servers, do not support bi-directional message passing. Any time the server receives a message from a client it can reply back
to that specific client itself.
Also supported to send messages between clients, or broadcast an announcment to all clients.

This implementation of the movie rental service is performed over User service textbased protocol. 
The User Service Text-based protocol is the base protocol which will define the message structure and base command. 

Since the service requires data to be saved about each user and available movies for rental, I implemented a simple JSON text database which will be
read when the server starts and updated each time a change is made.


********* User Service Text based protocol *************

1. Establishing a client/server connection
Upon connecting, a client must identify themselves to the system. In order to identify, a
user must be registered in the system. The LOGIN command is used to identiy. Any
command (except for REGISTER) used before the login is complete will be rejected by the
system.

2. Message encoding
A message is defined by a list of characters in UTF-8 encoding following the special
character ‘\n’. This is a very simple message encoding pattern that was seen in class.

3. Supported Commands
In the following section we will entail a list of commands supported by the User Service
Text-based protocol. Each of these commands will be sent independently within the
encoding defined in the previous section. (User examples appear at the end of the
assignment)

Annotations:
	• <x> – defines mandatory data to be sent with the command
	• [x] – defines optional data to be sent with the command
	• “x” – strings that allow a space or comma in complex commands will be wrapped
		with quotation mark (more than a single argument)
	• x,… - defines a variable list of arguments
	
Server commands:
	All ACK and ERROR message may be extended over the specifications, but the message
	prefix must match the instructions:
	1) ACK [message]
		The acknowledge command is sent by the server to reply to a successful request by a
		client. Specific cases are noted in the Client commands section.
		
	2) ERROR <error message>
		The error command is sent by the server to reply to a failed request. Specific cases are
		noted in the Client commands section.
		
	3) BROADCAST <message>
		The broadcast command is sent by the server to all logged in clients. Specific cases are
		noted in the Client commands section.

Client commands:
	1) REGISTER <username> <password> [Data block,…]
		Used to register a new user to the system.
		• Username – The user name.
		• Password – the password.
		• Data Block – An optional block of additional information that may be used by the service.

		In case of failure, an ERROR command will be sent by the server: ERROR registration failed
		Reasons for failure:
			1. The client performing the register call is already logged in.
			2. The username requested is already registered in the system.
			3. Missing info (username/password).
			4. Data block does not fit service requirements (defined in rental service section).

		In case of successful registration an ACK command will be sent: ACK registration succeeded

	2) LOGIN <username> <password>
		Used to login into the system.
		• Username – The username.
		• Password – The password.
		In case of failure, an ERROR command will be sent by the server: ERROR login failed
		Reasons for failure:
		1. Client performing LOGIN command already performed successful LOGIN
		command.
		2. Username already logged in.
		3. Username and Password combination does not fit any user in the system.
		In case of a successful login an ACK command will be sent: ACK login succeeded
		
	3) SIGNOUT
		Sign out from the server.
		In case of failure, an ERROR command will be sent by the server: ERROR signout failed
		Reasons for failure:
		1. Client not logged in.
		In case of successful sign out an ACK command will be sent: ACK signout succeeded
		After a successful ACK for sign out the client should terminate!
		
	7) REQUEST <name> [parameters,…]
		A general call to be used by clients. For example, our movie rental service will use it for
		its applications. The next section will list all the supported requests.
		• Name – The name of the service request.
		• Parameters,.. – specific parameters for the request.
		In case of a failure, an ERROR command will be sent by the server:
		ERROR request <name> failed
		Reasons for failure:
		1. Client not logged in.
		2. Error forced by service requirements (defined in rental service section).
		In case of successful request an ACK command will be sent. Specific ACK messages are
		listed on the service specifications
		
		
****** Movie Rental Service *****

1. Overview
	Our server will maintain two datasets with JSON text files. One file will contain the user
	information and the other the movie information. More about the files and the JSON
	format in the next section. A new user must register in the system before being able to
	login. Once registered, a user can use the login command to identify themselves and
	start interacting with the system using the REQUEST commands.
	
2 Service REGISTER data block command
	When a REGISTER command is processed the user created will be a normal user with
	credit balance 0 by default.
	The service requires additional information about the user and the data block is where
	the user inserts that information. In this case, the only information we save on a specific
	user that is recieved from the REGISTER command is the users origin country.
	REGISTER <username> <password> country=”<country name>”
	
3 Normal Service REQUEST commands
	The REQUEST command is used for most of the user operations. This is the list of service
	specific request and their response messages. These commands are available to all logged
	in users.
	1) REQUEST balance info
		Server returns the user’s current balance within an ACK message:
			ACK balance <balance>
	2) REQUEST balance add <amount>
		Server adds the amount given to the user’s balance. The server will return an ACK
			message: ACK balance <new balance> added <amount>
	Note: the new balance should be calculated after the added amount. I assumed the amount is always a number greater than zero.
	
	3) REQUEST info “[movie name]”
		Server returns information about the movies in the system. If no movie name was given
		a list of all movies’ names is returned (even if some of them are not available for rental).
		If the request fails an ERROR message is sent.
		
		Reasons of failure:
			1. The movie does not exist
			If the request is successful, the user performing the request will receive an ACK command:
			ACK info <”movie name”,…>.
			If a movie name was given: ACK info <”movie name”> <No. copies left> <price> <”banned country”,…>
			
	4) REQUEST rent <”movie name”>
		Server tries to add the movie to the user rented movie list, remove the cost from the
		user’s balance and reduce the amount available for rent by 1. If the request fails an ERROR
		message is sent.

		Reasons for failure:
			1. The user does not have enough money in their balance
			2. The movie does not exist in the system
			3. There are no more copies of the movie that are available for rental
			4. The movie is banned in the user’s country
			5. The user is already renting the movie
			If the request is successful, the user performing the request will receive an ACK command:
				ACK rent <”movie name”> success. The server will also send a broadcast to all logged-in
				clients: BROADCAST movie <”movie name”> < No. copies left > <price>
	
	5) REQUEST return <”movie name”>
		Server tries to remove the movie from the user rented movie list and increase the amount
		of available copies of the movies by 1. If the request fails an ERROR message is sent.

		Reasons of failure:
			1. The user is currently not renting the movie
			2. The movie does not exist
		
		If the request is successful, the user performing the request will receive an ACK command:
		ACK return <”movie name”> success. The server will also send a broadcast to all loggedin clients: BROADCAST movie <”movie name”> <No. copies left> <price>
	
4 Admin Service REQUEST commands
	These commands are only eligible to a user marked as admin. They are meant to help a
	remote super user to manage the list of movies. Any time a normal user attempts to run
	one of the following commands it will result in an error message.
	
	1) REQUEST addmovie <”movie name”> <amount> <price> [“banned country”,…]
		The server adds a new movie to the system with the given information. The new movie
		ID will be the highest ID in the system + 1. If the request fails an ERROR message is sent.
		Reason to failure:
	
			1. User is not an administrator
			2. Movie name already exists in the system
			3. Price or Amount are smaller than or equal to 0 (there are no free movies)

		If the request is successful, the admin performing the request will receive an ACK
		command: ACK addmovie <”movie name”> success. The server will also send a broadcast
		to all logged-in clients: BROADCAST movie <”movie name”> <No. copies left> <price>
	
	2) REQUEST remmovie <”movie name”>
	Server removes a movie by the given name from the system. If the request fails an ERROR
	message is sent.
	
	Reason to failure:
		1. User is not an administrator
		2. Movie does not exist in the system
		3. There is (at least one) a copy of the movie that is currently rented by a user
	
	If the request is successful, the admin performing the request will receive an ACK
	command: ACK remmovie <”movie name”> success. The server will also send a broadcast
	to all logged-in clients: BROADCAST movie <”movie name”> removed
	
	3) REQUEST changeprice <”movie name”> <price>
	Server changes the price of a movie by the given name. If the request fails an ERROR
	message is sent.
	
	Reason to failure:
		1. User is not an administrator
		2. Movie does not exist in the system
		3. Price is smaller than or equal to 0
	
	If the request is successful, the admin performing the request will receive an ACK
	command: ACK changeprice <”movie name”> success. The server will also send a
	broadcast to all logged-in clients: BROADCAST movie <”movie name”> <No. copies left>
	<price>

	
*******Our JSON data*****

In this project, I have two JSON files that are in the server-side. One is
“Users.json”, which stores information about the customers registered to the online
store. The other is “Movies.json”, which stores information about the warehouse, i.e.
movies that the online store offers and information about them.
Every change in the state of the store must be updated into the files (movie rented,
movie returned, movie removed, user registered etc.)

Users.json example:
	Please see the supplied file example_Users.json
	
	The file implies that the store currently contains 3 users:
	1. User “john”, an admin, with password “potato”, from the United States, no
	movies rented and has a $0 balance.
	2. User “lisa”, a normal user (customer), with password “chips123”, from Spain,
	currently has (by rent) the movies “The Pursuit of Happyness” (movie id 2) and
	“The Notebook” (movie id 3), and has a balance of $37.
	3. User “shlomi”, a normal user (customer), with password “cocacola”, from Israel,
	currently has (by rent) the movies “The Godfather” (movie id 1) and “The Pursuit
	of Happyness” (movie id 2), and has a balance of $112.

Movies.json example:
	Please see the supplied file example_Movies.json
	
	The file implies that the store currently contains 4 movies:
	1. The movie “The Godfather”, of price 25, which is banned in both the United
	Kingdom and Italy. The immediate amount available for rental is 1, and the total
	number of copies the store owns is 2 (but one of them is currently rented by the
	user shlomi as seen in the previous Users.json file)
	2. The movie “The Pursuit of Happyness”, of price 14, which is not banned in any
	country. The immediate amount available for rental is 3, and the total number of
	copies the store owns is 5 (but two of them are currently rented by users
	shlomi and lisa)
	3. The movie “The Notebook”, of price 5, which is not banned in any country. The
	immediate amount available for rental is zero (none), and the total number of
	copies the store owns is 1 (it is rented by lisa)
	4. The movie “Justice League”, of price 17, which is banned in Jordan, Iran and
	Lebanon. The immediate amount available for rental is 4, and the total number
	of copies the store owns is 4 (no one is renting the movie currently)
	
	
*******Technical Implementation Details******

SERVER:
	• The server is written in Java. The client is written in C++ with BOOST. 
		Both tested on Linux.

	• I used maven as your build tool for the server and Makefile for the C++ client.

	• The Server side supports the Thread-Per-Client and Reactor server patterns.

	*******run commands******

	• Reactor server:
		mvn exec:java -Dexec.mainClass=”bgu.spl181.net.impl.BBreactor.ReactorMain” - Dexec.args=”<port>”

	• Thread per client server:
		mvn exec:java -Dexec.mainClass=”bgu.spl181.net.impl.BBtpc.TPCMain” - Dexec.args=”<port>”

	The server directory should contain a pom.xml file, a Database directory (for Movies.json and Users.json) and the src directory. 
	Compilation will be done from the server folder using:
		mvn compile

CLIENT:
	echo client which runa 2 threads. One read from keyboard while the other read from socket. 
	Both threads writes to the socket. The client receive the server’s IP and PORT as arguments. 
	You may assume a network disconnection does not happen (like disconnecting the network cable).
	The client should recive commands using the standard input. 
	Commands are defined in previous sections. The client should print to screen any message coming from the server (ACK’s, ERROR’s and BROADCAST’s). 
	Notice that the client not close until he recives an ACK packet for the SIGNOUT call.

	The Client directory should contain a src, include and bin subdirectories and a Makefile
	as shown in class. The output executable for the client is named BBclient and should
	reside in the bin folder after calling make.

	*******run commands***********
		bin/BBclient <ip> <port>
		
		
		
		
***********Examples**********
	The following section contains examples of commands running on client. It assumes that
	the software opened a socket properly and a connection has been initiated.
	We use “>” for keyboard input and “<” for screen output at the client side only. Server
	and client actions are explained in between.
	
	Assume that the starting state of the Server is as presented in the example database
	files shown above.

	
	1 Failed register, login, balance and movie info, rent and return a copy

	Further assumptions:
	• The current client is not logged in yet.
	• The user shlomi is not logged in.
		> REGISTER shlomi tryingagain country="Russia"
		< ERROR registration failed
		(registration failed because the username shlomi is already taken)
		> REQUEST balance info
		(server checks if the user is logged in)
		< ERROR request balance failed
		(it failed because the user is not logged in)
		> LOGIN shlomi mahpass
		(server checks user-pass combination)
		< ERROR login failed
		(it failed because the password is wrong)
		> LOGIN shlomi cocacola
		< ACK login succeeded
		> REQUEST balance info
		< ACK balance 112
		> LOGIN shlomi moipass
		< ERROR login failed
		(this client is already logged in as shlomi)
		> REQUEST info
		< ACK info "The Godfather" "The Pursuit Of Happyness" "The Notebook" "Justice
		League"
		> REQUEST info "The Notebook"
		< ACK info "The Notebook" 0 5
		> REQUEST rent "The Notebook"
		< ERROR request rent failed
		(it failed because there are no available copies)
		> REQUEST rent "Justice League"
		< ACK rent "Justice League" success
		(at this point the file Users.json is updated that
		shlomi has rented "Justice League", his balance
		is lowered from 112 to 95 and the file
		Movies.json is updated that there is one less copy
		available of Justice League)
		< BROADCAST movie "Justice League" 3 17
		> REQUEST balance info
		< ACK balance 95
		> REQUEST changeprice “The Notebook” 22
		< ERROR request changeprice failed
		(because shlomi is not an admin)
		> REQUEST return "The Notebook"
		< ERROR request return failed
		(because shlomi does not own The Notebook)
		> REQUEST info "The Godfather"
		< ACK info "The Godfather" 1 25 "united kingdom" "italy"
		> REQUEST return "The Godfather"
		< ACK return "The Godfather" success
		< BROADCAST movie "The Godfather" 2 25
		> REQUEST balance info
		< ACK balance 95
		< BROADCAST movie “The Godfather” removed
		(an admin, which is not the current user, removed The Godfather from the available
		movies)
		> SIGNOUT
		< ACK signout succeeded
		(client’s app closes at this stage)

	7.2 Successfully registered, add balance, try to rent a forbidden movie in
	the country
	Further assumptions:
	• The current client is not logged in yet.
		> REGISTER steve mypass country="iran"
		< ACK registration succeeded
		(remember to update Users.json)
		> REQUEST balance info
		< ERROR request balance failed
		(it failed because the user has not logged in yet)
		> LOGIN steve mypass
		< ACK login succeeded
		> REQUEST balance info
		< ACK balance 0
		> REQUEST balance add 50
		< ACK balance 50 added 50
		< BROADCAST movie "The Godfather" 2 25
		(some user, which is not the current user, rented or returned The Godfather)
		> REQUEST rent "Justice League"
		< ERROR request rent failed
		(because Steve is from Iran and Justice League is banned there)
		> SIGNOUT
		< ACK signout succeeded
		(client’s app closes at this stage)
	
	7.3 Admin: a simple example
	• The client is not logged in yet
	• The admin (user john) is not logged in
		> LOGIN john potato
		< ACK login succeeded
		> REQUEST remmovie “The Godfather"
		< ERROR request remmovie failed
		(because The Godfather has a copy rented by shlomi)
		> REQUEST remmovie "Justice League"
		< ACK remmovie "Justice League" success
		(succeeds because no one has rented this movie yet)
		< BROADCAST movie "Justice League" removed
		(remember that even the admin is a user, that’s why he received a broadcast as well)
		> REQUEST addmovie “South Park: Bigger, Longer & Uncut” 30 9 “Israel” “Iran” “Italy”
		< ACK addmovie “South Park: Bigger, Longer & Uncut” success
		< BROADCAST movie " South Park: Bigger, Longer & Uncut" 30 9
		> SIGNOUT
		< ACK signout succeeded
		(client’s app closes at this stage)
