27-jan-2011/FK
03-feb-2009/FK
07-nov-2006/FK

Please remember that this is the second lab. The first lab will provide
more background knowledge.

This distribution is similar to that of lab1, but there are more files
and more packages to deal with:

dsv.pis.gotag.util	  Commandline and logging utilities
dsv.pis.gotag.bailiff	  A Jini-aware execution service for mobile Java code.
dsv.pis.gotag.dexter	  A small agent that jumps randomly between Bailiffs
dsv.pis.gotag.deedee	  A messenger agent that wants to deliver a text
			  message on a Bailiff that belongs to a certain user.


Compiling, building, and installing is all managed by the file
build.xml, which is an ant script. Ant is a free software build tool
which can be found at

    http://ant.apache.org/

When ant is successfully installed, open a command prompt/shell and
change to the develop directory. Typing 

    > ant

at the command prompt should compile the sources and compose the
jar-files. The directories build and dist are created to hold the
generated file. The build directory holds the Java class files and the
dist (distribution) contains the jar-files.

To install the jar-files in the test branch of the software tree, type
the command

    > ant install

which copies the needed files over to test directories.

Sometimes it is valuable to make sure everything has been recompiled
and rebuilt fresh. To remove all generated files (your sources are not
touched), give the command

    > ant clean

The above three commands are targets that can be found at the end of
the build.xml file.


You can also compile and install by using the BAT-files in the
develop/bat directory, but this is not recommended as they have not
been maintained for some time.



			  The TAG assignment

The challenge is to create the Game of Tag for mobile software agents.

The game of tag is a child's game, played minimally by two players. It
is very simple. One player is choosen to be 'it'. That player's task
is to run up to one of the other players and touch him or her, saying
"You're it!", whereupon the 'it' property and associated task is
transferred to the other player. The players who are currently not
'it', try to evade being tagged, usually by running and hiding.

Using the provided software as inspiration, you must design and
implement the game of tag for at least three mobile agents. The
playfield consists of three or more Bailiffs (execution servers).

There are two important requirements on which the game's
implementation depend:

 (1) tagging can only be done between players in the same Bailiff

 (2) the tag (the 'it' property) must be passed reliably from one
     player to another. It must not be lost or duplicated during the
     transaction.

From the given code we know that Jini will help each player to
discover Bailiffs. You may want to use the room or user properties of
the Bailiff to define 'your' playfield. Hint: look at how Deedee finds
the Bailiff she is looking for.

From (1) we realise that a player needs to know how the other players
are distributed among the Bailiffs. The player being 'it', needs to
find a Bailiff with players in it, move there, select a victim and
then attempt to tag that victim. The other players all want to know
where the player being 'it' is located, so that they can avoid being
tagged, by moving to another Bailiff if the player being 'it' arrives
in their Bailiff.

From the perspective of a player agent, you first want to know which
Bailiffs that are currently in the playfield. Then for each Bailiff,
you want to be able to get some information about it. These items are
all relevant when examining a Bailiff participating in the game:

    - Is it the Bailiff the agent is currently in?
    - Are there any players in it?
    - Is any of the players in the Bailiff 'it'?
    - You're it!

You must modify the Bailiff and the Bailiff service interface to make
it possible for the playing agent to get answers for these questions.
You must of course also construct the agent to be able to ask and
answer questions.


"Is it the Bailiff the agent is currently in?"

By default, the agent does not know which Bailiff it is in once it has
migrated to it and started to execute. The agent can discover all
Bailiffs using Jini, but which one is the local one? However, the
agent can make a local method call to Bailiff.getProperty() and find
an identification string stored there, if the Bailiff has prepared
one. It can then query the remote service objects of the Bailiffs it
has discovered (BailiffInterface.getProperty()) and compare the
returned strings until there is a match. But read on, there may be
another way to do this.


"Are there any players in it?"

