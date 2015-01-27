07-feb-2011/FK
06-feb-2009/FK
07-feb-2008/FK

INTRODUCTION TO LAB CHAT

The system presented here consists of a chat-server and a client.
The server and the clients are both Jini applications, so they depend
on a Jini infrastructure being present in order to find each other.

Furthermore, both server and client are deployed in a distributed
fashion. This means that they are not installed directly on the
computer that is to run them. A small and general boot-strap program
is installed instead (JarRunner.jar), and when executed with the
correct parameters it fetches the real program from a web server and
runs it.

Your task in this assignmnet, is to select a development task from a
list of alternatives (given separately), and then to modify the system
according to that task. This text describes the technical setup of
the system and how to work with it.

This distribution has two major parts:

  - The application sources and the files needed to compile and build
    them. These files are found in the   develop   subdirectory.

  - Files that start the Jini service infrastructure and runs the
    chat-server and client. These files reside in the   test   sub-
    directory.


There will be several technical points to go through, but before that
some emphasis on things that can be learned from this excercise:

  - An application deployed on-demand from a webserver is much easier
    to maintain for a large group of users, since upgrades only are
    done on the webserver. No need to run around among users or rely
    on their ability to perform the upgrade themselves.

  - A separation of the development and test environment reflects how
    the program will actually be used. When the compiler and the
    runtime are on two different computer systems it is much
    easier to spot incompatibilities between them.


PREPARATION

Initially there are some choices to be made.

    You must select a development platform (default Windows)
    You must choose a webserver (default provided)
    You must select a testing platform (default Windows)


Development platform

  The distribution is set up to work for a PC. This would suit most
  personal laptops. The computer needs to be equipped like this:

  Windows XP or Linux

  Java SDK 1.4 or later
  emacs (or some other source-code editor)
  ant (build tool)

  Ant (Another Neat Tool) is available from ant.apache.org and is used
  to compile, build and install the software. 



Deployment and codebase webserver

  A critical component of the system is the codebase server. This
  consists of a web-server that can serve class-files to the
  applications as they need them. A small web-server is provided with
  this distribution, and its default codebase (the directory from
  which it serves its files) is in the test/cbs directory. The script
  text/bin/pc/r1_httpd.bat starts the web-server and its code is in
  the lib/tools.jar archive which is part of the Jini distribution.

  However, if you have access to another webserver which you think
  would be better for you, you can use that. Just remember to change
  the launching scripts in test/bin/{pc,unix} to provide the correct
  URL to the applications as they start, and to arrange for an easy
  way to upload new versions of the jar files each time you rebuild
  the system.

  The CHAT system uses three different codebases:

  http://some.host/.../	     	       For the Jini middleware
  http://some.host/.../server	       For the chat server
  http://some.host/.../client	       For the chat client

  The Jini jar-files will be duplicated in all three, but that is
  because they are needed by each. The reason for separating them like
  this is to hammer home the point that they are separate applications
  that could just as well be deployed on separate webservers, with
  individual URLs.




Testing platform

  For testing a PC must be involved, and the reason is Jini. The
  chat-server and client will not find each other unless a Jini lookup
  service is running. The lookup server is actually an RMI service, so
  it needs rmid (the RMI daemon) running on its computer.



INSTALLING

  Installation is easy, just unzip the distribution archive into a
  folder of your choice.

  Then edit the following file:

      develop/build.xml              Path to webserver directory

  Verify that you are able to:

    - compile the sources

    - install the compiled and provided jar-files on the webserver:

      	in the web-server's root directory:
	  jini-core.jar              copied from develop/lib
	  jini-ext.jar               copied from develop/lib
	  reggie-dl.jar              copied from develop/lib


        in the web-server's client directory:
          ChatClient-dl.jar          compiled from sources
	  ChatClient.jar             compiled from sources
	  jini-core.jar              copied from develop/lib
	  jini-ext.jar               copied from develop/lib
	  reggie-dl.jar              copied from develop/lib

        in the web-server's server directory:
          ChatServer-dl.jar          compiled from sources
	  ChatServer.jar             compiled from sources
	  jini-core.jar              copied from develop/lib
	  jini-ext.jar               copied from develop/lib
	  reggie-dl.jar              copied from develop/lib

    - verify that files can be fetched by opening a web browser and
      pointing it to the URLs of the files.

  It is important that you are able to automatise the installation on
  the webserver, because it is otherwise easy to forget that step when
  chasing a bug over frequent recompilations.


  Assuming you are running under Windows, in the test directory tree,
  locate the folder test/bin/pc:

  By default you are assumed to be using the provided web-server for
  serving the codebase. 

    test/bin/pc/r1_httpd.bat         The location of files to serve
                                     (only if used, of course)

    test/bin/pc/r3_reggie.bat        The URL to your webserver. Look for
                                     Set CBS=http: ... etc

      The lookup server (reggie) needs a codebase for the file
      reggie-dl.jar, and if properly done this would be separate from
      the chat-client and service. But we can point to either the
      chat-service or the client since both can serve up that
      particular file.

    test/bin/pc/chatclient.bat       The URL to your webserver

    test/bin/pc/chatservice.bat      The URL to your webserver


  If you are running a local webserver, open a command window, cd to
  test/bin/pc  and execute:

    r1_httpd

  Use a web-browser to verify that it works.


  You are now ready to start the Jini system. Open a command window,
  cd to test/bin/pc and execute:

    r2_rmid

  The prompt should return after a few seconds. The rmid program
  starts in a minimized window. Then execute:

    r3_reggie

  This launches the Jini lookup service. If all goes well the prompt
  returns after a while. The lookup service will have registered
  itself with rmid and exited. When a request for the service arrives,
  rmid will invisibly reactivate the lookup service.

  The local webserver, rmid and the lookup server can now be left
  running while you start and stop the chat-server and
  clients. However, when you want to shut them down, give the
  following command in any command window:

    rmid -stop

  This terminates rmid and when it is dead it can no longer reactivate
  the lookup service.

  The httpd implementation can be stopped by ^C or by closing its
  window.



  Then run the chat-server by opening a new command window, cd to
  test/bin/pc   and execute:

    chatserver

  A normal startup should look similar to this:

D:\TEMP\pis\test\bin\pc>chatserver
Server ChatServer  on portage started.
Server> Registered as Jini service 6c1cc230-f2b3-4b3c-a62c-e30c5c1ba271

  Here is the syntax for the chatserver script:

     chatserver [-n name]|[-h|--help]

  where   name   is a cute name for the server instance.


  Run the chat-client by opening a new command window, cd to
  test/bin/pc  and execute:

    chatclient

  A normal launch should look close to this:

D:\TEMP\pis\test\bin\pc>chatclient
[Output from the client is in braces]
[Commands start with '.' (period). Try .help]
[When connected, type text and hit return to send]
Client>

  When the chat-client finds a chat-server a message like this
  appears:

[Added server net.jini.core.lookup.ServiceItem@1bc82e7]

  The chat-client has no commandline options. And keep in mind that
  the commands to it begin with a period ('.').

