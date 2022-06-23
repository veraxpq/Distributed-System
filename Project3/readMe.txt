Assignment overview

This project helps us explore split the servers into several replicas, which is practical in real life. For example,
there are people from the United States and China using the key-value pair service, they want to use the same service
and share common resources. In order to provide consistent service in short response time, we can provide servers in
China and the United States, so that the transmission time would be shorter. But there is a problem of inconsistency,
since these are services are run in seperated machines. To solve the problem, we can make use of a coordinator, which
can keep the resources consistent. When users send get requests, since these requests do not modify data, we can return
the result directly. When users send put or delete requests, since these requests modify data, we need to keep all the
data sources in servers the latest value. So the coordinator collect the put and delete requests, send the command to
all servers to execute the same operations. In the process, if any of the servers fail to execute the request, the
coordinator would make all servers roll back to the previous status. And we can regard this process as a transaction,
we should always keep it atomic, they should all succeed or all fail.


Technical impression

One of the biggest challenges I encountered in this project was designing the server. The server acts as different
parts in this project, one is a server that provides key-value service, the other is a participant in the transaction.
So I implemented two interfaces, one is MapService, the other is Participant.

There are 3 methods in MapService:
"String map(String command)" to execute the command from client or coordinator; "boolean isGetRequest(String command)"
to return if the command is a get request; "String originalKVPair()", to return the operation to roll back the execution.

There are 3 methods in the interface Participant: vote() to execute the command from the coordinator, and return if it
succeeds or fails; abort() to execute the operation of rolling back.

There are 2 methods in interface coordinator: connect() to record the servers connect to the coordinator; startCommit()
to make all the servers execute the modified operation, and roll back if any of them fails.

On the client side, the client can send to any of the 5 servers. To make it easier to test, I put all the servers into
a list, and the servers execute the request according to their orders in periodically.



How to run the server and client codes:

1. open the terminal, enter into the current directory

2. run the coordinator in terminal by input the below command line:
java -cp project3.jar server.CoordinatorService 30000

3. open another terminal, run the server1 in terminal by input the below command line:
 java -cp project3.jar server.Server 30000 30001 31001

4.open another terminal, run the server2 in terminal by input the below command line:
 java -cp project3.jar server.Server 30000 30002 31002

5. open another terminal, run the server3 in terminal by input the below command line:
 java -cp project3.jar server.Server 30000 30003 31003

6. open another terminal, run the server4 in terminal by input the below command line:
 java -cp project3.jar server.Server 30000 30004 31004

7. open another terminal, run the server5 in terminal by input the below command line:
 java -cp project3.jar server.Server 30000 30005 31005

8. open another terminal, run the client in terminal by input the below command line:
java -cp project3.jar client.Client 30001 30002 30003 30004 30005

5. You can see "Input the command in this format: PUT/GET/DELETE KEY VALUE" in the console
Input "PUT KEY VALUE", (for example PUT 3 2) to store a key-value pair in the server.
Input "GET KEY VALUE", (for example GET E) to retrieve the value for the key.
Input "DELETE KEY", (for example DELETE 3) to delete a key-value pair.

In the console of Server and coordinator, we can see the corresponding output.
