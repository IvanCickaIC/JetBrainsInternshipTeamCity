# Chat Application

This chat application allows users to create or join chat servers. The `ChatRegistryServer` manages all active chat servers, and clients can connect to these servers using the `ChatClient`.

## Setup Instructions

1. **Start the Chat Registry Server**

   To register and manage all active chat servers, the `ChatRegistryServer` must be started first. Follow these steps:

   1. Navigate to the project directory where the `ChatRegistryServer` is located:

      ```
      cd TeamCity/src/main/java
      ```

   2. Run the following command to start the `ChatRegistryServer`:

      ```
      java jetbrain.internship.ChatRegistryServer
      ```

2. **Start the Chat Client**

   After starting the registry server, you can run the `ChatClient` to create or join chat servers. Use the following command:

      ```
      java jetbrain.internship.ChatClient
      ```