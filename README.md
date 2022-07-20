# Messaging application 💬
Chat with your friends with ease!

## Installation of the JMS
In order to run this application, you should have a JMS (Java Message Service) provider.
In my case, it is OpenJMS.
You can download it [here](http://openjms.sourceforge.net/downloads.html).

**Important note**: OpenJMS works with JDK 1.8 or earlier.

Next, you should create the _OPENJMS_HOME_ environment variable,
which references main directory of the OpenJMS (by default, the directory name is _openjms-0.7.7-beta-1_).
In the _bin_ directory, you will be able to find _admin.bat_ (for Windows users). Run it.

Now, you are ready to run the application!

## Case
In the example below, we have one group with 3 members (private chats are available, too).

2 members are online and write messages, while the 3rd member is offline.

However, when the 3rd member opens the application, he/she will be able to read messages sent by the other members earlier.

![image](https://user-images.githubusercontent.com/70007684/180035302-39b6d605-dcbe-4902-a18b-547614953885.png)

Two members of the group have a chat.

---

![image](https://user-images.githubusercontent.com/70007684/180035518-3bffbba5-5042-473b-86a8-c8f15d59ac3e.png)

This is administrator's control panel.
To the right side of _theThirdChatUser_, you can see the number **3**. It means, that this user has **3** unread messages.

---

![image](https://user-images.githubusercontent.com/70007684/180036019-31a3ebee-f68a-4382-9545-41457364df9e.png)

Some time later, the 3rd member of the group opens the application, and now can join the conversation.

---