To be able to answer this question, the Bailiff must keep track of the
number of agents that are executing in it. This is fairly easy to do,
for example by maintaining a (synchronized) counter which is
incremented when an Agitator (class Agitator is internal in class
Bailiff) starts and decremented when it exits. However, with a
slightly more advanced data structure, additional possibilities
open. More about this below.


"Is any of the players in the Bailiff 'it'?"

The question if an agent is 'it' should not be answered by the
Bailiff, but by the other agent. The Bailiff, however, must somehow
allow the agents to communicate. This can be done locally, or
remotely, or both.

Local communication between agents in the same Bailiff just needs an
ordinary object reference to work. During the method call, the calling
agent's thread executes a method in the instance of the called agent,
to read information or leave a message.

Communication between agents in different Bailiffs is a bit more
complicated, but also much more interesting because it makes it
possible to create a smarter agent. There are two ways to achieve
remote agent-to-agent communication: Bailiff proxy calls or
full-service agents.

When the Bailiff acts as a proxy, the Bailiff's service interface can
be modified to provide methods which allows a client to reach an agent
executing in the Bailiff. It needs some kind of reference, obviously,
like a string ID for example, so that the Bailiff knows which agent to
talk to locally. For reasons explained below, this is probably the
most economical approach for agent communication in this assignment.


      +--------------+                       +--------------+
      |              |                       |              |
      |              |                       |              |
      |  Bailiff 1   |                       |  Bailiff 2   |
      |              |                       |              |
      |              |                       |              |
      |  [Agent a]-----------m("b",msg)----->m-->[Agent b]  |
      |              |                       |              |
      |              |                       |   [Agent c]  |
      |              |                       |              |
      |              |                       |              |
      +--------------+                       +--------------+

  [In this figure, agent a executes in Bailiff 1. To communicate
   with Agent b, executing in Bailiff 2, it calls a service
   method on Bailiff 2. Bailiff 2 then makes a local method
   call to agent b.]


Full-service agents implement their own remote service interface, just
like the Bailiffs, and can be contacted directly. But before the
client can use it, the client needs to obtain an instance of
the agent's service interface.

Agents could of course be designed to use the Jini system, just like
the Bailiffs do. However, full-service agents in the game of Tag are
very inefficient, because the agents keep moving around, and the
service interface they register with the Jini lookup server would only
be valid for the time the agent remains in its current Bailiff. As
soon as the agent migrates, the old instance of the agent exits, and
the service interface instance which refers to it becomes
unusable. The new instance of the agent must re-register itself with
the Jini lookup service, and this will add considerable overhead to
the whole system.

Instead of using the Jini lookup server, Bailiffs can be made to
provide the agent's service interface, but this may be overkill since
you will still need some kind of proxy functionality (see above) in
the Bailiff to find and retrieve the service object from the correct
agent.

Two additional technologies exist and they will be mentioned for
completeness. In the first, Jini-enabled applications (like the
agents) can exchange messages by implementing interface
net.jini.core.event.RemoteEventListener. A sender then creates
instances of net.jini.core.event.RemoteEvent (if needed, properly
subclassed to contain the application data), and sends them. However,
once more we arrive at the question of how to locate and retrieve the
necessary remote reference to the agent we want to talk to.

[For Jini API documentation, please see:
 http://www.gigaspaces.com/docs/JiniApi/index.html]

In the second case, some external messaging middleware, completely
unrelated to Jini could be implemented and used. The major advantage
with such an approach, is that the identity of an agent would be
preserved by the middleware, just like in an email system. The agent
would enter a Bailiff, log in with the messaging system, exchange
messages, log out and migrate, and it could keep its identity (message
system address) regardless of which Bailiff it happened to be
executing in. The effort of implementing such a system, however, is
far too high for the small needs of the game of Tag.

For the above reasons, it appears as if the Bailiff proxy approach is
the best. This means that an agent repeatedly acts in the following
way:

    - Discover Bailiffs
    - For each Bailiff:
      = Retrieve the names of agents in it
      = Examine the names:
         - no names found => no agents are in that Bailiff
	 - the agent finds its own name => it knows where it is now
	 - the remaining names are used to contact the other agents

The scheme outlined here requires that each agent has a name
(identifier). Since the namespace is scoped by the Bailiff
(Bailiff-instance:agent-instance), the only strong requirement we have
is that the agent's name is unique within the set of agents currently
executing in the Bailiff, and that old names are not reused. A simple
counter which increments by one for each accepted agent migrating to
the Bailiff, would seem to meet this. Unfortunately, that is not so.

The reason is that with a small integer as the name, the agent is not
able to detect itself reliably. There could two agents with the same
number, executing in different Bailiffs. The agent would not be able
to tell which one refers to itself.

The way out is of course to include some unique property of the
Bailiff in the temporary name, like the IP-number and port, and
include this in the name. For example:

			 192.168.1.6:8976:42
                         ----IPnr---:port:counter

Even though the IPnr will be shared by all Bailiffs running on the
same computer, TCP or UDP port numbers are non-sharable, so the
Bailiff would be uniqely identified. The disadvantage is that the
Bailiff would have to open a port just to obtain the number, and not
for communication purposes.

On the other hand, we could use the user or room property strings
already built in the Bailiff, but these must be set manually from the
commandline and an automatic solution is more reliable and much easier
to maintain. There is then no need to remember unique Bailiff names
or to create separate launch scripts for each instance.

Since we now know that agents need to have universially unique names
(identifers), and we are not truly interested in the identity of the
Bailiff, why not do it like that? Let the agent generate its own UUID
when it starts, and then keep this name while it plays [see class
java.util.UUID.randomUUID()]. So, rather than having the Bailiff
assign the agent a temporary name, the Bailiff asks the agent what its
name is.

Finally, the Bailiff will need a data structure that maps from UUIDs
to the local objects that are the agents themselves. The agent should
be added to the map when the Agitator starts, and removed when it
stops, even if it stopped because of an exception. The data structure
must be protected against concurrent modification, because an agent
may migrate to the Bailiff at any time. A suitable instance of
interface java.util.Map is java.util.HashMap.

It must also be possible to create a list of the names of the
currently active agents.  For the namelist, start looking at method
java.util.Map.keySet(). Do not send the whole map instance as the
response, extract the names into something nice, like an array of
String, or something which implements interface java.util.List.


"Is any of the players in the Bailiff 'it'?"

Using the proxy method outlined above, we can now answer this question
in several ways. We have already the list of names of agents in a
Bailiff. To find out if a particular agent is 'it' or not, we can ask
it by way of the Bailiff, for example like this (T in the type
declaration should be replaced by the type of the parameter choosen by
you in your work):

  The method used by the agent calling the Bailiff (defined in
  interface BailiffInterface):

    public boolean isIt (T name) throws java.rmi.RemoteException;

  Its implementation in the Bailiff:

    public boolean isIt (T name) { ... }

  In the agent:

    public boolean isIt () { ... }


The caller should expect exceptions to be thrown, because there may be
several reasons for them, all likely:

   - A network error prevents communication
   - The Bailiff has shut down or crashed
   - The remote agent has migrated and the question no longer applies



"You're it!"

This message can be implemented in the same way an agent asks another
agent if it is 'it'. The agent doing the tagging sends the message,
while the agent being tagged responds. There are two responses: the
tagging succeeded and the tagged agent does now recognise that it is
'it', or, the tagging failed. In addition to this, exceptions may be
thrown (see above) in which case the tagging should of course be
regarded has having failed.

There is one particular scenario in which the tagging should fail, and
this concerns agent migration. When an agent has decided to migrate to
another Bailiff (perhaps because the agent being 'it' just showed up
in the current Bailiff), it locates another Bailiff and calls its
migrate method, sending a copy of itself. The call to migrate blocks,
and although its own execution is suspended, a proxy call from another
agent may still be able to execute methods in it, because it comes
from a listening thread in the RMI subsystem.

Assume now, that an agent is blocking on migrate. The 'it' player
calls and tries to tag it. The implementation is naive, and just sets
the 'it' property of the blocked agent. The caller believes that the
tagging was successful, and sets itself not to be 'it' anymore. Then
the call to migrate returns; it was a success and a copy of the agent
is already executing on the other Bailiff. Since the migration worked,
the original agent exits. As it disappears from the game, so does the
'it' property, and the tag has been lost.

It is for the above reason that some kind of synchronization mechanism
must be employed while tagging. Essentially, while an agent is blocked
in its call to migrate, it cannot be tagged, and any attempts to do so
must fail. If the call to migrate fails, then the agent continues to
play, and again becomes taggable.

Another spooky variant of this error seen previously on the course, is
the Horror of the Ghostly Clones. In this story an agent tries to tag
another agent, by way of the Bailiff, and the call succeeds nicely
through all the synchronization. Since the call succeeds, the previous
'it' player untags itself and continues to play as prey. However, the
victim seems ignorant of the fact that is has been tagged, and plays
on as if the successful tagging never happened.

The source of this bug was in the Bailiff, and in the management of
the name-to-agent lookup table. In short, the Bailiff did not
correctly remove an agent from the table when the agent had migrated
away. Although the thread in the old copy had exited, the object was
still reachable, and method calls could be made on it. This meant that
the caller could indeed tag it, but instead of tagging an active agent
it tagged the ghost of the dead copy left behind from migration.


			    Doing the work

First, study the provided sources and learn how the programs
work. Then go back to the instruction earlier in this text, and start
planning:

    - what to do
    - where to do it (i.e. which files do you need to modify)
    - how to do it (checklists, editors)
    - how to determine if it works (testing, debugging)

You will need the Bailiff, and you will need to make changes to
it. The player agent can be created by modifying Deedee or Dexter, or
by creating an entirely new agent with code copied from both. There is
no right or wrong here, but it may be easier to recompile things if
you keep one of the example agents and just change its code, even if
that means a lot of changes. But your are free to make any changes you
see fit, adding or dropping classes etc.

When your plan is done, make sure you can compile and run the
system BEFORE you start modifying it. 

Then implement the changes. If you can, start with something small and
confirm that it works as you intended.

When you get compilation errors, read them carefully and remember that
the first error is usually the significant one. Subsequent errors
often follow as a consequense of the first problem.

When you get run-time exceptions, read them carefully and try to
understand what is happening. Remember that exceptions are usually
nested, meaning that the critical point of failure is to be found near
the top of the list. For remote exceptions, it will be the case that
the exception is printed in the client, although it actually happened
on the server.

Online documentation for the Java API is here:

       http://download.oracle.com/javase/6/docs/api/

Please use kth.se/social or email for questions.


			     Examination

Both labs are done in groups of two students. Groups with only one
member are allowed in exceptional cases. Groups must be registered in
Daisy.

The grades given for the two labs combined are PASS or FAIL (godkänd
eller underkänd). The grades are individual, which means that one
group member may be passed and the other failed, if it does not seem
credible that that member did contribute to the work.

Both labs (CHAT and TAG) must be passed for a passing grade on the lab
component of the course. Work which is not up to a passing standard but
is judged to be salvagable may be required to provide additional
material before being graded.

For lab TAG you must do the following:

    - Implement the game of Tag using the provided material.

    - Write, print and submit a short report on your design choices,
      and problems encountered and solved. Remember to put the
      following on the title sheet:
      = PRIS/LAB TAG
      = you group number
      = both your names.

    - Give an oral presentation and show a running implementation. The
      result must execute and demonstrate (textually or graphically)
      that the game is being played by three players on a playfield of
      three Bailiffs. [The group and the examiner sits down together
      at the computers.  The group runs their system and explains
      briefly what they have done. The examiner asks questions as
      necessary.]





Good luck and remember to have fun!
